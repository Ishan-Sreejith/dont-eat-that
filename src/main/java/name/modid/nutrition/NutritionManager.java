package name.modid.nutrition;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import name.modid.DontEatThat;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NutritionManager {
    private static final Map<UUID, NutritionState> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> NEXT_DODGE_TICK = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> PENDING_HEAL = new ConcurrentHashMap<>();
    private static int decayTicker = 0;
    private static final Map<UUID, Integer> REMOVAL_TIMERS = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(NutritionManager.class);

    private NutritionManager() {
    }

    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            STATES.remove(handler.player.getUuid());
            NEXT_DODGE_TICK.remove(handler.player.getUuid());
            PENDING_HEAL.remove(handler.player.getUuid());
            REMOVAL_TIMERS.remove(handler.player.getUuid());
        });
    }

    public static void scheduleNutrientRemoval(ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        REMOVAL_TIMERS.put(player.getUuid(), 40);
        LOGGER.debug("Scheduled nutrient removal for player {}", player.getName().getString());
    }

    public static NutritionState get(ServerPlayerEntity player) {
        return STATES.computeIfAbsent(player.getUuid(), id -> new NutritionState());
    }

    public static void tickServer(MinecraftServer server) {
        decayTicker++;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            NutritionState state = get(player);
            boolean changed = false;
            Integer removalTimer = REMOVAL_TIMERS.get(player.getUuid());
            if (removalTimer != null && removalTimer > 0) {
                int next = removalTimer - 1;
                if (next <= 0) {
                    if (state.popRecentPoint()) {
                        changed = true;
                    }
                    REMOVAL_TIMERS.remove(player.getUuid());
                } else {
                    REMOVAL_TIMERS.put(player.getUuid(), next);
                }
            }
            applyPendingHeal(player);
            float absorptionMultiplier = DontEatThat.config.enableFiber ? state.absorptionMultiplier() : 1.0f;
            changed |= state.tickDigestion(absorptionMultiplier);
            if (decayTicker % 80 == 0) {
                changed |= applyDecay(player, state, absorptionMultiplier);
            }
            changed |= applyEffects(player, state);
            if (changed && player.age % 20 == 0) {
                sync(player);
            }
        }
    }

    private static boolean applyDecay(ServerPlayerEntity player, NutritionState state, float absorptionMultiplier) {
        boolean changed = false;
        float decayScale = MathHelper.clamp(1.15f - ((absorptionMultiplier - 1.0f) * 0.9f), 0.70f, 1.15f);
        float totalDecay = (0.035f + 0.045f + 0.060f + 0.030f + 0.040f + 0.025f) * decayScale * DontEatThat.config.decayMultiplier;
        if (player.getHungerManager().getFoodLevel() <= 6) {
            totalDecay += 0.02f * NutrientType.values().length * DontEatThat.config.decayMultiplier;
        }
        changed |= state.consumeLeftmost(totalDecay);
        return changed;
    }

    public static void onFoodEaten(ServerPlayerEntity player, ItemStack stack) {
        if (player == null || stack == null) {
            return;
        }
        LOGGER.debug("Player {} ate food {}", player.getName().getString(), stack.getName().getString());
        NutritionState state = get(player);
        EnumMap<NutrientType, Float> nutrients = FoodNutrients.forStack(stack);
        if (nutrients.isEmpty()) {
            return;
        }
        if (stack.getItem() == Items.MUSHROOM_STEW || stack.getItem() == Items.BEETROOT_SOUP) {
            scaleSoup(nutrients, 2);
        } else if (stack.getItem() == Items.SUSPICIOUS_STEW) {
            scaleSoup(nutrients, 3);
        } else if (stack.getItem() == Items.RABBIT_STEW) {
            scaleSoup(nutrients, 4);
        }

        if (stack.getItem() == Items.BEETROOT_SOUP) {
            StatusEffectInstance existing = player.getStatusEffect(StatusEffects.ABSORPTION);
            int amp = (existing != null) ? Math.min(existing.getAmplifier() + 1, 5) : 1;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 4800, amp));
        }

        for (Map.Entry<NutrientType, Float> entry : nutrients.entrySet()) {
            state.addNow(entry.getKey(), entry.getValue());
        }
        state.addPoints(nutrients);

        if (FoodNutrients.isRawMeat(stack) && state.level(NutrientType.VITAMIN_C) < 1.5f) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 20 * 6, 0));
        }
        if (FoodNutrients.isDesperationFood(stack)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 120, 0));
        }

        sync(player);
    }

    public static boolean tryDodge(ServerPlayerEntity player, DamageSource source) {
        if (source.isIn(DamageTypeTags.IS_FALL)) {
            return false;
        }
        NutritionState state = get(player);
        float b6 = state.level(NutrientType.VITAMIN_B6);
        if (b6 < 1.5f) {
            return false;
        }

        int icons = Math.max(1, (int) Math.floor(b6));
        int cooldownTicks = dodgeCooldownTicks(icons);
        int now = player.age;
        int next = NEXT_DODGE_TICK.getOrDefault(player.getUuid(), 0);
        if (now < next) {
            return false;
        }
        NEXT_DODGE_TICK.put(player.getUuid(), now + cooldownTicks);
        return true;
    }

    private static final Map<UUID, Boolean> CURSE_GUARD = new ConcurrentHashMap<>();

    public static void onDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        if (amount <= 0.0f || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (CURSE_GUARD.putIfAbsent(player.getUuid(), true) != null) {
            return;
        }
        try {
            NutritionState state = get(player);
            int spikyPoints = state.spikyPointCount();
            applySpikyThorns(player, source, amount, spikyPoints);

            if (spikyPoints >= 5 && isCurseDamage(player, source)) {
                float multiplier = spikyPoints * 0.2f;
                float bonus = amount * multiplier + spikyPoints;
                if (bonus > 0) {
                    player.damage(player.getDamageSources().genericKill(), bonus);
                }
            }

            float e = state.level(NutrientType.VITAMIN_E);
            if (e >= 1.5f && amount > 0.0f) {
                int icons = Math.max(1, (int) Math.floor(e));
                float reduction = Math.min(0.50f, icons * 0.10f);
                float healBack = amount * reduction;
                if (healBack > 0.0f) {
                    PENDING_HEAL.merge(player.getUuid(), healBack, Float::sum);
                }
            }

            float severity = MathHelper.clamp(amount / 10.0f, 0.05f, 0.5f);
            for (NutrientType type : NutrientType.values()) {
                state.reduce(type, severity * 0.08f);
            }
            if (player.age % 5 == 0) {
                sync(player);
            }
        } finally {
            CURSE_GUARD.remove(player.getUuid());
        }
    }

    private static boolean isCurseDamage(ServerPlayerEntity player, DamageSource source) {
        return source.isIn(DamageTypeTags.IS_FALL)
            || source.isIn(DamageTypeTags.IS_FIRE)
            || source.isIn(DamageTypeTags.IS_DROWNING)
            || source == player.getDamageSources().inWall()
            || source == player.getDamageSources().starve()
            || source == player.getDamageSources().magic();
    }

    private static final int EFFECT_DURATION = 1200; // 60 seconds

    private static boolean applyEffects(ServerPlayerEntity player, NutritionState state) {
        if (player == null || state == null || !DontEatThat.config.enableEffects) {
            return false;
        }
        boolean changed = false;
        float vitaminA = state.level(NutrientType.VITAMIN_A);
        float vitaminB6 = state.level(NutrientType.VITAMIN_B6);
        float vitaminC = state.level(NutrientType.VITAMIN_C);
        float vitaminK = state.level(NutrientType.VITAMIN_K);

        if (vitaminA >= 1.5f) {
            if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, EFFECT_DURATION, 0, true, false, true));
                changed = true;
            }
        } else {
            if (player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                changed = true;
            }
        }

        boolean needsRegen = false;
        int regenAmp = 0;

        if (vitaminC >= 1.5f) {
            if (player.hasStatusEffect(StatusEffects.POISON)) {
                player.removeStatusEffect(StatusEffects.POISON);
            }
            needsRegen = true;
            regenAmp = Math.max(regenAmp, vitaminC >= 5.0f ? 1 : 0);
            if (player.age % 80 == 0) {
                player.heal(0.5f);
            }
        }

        if (vitaminK >= 1.5f) {
            needsRegen = true;
            regenAmp = Math.max(regenAmp, vitaminK >= 4.0f ? 1 : 0);
        }

        if (needsRegen) {
            if (!player.hasStatusEffect(StatusEffects.REGENERATION) || player.getStatusEffect(StatusEffects.REGENERATION).getAmplifier() < regenAmp) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, EFFECT_DURATION, regenAmp, true, false, true));
                changed = true;
            }
        } else {
            if (player.hasStatusEffect(StatusEffects.REGENERATION)) {
                player.removeStatusEffect(StatusEffects.REGENERATION);
                changed = true;
            }
        }

        if (vitaminB6 >= 1.5f) {
            if (!player.hasStatusEffect(StatusEffects.LUCK)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, EFFECT_DURATION, 0, true, false, true));
                changed = true;
            }
        } else {
            if (player.hasStatusEffect(StatusEffects.LUCK)) {
                player.removeStatusEffect(StatusEffects.LUCK);
                changed = true;
            }
        }

        if (changed) {
            LOGGER.debug("Applied status effects to player {}", player.getName().getString());
        }
        return changed;
    }

    private static int dodgeCooldownTicks(int icons) {
        if (icons >= 7) {
            return 20;
        }
        if (icons >= 5) {
            return 40;
        }
        if (icons >= 3) {
            return 80;
        }
        if (icons >= 2) {
            return 100;
        }
        return 200;
    }

    private static void applySpikyThorns(ServerPlayerEntity player, DamageSource source, float amount, int spikyPoints) {
        if (spikyPoints < 5) {
            return;
        }
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        if (!(source.getAttacker() instanceof LivingEntity attacker)) {
            return;
        }
        float multiplier = spikyPoints * 0.2f;
        float reflect = amount * multiplier;
        if (reflect > 0) {
            attacker.damage(player.getDamageSources().thorns(player), reflect);
        }
    }

    private static void applyPendingHeal(ServerPlayerEntity player) {
        Float amount = PENDING_HEAL.remove(player.getUuid());
        if (amount == null || amount <= 0.0f || !player.isAlive()) {
            return;
        }
        player.heal(amount);
    }

    private static void scaleSoup(EnumMap<NutrientType, Float> nutrients, int ingredientCount) {
        float scale = switch (ingredientCount) {
            case 2 -> 0.50f;
            case 3 -> 0.33f;
            default -> 0.25f;
        };
        float floor = NutritionConfig.SOUP_FLOOR;
        for (Map.Entry<NutrientType, Float> entry : nutrients.entrySet()) {
            if (entry.getValue() <= 0.0f) {
                continue;
            }
            entry.setValue(Math.max(floor, entry.getValue() * scale));
        }
    }

    public static void sync(ServerPlayerEntity player) {
        NutritionState state = get(player);
        var buf = PacketByteBufs.create();
        state.writeToBuf(buf);
        ServerPlayNetworking.send(player, NutritionNetworking.SYNC_ID, buf);
    }

    public static void copy(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        STATES.put(newPlayer.getUuid(), get(oldPlayer).copy());
        sync(newPlayer);
    }

    public static void load(ServerPlayerEntity player, NutritionState state) {
        STATES.put(player.getUuid(), state);
    }
}
