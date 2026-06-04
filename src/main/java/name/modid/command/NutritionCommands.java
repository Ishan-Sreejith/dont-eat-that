package name.modid.command;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import name.modid.DontEatThat;
import name.modid.nutrition.NutrientType;
import name.modid.nutrition.NutritionManager;
import name.modid.nutrition.NutritionState;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NutritionCommands {
    public static final Identifier REQUEST_STATS_ID = new Identifier(DontEatThat.MOD_ID, "request_stats");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_STATS_ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                NutritionState state = NutritionManager.get(player);
                player.sendMessage(net.minecraft.text.Text.literal("=== Nutrition ==="));
                for (NutrientType type : NutrientType.values()) {
                    player.sendMessage(net.minecraft.text.Text.literal(type.name() + ": " + String.format("%.1f", state.level(type))));
                }
            });
        });
    }

    public static void clear(ServerPlayerEntity player) {
        NutritionState state = NutritionManager.get(player);
        state.clearRecentPoints();
        NutritionManager.sync(player);
    }
}
