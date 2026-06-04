package name.modid.client;

import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class NutritionMenu {
    public static KeyBinding OPEN_MENU;
    public static KeyBinding TOGGLE_HUD;

    public static void init() {
        OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dont-eat-that.menu",
                InputUtil.Type.KEYSYM,
                InputUtil.fromTranslationKey("key.keyboard.k").getCode(),
                "category.dont-eat-that"
        ));
        TOGGLE_HUD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dont-eat-that.toggle_hud",
                InputUtil.Type.KEYSYM,
                InputUtil.fromTranslationKey("key.keyboard.h").getCode(),
                "category.dont-eat-that"
        ));
    }

    public static void checkOpen(MinecraftClient client) {
        while (OPEN_MENU != null && OPEN_MENU.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new NutritionInfoScreen());
            }
        }
        while (TOGGLE_HUD != null && TOGGLE_HUD.wasPressed()) {
            NutritionClientState.toggleHud();
        }
    }
}
