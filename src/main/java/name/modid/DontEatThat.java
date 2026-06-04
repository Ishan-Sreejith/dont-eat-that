package name.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import name.modid.command.NutritionCommands;
import name.modid.config.ModConfig;
import name.modid.nutrition.NutrientType;
import name.modid.nutrition.NutritionManager;
import name.modid.nutrition.NutritionNetworking;
import name.modid.nutrition.NutritionState;
import name.modid.registry.ModBlocks;
import name.modid.registry.ModItems;
import name.modid.registry.ModLootTables;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public class DontEatThat implements ModInitializer {
	public static final String MOD_ID = "dont-eat-that";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfig config;

	private static final Set<Item> NUTRIENT_REDUCING_ITEMS = new HashSet<>();
	static {
		NUTRIENT_REDUCING_ITEMS.add(Items.WHEAT);
		NUTRIENT_REDUCING_ITEMS.add(Items.WHEAT_SEEDS);
		NUTRIENT_REDUCING_ITEMS.add(Items.MELON_SEEDS);
		NUTRIENT_REDUCING_ITEMS.add(Items.PUMPKIN_SEEDS);
		NUTRIENT_REDUCING_ITEMS.add(Items.BEETROOT_SEEDS);
		NUTRIENT_REDUCING_ITEMS.add(Items.TORCHFLOWER_SEEDS);
	}

	@Override
	public void onInitialize() {
		config = ModConfig.load();
		ModBlocks.register();
		ModItems.register();
		ModLootTables.register();
		NutritionNetworking.register();
		NutritionManager.init();
		NutritionCommands.init();

		ServerTickEvents.END_SERVER_TICK.register(server -> NutritionManager.tickServer(server));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> NutritionManager.sync(handler.player));
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> NutritionManager.copy(oldPlayer, newPlayer));
		ServerPlayNetworking.registerGlobalReceiver(NutritionNetworking.REQUEST_SYNC_ID,
				(server, player, handler, buf, responseSender) -> server.execute(() -> NutritionManager.sync(player)));

		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (hand != Hand.MAIN_HAND && hand != Hand.OFF_HAND) {
				return TypedActionResult.pass(player.getStackInHand(hand));
			}
			var stack = player.getStackInHand(hand);
			if (stack.isEmpty()) {
				return TypedActionResult.pass(stack);
			}
			Item item = stack.getItem();
			LOGGER.debug("UseItemCallback invoked for hand {} item {}", hand, item);
			if (item == Items.CACTUS) {
				return handleCactusEating(player, world, hand, stack);
			} else if (NUTRIENT_REDUCING_ITEMS.contains(item)) {
				return handleSeedEating(player, world, hand, stack, item);
			}
			return TypedActionResult.pass(stack);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			server.getCommandManager().getDispatcher().register(literal("nutrition")
				.requires(source -> source.hasPermissionLevel(0))
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayer();
					if (player == null) return 0;
					NutritionState state = NutritionManager.get(player);
					context.getSource().sendFeedback(() -> Text.literal("=== Nutrition ==="), false);
					for (NutrientType type : NutrientType.values()) {
						float level = state.level(type);
						String status = level < 1.5f ? "[LOW]" : (level > 10.0f ? "[EXCESS]" : (level >= 3.0f && level <= 8.5f ? "[GOOD]" : "[OK]"));
						context.getSource().sendFeedback(() -> Text.literal(type.name() + ": " + String.format("%.1f", level) + " " + status), false);
					}
					return 1;
				})
				.then(literal("clear").executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayer();
					if (player == null) return 0;
					NutritionCommands.clear(player);
					context.getSource().sendFeedback(() -> Text.literal("Nutrition points cleared."), false);
					return 1;
				}))
				);
		});

		LOGGER.info("Don't Eat That mod initialized.");
	}

	private static TypedActionResult<ItemStack> handleCactusEating(PlayerEntity player, World world, Hand hand, ItemStack stack) {
		if (player.isSneaking()) {
			return TypedActionResult.pass(stack);
		}
		if (player.getItemCooldownManager().isCoolingDown(Items.CACTUS)) {
			return TypedActionResult.fail(stack);
		}
		if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
			if (!serverPlayer.getAbilities().creativeMode) {
				stack.decrement(1);
			}
			serverPlayer.getHungerManager().add(1, 0.0f);
			serverPlayer.getHungerManager().addExhaustion(5.0f);
			serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 20 * 10, 1));
			serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 20 * 5, 0));
			serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20 * 4, 0));
			serverPlayer.damage(serverPlayer.getDamageSources().cactus(), 4.0f);
			serverPlayer.getItemCooldownManager().set(Items.CACTUS, 60);
			var consumed = Items.CACTUS.getDefaultStack();
			NutritionManager.onFoodEaten(serverPlayer, consumed);
			serverPlayer.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
			((net.minecraft.server.world.ServerWorld)serverPlayer.getWorld()).spawnParticles(ParticleTypes.DAMAGE_INDICATOR,
				serverPlayer.getX(), serverPlayer.getY() + serverPlayer.getHeight() / 2.0, serverPlayer.getZ(),
				5, 0.5, 0.5, 0.5, 0.1);
		}
		player.swingHand(hand, true);
		return TypedActionResult.success(stack, world.isClient());
	}

	private static TypedActionResult<ItemStack> handleSeedEating(PlayerEntity player, World world, Hand hand, ItemStack stack, Item item) {
		if (player.isSneaking()) {
			return TypedActionResult.pass(stack);
		}
		if (player.getItemCooldownManager().isCoolingDown(item)) {
			return TypedActionResult.fail(stack);
		}
		if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
			if (!serverPlayer.getAbilities().creativeMode) {
				stack.decrement(1);
			}
			NutritionManager.scheduleNutrientRemoval(serverPlayer);
			serverPlayer.getHungerManager().add(1, 0.0f);
			serverPlayer.getItemCooldownManager().set(item, 40);
			serverPlayer.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
			((net.minecraft.server.world.ServerWorld)serverPlayer.getWorld()).spawnParticles(ParticleTypes.ITEM_SLIME,
				serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(),
				4, 0.3, 0.3, 0.3, 0.05);
		}
		player.swingHand(hand, true);
		return TypedActionResult.success(stack, world.isClient());
	}
}
