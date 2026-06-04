package name.modid.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import name.modid.DontEatThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("dont-eat-that.json");

    public boolean hudEnabled = true;
    public boolean hudShowOnSneak = false;
    public int hudAnchor = 0;
    public float decayMultiplier = 1.0f;
    public boolean enableEffects = true;
    public boolean enableFiber = true;

    public static ModConfig load() {
        ModConfig config = new ModConfig();
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ModConfig loaded = GSON.fromJson(json, ModConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (Exception e) {
                DontEatThat.LOGGER.warn("Failed to load config, using defaults: {}", e.getMessage());
            }
        } else {
            save(config);
        }
        return config;
    }

    public static void save(ModConfig config) {
        try {
            String json = GSON.toJson(config);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            DontEatThat.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public enum Anchor {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}