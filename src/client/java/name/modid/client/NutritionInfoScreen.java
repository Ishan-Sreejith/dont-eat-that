package name.modid.client;

import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import name.modid.nutrition.NutrientType;
import name.modid.nutrition.NutritionState;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class NutritionInfoScreen extends Screen {
    private final List<NutrientInfo> nutrientInfos = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxScroll = 0;

    public NutritionInfoScreen() {
        super(Text.literal("Nutrition Guide"));
        initInfo();
    }

    private void initInfo() {
        nutrientInfos.add(new NutrientInfo("VITAMIN A", 0xFFFFC33C,
                "Vision / Night Survival",
                "Sources: Carrots, Melon, Mango",
                "1.5+: Night vision",
                "3+: Stable night vision",
                "6+: Long lasting vision",
                "No slowness penalty"));

        nutrientInfos.add(new NutrientInfo("VITAMIN B6", 0xFF8B5CF6,
                "Combat Dodge",
                "Sources: Banana, Potato, Rabbit Stew",
                "1 icon: Dodge every 10s",
                "2 icons: Dodge every 5s",
                "3/5/7 icons: 4s / 2s / 1s",
                "Does not dodge fall damage"));

        nutrientInfos.add(new NutrientInfo("VITAMIN C", 0xFFFF8232,
                "Immunity + Healing",
                "Sources: Apple, Orange, Berries",
                "1.5+: Poison immunity",
                "1.5+: Passive regeneration",
                "5+: Stronger regeneration",
                "Raw meat safer with C"));

        nutrientInfos.add(new NutrientInfo("VITAMIN E", 0xFF4CAF7A,
                "Damage Reduction",
                "Sources: Beetroot, Seeds, Sunflower",
                "1 icon: 10% reduction",
                "2 icons: 20% reduction",
                "Up to 5 icons: 50% max",
                "Reduction applies on hit"));

        nutrientInfos.add(new NutrientInfo("VITAMIN K", 0xFF66BB6A,
                "Regeneration Speed",
                "Sources: Kelp, Sea Pickle, Dandelion",
                "1.5+: Regeneration",
                "4+: Stronger regeneration",
                "Pairs with vitamin C",
                "Sustains recovery"));

        nutrientInfos.add(new NutrientInfo("FIBER", 0xFF44FF44,
                "Digestion + Absorption",
                "Sources: Bread, Fiber Item, Cake",
                "Bread gives half icon",
                "1.5+: Faster digestion",
                "Higher fiber = better absorption",
                "No weakness or slowness"));

        nutrientInfos.add(new NutrientInfo("SPIKY", 0xFF2FA84F,
                "Thorns Body",
                "Sources: Cactus",
                "Cactus gives +3 spiky",
                "1.5+: Reflect melee damage",
                "More icons = stronger thorns",
                "High spiky gives extra resistance"));
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    protected void init() {
        super.init();
        recalcScrollBounds();
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_K) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (amount * 18)));
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xFF1A1A1A);
        
        int centerX = this.width / 2;
        int startY = 18 - scrollOffset;

        context.drawCenteredTextWithShadow(this.textRenderer, "NUTRITION SYSTEM GUIDE", centerX, startY, 0xFFFFFFFF);

        NutritionState state = NutritionClientState.getState();
        context.drawCenteredTextWithShadow(this.textRenderer, "Your Current Levels", centerX, startY + 16, 0xFFAAAAAA);

        int lineY = startY + 32;
        int leftX = centerX - 160;
        int rightX = centerX + 10;
        int totalTypes = NutrientType.values().length;
        int rowsPerColumn = (totalTypes + 1) / 2;
        int index = 0;
        for (NutrientType type : NutrientType.values()) {
            float value = state == null ? 0.0f : state.level(type);
            String label = labelFor(type);
            String status = value < 1.5f ? "LOW" : (value > 10 ? "EXCESS" : "OK");
            int color = value < 1.5f ? 0xFFFF4D4D : (value > 10 ? 0xFFFFB74D : 0xFF7CFF7C);
            int x = index < rowsPerColumn ? leftX : rightX;
            int y = lineY + (index % rowsPerColumn) * 12;
            context.drawTextWithShadow(this.textRenderer, label + ": " + String.format("%.1f", value) + " " + status, x, y, color);
            index++;
        }

        int infoStartY = startY + 32 + rowsPerColumn * 12 + 20;
        int cardWidth = 260;
        int cardHeight = 78;
        int cardX = centerX - (cardWidth / 2);
        int viewTop = 10;
        int viewBottom = this.height - 30;

        for (int i = 0; i < nutrientInfos.size(); i++) {
            NutrientInfo info = nutrientInfos.get(i);
            int y = infoStartY + i * (cardHeight + 8);
            if (y > viewBottom || y + cardHeight < viewTop) {
                continue;
            }

            context.fill(cardX - 2, y - 2, cardX + cardWidth + 2, y + cardHeight + 2, 0xFF0F0F0F);
            context.fill(cardX, y, cardX + cardWidth, y + cardHeight, 0xFF1C1C1C);

            context.drawTextWithShadow(this.textRenderer, info.name, cardX + 8, y + 6, info.color);
            context.drawTextWithShadow(this.textRenderer, info.role, cardX + 8, y + 18, 0xFFCCCCCC);
            context.drawTextWithShadow(this.textRenderer, info.sources, cardX + 8, y + 30, 0xFF888888);

            String[] buffs = info.getBuffs();
            int buffY = y + 42;
            for (int b = 0; b < buffs.length - 1; b++) {
                context.drawTextWithShadow(this.textRenderer, buffs[b], cardX + 8, buffY, 0xFF6FE56F);
                buffY += 10;
            }
            context.drawTextWithShadow(this.textRenderer, info.debuff, cardX + 8, y + cardHeight - 12, 0xFFFF7A7A);
        }

        // Quick legend
        int legendY = startY + 32 + rowsPerColumn * 12 + 6;
        context.fill(centerX - 160, legendY, centerX + 160, legendY + 18, 0xFF141414);
        context.drawTextWithShadow(this.textRenderer, "Legend: LOW = red • OK = green • EXCESS = orange", centerX - 150, legendY + 5, 0xFFBDBDBD);

        context.drawCenteredTextWithShadow(this.textRenderer, "Scroll to read • Press K or ESC to close", centerX, this.height - 20, 0xFF888888);

        super.render(context, mouseX, mouseY, delta);
    }

    private void recalcScrollBounds() {
        int totalTypes = NutrientType.values().length;
        int rowsPerColumn = (totalTypes + 1) / 2;
        int infoStartY = 18 + 32 + rowsPerColumn * 12 + 20;
        int cardHeight = 78;
        int contentBottom = infoStartY + nutrientInfos.size() * (cardHeight + 8) + 8;
        int visibleBottom = this.height - 30;
        maxScroll = Math.max(0, contentBottom - visibleBottom);
    }

    private static class NutrientInfo {
        String name;
        int color;
        String role;
        String sources;
        String[] buffs;
        String debuff;

        NutrientInfo(String name, int color, String role, String sources, String... buffs) {
            this.name = name;
            this.color = color;
            this.role = role;
            this.sources = sources;
            this.buffs = buffs;
            this.debuff = buffs[buffs.length - 1];
        }

        String[] getBuffs() {
            return buffs;
        }
    }

    private static String labelFor(NutrientType type) {
        return switch (type) {
            case VITAMIN_A -> "Vitamin A";
            case VITAMIN_B6 -> "Vitamin B6";
            case VITAMIN_C -> "Vitamin C";
            case VITAMIN_E -> "Vitamin E";
            case VITAMIN_K -> "Vitamin K";
            case FIBER -> "Fiber";
            case SPIKY -> "Spiky";
        };
    }
}
