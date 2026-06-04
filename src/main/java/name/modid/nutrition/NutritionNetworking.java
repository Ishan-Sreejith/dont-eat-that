package name.modid.nutrition;

import net.minecraft.util.Identifier;
import name.modid.DontEatThat;

public final class NutritionNetworking {
    public static final Identifier SYNC_ID = new Identifier(DontEatThat.MOD_ID, "nutrition_sync");
    public static final Identifier REQUEST_SYNC_ID = new Identifier(DontEatThat.MOD_ID, "nutrition_request_sync");

    private NutritionNetworking() {
    }

    public static void register() {
    }
}
