package name.modid.client;

import name.modid.nutrition.NutrientType;
import name.modid.nutrition.NutritionState;

import java.util.EnumMap;

public final class NutritionClientState {
    private static NutritionState currentState;
    private static boolean hudEnabled = true;
    private static NutrientType[] cachedPoints = new NutrientType[0];
    private static long flashUntilMs = 0L;

    private NutritionClientState() {
    }

    public static void update(NutritionState state) {
        currentState = state;
        if (state != null) {
            cachedPoints = state.getRecentPoints().toArray(new NutrientType[0]);
            flashUntilMs = System.currentTimeMillis() + 800L;
        } else {
            cachedPoints = new NutrientType[0];
        }
    }

    public static float level(NutrientType type) {
        if (currentState == null) return 0.0f;
        return currentState.level(type);
    }

    public static NutritionState getState() {
        return currentState;
    }

    public static NutrientType[] getCachedPoints() {
        return cachedPoints;
    }

    public static long getFlashUntilMs() {
        return flashUntilMs;
    }

    public static boolean isHudEnabled() {
        return hudEnabled;
    }

    public static void toggleHud() {
        hudEnabled = !hudEnabled;
    }
}
