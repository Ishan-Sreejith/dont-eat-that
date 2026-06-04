package name.modid.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import name.modid.nutrition.NutrientType;
import name.modid.nutrition.NutritionState;

public final class NutritionHudRenderer {
    private static final int ICON_SIZE = 8;
    private static final int ICONS_COUNT = 10;
    private static final int ICON_SPACING = 1;
    private static final int BAR_WIDTH = ICONS_COUNT * (ICON_SIZE + ICON_SPACING) - ICON_SPACING;

    private static final int[] NUTRIENT_COLORS = {
        0xFFFFC33C,   // Vitamin A - Yellow
        0xFF8B5CF6,   // Vitamin B6 - Violet
        0xFFFF8232,   // Vitamin C - Orange
        0xFF4CAF7A,   // Vitamin E - Green
        0xFF66BB6A,   // Vitamin K - Lime Green
        0xFFC9A46A,   // Fiber - Wheat
        0xFF2FA84F,   // Spiky - Cactus Green
    };

    private static float[] animatedValues = new float[NutrientType.values().length];
    private static long lastUpdate = 0;

    private NutritionHudRenderer() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            NutritionMenu.checkOpen(MinecraftClient.getInstance());
            onHudRender(context);
        });
    }

    private static void onHudRender(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (!NutritionClientState.isHudEnabled()) {
            return;
        }
        if (player == null || client.options.hudHidden || player.isSpectator()) {
            return;
        }

        NutritionState state = NutritionClientState.getState();
        if (state == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastUpdate > 30) {
            for (NutrientType type : NutrientType.values()) {
                float target = MathHelper.clamp(state.level(type), 0.0f, 10.0f);
                float current = animatedValues[type.ordinal()];
                animatedValues[type.ordinal()] = current + (target - current) * 0.3f;
            }
            lastUpdate = now;
        }

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int startX = (screenWidth - BAR_WIDTH) / 2;
        int startY = screenHeight - 39 - ICON_SIZE - 6;

        boolean anyLow = false;
        boolean anyExcess = false;
        for (NutrientType type : NutrientType.values()) {
            float value = state.level(type);
            if (value < 1.5f) {
                anyLow = true;
            }
            if (value > 10.0f) {
                anyExcess = true;
            }
        }

        NutrientType[] pointArray = NutritionClientState.getCachedPoints();
        for (int i = 0; i < ICONS_COUNT; i++) {
            int iconX = startX + i * (ICON_SIZE + ICON_SPACING);
            int iconY = startY;

            int leftIndex = i * 2;
            int rightIndex = i * 2 + 1;

            if (leftIndex < pointArray.length && pointArray[leftIndex] != null) {
                int colorLeft = NUTRIENT_COLORS[pointArray[leftIndex].ordinal()];
                drawHalfCircle(context, iconX, iconY, ICON_SIZE, colorLeft, true);
            } else {
                drawCircleOutline(context, iconX, iconY, ICON_SIZE, 0xFF2A2A2A);
            }

            if (rightIndex < pointArray.length && pointArray[rightIndex] != null) {
                int colorRight = NUTRIENT_COLORS[pointArray[rightIndex].ordinal()];
                drawHalfCircle(context, iconX, iconY, ICON_SIZE, colorRight, false);
            }

            if (leftIndex < pointArray.length || rightIndex < pointArray.length) {
                drawCircleOutline(context, iconX, iconY, ICON_SIZE, 0xFF1C1C1C);
                drawCircleCap(context, iconX, iconY, ICON_SIZE, 0xFF0F0F0F);
            }
        }

        long flashUntil = NutritionClientState.getFlashUntilMs();
        if (System.currentTimeMillis() < flashUntil && (anyExcess || anyLow)) {
            int pulse = anyExcess ? 0x88 : 0x66;
            for (int i = 0; i < ICONS_COUNT; i++) {
                int iconX = startX + i * (ICON_SIZE + ICON_SPACING);
                drawCircleOutline(context, iconX, startY, ICON_SIZE, (pulse << 24) | 0xFFFFFF);
            }
        }
    }

    private static void drawHalfCircle(DrawContext context, int x, int y, int size, int color, boolean left) {
        int radius = size / 2;
        int centerX = x + radius;
        int centerY = y + radius;

        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    if (left && dx <= 0 || (!left && dx >= 0)) {
                        int shade = (dx * dx + dy * dy >= (radius - 1) * (radius - 1)) ? 200 : 255;
                        int r = ((color >> 16) & 0xFF) * shade / 255;
                        int g = ((color >> 8) & 0xFF) * shade / 255;
                        int b = (color & 0xFF) * shade / 255;
                        context.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, 0xFF000000 | (r << 16) | (g << 8) | b);
                    }
                }
            }
        }
    }

    private static void drawCircleCap(DrawContext context, int x, int y, int size, int color) {
        int radius = size / 2;
        int centerX = x + radius;
        int centerY = y + radius;

        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int dist = dx * dx + dy * dy;
                if (dist == radius * radius || dist == (radius - 1) * (radius - 1)) {
                    context.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, color);
                }
            }
        }
    }

    private static void drawCircleOutline(DrawContext context, int x, int y, int size, int color) {
        int radius = size / 2;
        int centerX = x + radius;
        int centerY = y + radius;

        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int dist = dx * dx + dy * dy;
                if (dist >= (radius - 1) * (radius - 1) && dist <= radius * radius) {
                    context.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, color);
                }
            }
        }
    }
}
