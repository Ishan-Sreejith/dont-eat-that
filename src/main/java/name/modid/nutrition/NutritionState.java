package name.modid.nutrition;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public final class NutritionState {
    private static final String NBT_ROOT = "DontEatThatNutrition";
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NutritionState.class);

    private final EnumMap<NutrientType, Float> nutrientLevels = new EnumMap<>(NutrientType.class);
    private final Deque<DigestionEntry> digestionQueue = new ArrayDeque<>();
    private static final int MAX_UNITS = 40;
    private final Deque<NutrientType> recentPoints = new ArrayDeque<>();
    private final EnumMap<NutrientType, Float> pointRemainder = new EnumMap<>(NutrientType.class);
    private int ticksUntilAbsorb;

    public NutritionState() {
        for (NutrientType type : NutrientType.values()) {
            nutrientLevels.put(type, 0.0f);
            pointRemainder.put(type, 0.0f);
        }
    }

    public float level(NutrientType type) {
        return nutrientLevels.getOrDefault(type, 0.0f);
    }

    public void addNow(NutrientType type, float amount) {
        if (amount <= 0) {
            return;
        }
        nutrientLevels.put(type, Math.min(NutritionConfig.MAX_NUTRIENT, level(type) + amount));
    }

    public void reduce(NutrientType type, float amount) {
        if (amount <= 0) {
            return;
        }
        nutrientLevels.put(type, Math.max(0.0f, level(type) - amount));
    }

    public boolean decay(NutrientType type, float amount) {
        float before = level(type);
        reduce(type, amount);
        return level(type) != before;
    }

    public boolean consumeLeftmost(float amount) {
        if (amount <= 0) {
            return false;
        }
        boolean changed = false;
        for (NutrientType type : NutrientType.values()) {
            float current = level(type);
            if (current <= 0) {
                continue;
            }
            float deduction = Math.min(current, amount);
            reduce(type, deduction);
            amount -= deduction;
            changed = true;
            if (amount <= 0) {
                break;
            }
        }
        return changed;
    }

    public void queueFood(EnumMap<NutrientType, Float> nutrients) {
        if (nutrients.isEmpty()) {
            return;
        }
        if (digestionQueue.size() >= NutritionConfig.MAX_QUEUE_SIZE) {
            digestionQueue.removeFirst();
        }
        digestionQueue.addLast(new DigestionEntry(nutrients));
    }

    public void addPoints(EnumMap<NutrientType, Float> nutrients) {
        for (Map.Entry<NutrientType, Float> entry : nutrients.entrySet()) {
            NutrientType type = entry.getKey();
            float value = entry.getValue() + pointRemainder.getOrDefault(type, 0.0f);
            int units = (int) (value * 2.0f); // 0.5 NP = 1 unit, 2 NP = 4 units
            float remainder = value - (units / 2.0f);
            pointRemainder.put(type, remainder);
            if (units <= 0) {
                continue;
            }
            for (int i = 0; i < units; i++) {
                recentPoints.addLast(type);
                if (recentPoints.size() > MAX_UNITS) {
                    recentPoints.removeFirst();
                }
            }
        }
    }

    public boolean popRecentPoint() {
        if (recentPoints.isEmpty()) {
            return false;
        }
        recentPoints.removeLast();
        LOGGER.debug("Removed a recent point (remaining: {})", recentPoints.size());
        return true;
    }

    public void clearRecentPoints() {
        recentPoints.clear();
        for (NutrientType type : NutrientType.values()) {
            pointRemainder.put(type, 0.0f);
        }
    }

    public Deque<NutrientType> getRecentPoints() {
        return new ArrayDeque<>(recentPoints);
    }

    public int getRecentPointsCount() {
        return recentPoints.size();
    }

    public boolean hasRecentPoints() {
        return !recentPoints.isEmpty();
    }

    public int spikyPointCount() {
        int count = 0;
        for (NutrientType type : recentPoints) {
            if (type == NutrientType.SPIKY) count++;
        }
        return count;
    }

    public boolean tickDigestion(float absorptionMultiplier) {
        if (ticksUntilAbsorb > 0) {
            ticksUntilAbsorb--;
            return false;
        }
        int interval = Math.max(6, Math.round(20.0f / Math.max(1.0f, absorptionMultiplier)));
        ticksUntilAbsorb = interval;

        DigestionEntry entry = digestionQueue.peekFirst();
        if (entry == null) {
            return false;
        }

        boolean changed = false;
        Iterator<Map.Entry<NutrientType, Float>> it = entry.remaining.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<NutrientType, Float> next = it.next();
            float amount = next.getValue();
            if (amount <= 0.0f) {
                continue;
            }
            float step = Math.min(0.20f * Math.max(1.0f, absorptionMultiplier), amount);
            addNow(next.getKey(), step);
            next.setValue(amount - step);
            changed = true;
        }

        if (entry.isDone()) {
            digestionQueue.removeFirst();
        }

        return changed;
    }

    public boolean consumeFiber() {
        addNow(NutrientType.FIBER, 0.5f);
        return true;
    }

    public float absorptionMultiplier() {
        float fiber = level(NutrientType.FIBER);
        if (fiber < 1.5f) {
            return 1.0f;
        }
        int icons = Math.max(1, (int) Math.floor(fiber));
        return Math.min(1.5f, 1.0f + icons * 0.10f);
    }

    public NbtCompound toNbt() {
        try {
            NbtCompound root = new NbtCompound();
            for (NutrientType type : NutrientType.values()) {
                root.putFloat(type.name(), level(type));
            }
            root.putInt("TicksUntilAbsorb", ticksUntilAbsorb);
            NbtList queueList = new NbtList();
            for (DigestionEntry entry : digestionQueue) {
                NbtCompound entryNbt = new NbtCompound();
                for (Map.Entry<NutrientType, Float> e : entry.remaining.entrySet()) {
                    entryNbt.putFloat(e.getKey().name(), e.getValue());
                }
                queueList.add(entryNbt);
            }
            root.put("DigestionQueue", queueList);
            NbtList pointsList = new NbtList();
            for (NutrientType type : recentPoints) {
                NbtCompound p = new NbtCompound();
                p.putString("type", type.name());
                pointsList.add(p);
            }
            root.put("RecentPoints", pointsList);
            for (NutrientType type : NutrientType.values()) {
                root.putFloat("Remainder_" + type.name(), pointRemainder.getOrDefault(type, 0.0f));
            }
            return root;
        } catch (Exception e) {
            LOGGER.warn("Failed to serialize NutritionState to NBT", e);
            return new NbtCompound();
        }
    }

    public static void writeToPlayerNbt(NbtCompound nbt, NutritionState state) {
        nbt.put(NBT_ROOT, state.toNbt());
    }

    public static NutritionState fromPlayerNbt(NbtCompound nbt) {
        if (nbt == null) {
            return new NutritionState();
        }
        try {
            NutritionState state = new NutritionState();
            if (!nbt.contains(NBT_ROOT)) {
                return state;
            }
            NbtCompound root = nbt.getCompound(NBT_ROOT);
            for (NutrientType type : NutrientType.values()) {
                state.nutrientLevels.put(type, Math.max(0.0f, Math.min(NutritionConfig.MAX_NUTRIENT, root.getFloat(type.name()))));
            }
            state.ticksUntilAbsorb = Math.max(0, root.getInt("TicksUntilAbsorb"));
            if (root.contains("DigestionQueue", NbtElement.LIST_TYPE)) {
                NbtList queueList = root.getList("DigestionQueue", NbtElement.COMPOUND_TYPE);
                for (NbtElement el : queueList) {
                    NbtCompound entryNbt = (NbtCompound) el;
                    EnumMap<NutrientType, Float> remaining = new EnumMap<>(NutrientType.class);
                    for (NutrientType type : NutrientType.values()) {
                        if (entryNbt.contains(type.name())) {
                            remaining.put(type, entryNbt.getFloat(type.name()));
                        }
                    }
                    if (!remaining.isEmpty()) {
                        state.digestionQueue.addLast(new DigestionEntry(remaining));
                    }
                }
            }
            if (root.contains("RecentPoints", NbtElement.LIST_TYPE)) {
                NbtList pointsList = root.getList("RecentPoints", NbtElement.COMPOUND_TYPE);
                for (NbtElement el : pointsList) {
                    NbtCompound entryNbt = (NbtCompound) el;
                    if (entryNbt.contains("type")) {
                        try {
                            NutrientType type = NutrientType.valueOf(entryNbt.getString("type"));
                            if (state.recentPoints.size() < MAX_UNITS) {
                                state.recentPoints.addLast(type);
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
            for (NutrientType type : NutrientType.values()) {
                String key = "Remainder_" + type.name();
                if (root.contains(key)) {
                    state.pointRemainder.put(type, Math.max(0.0f, root.getFloat(key)));
                }
            }
            return state;
        } catch (Exception e) {
            LOGGER.warn("Failed to deserialize NutritionState from NBT", e);
            return new NutritionState();
        }
    }

    public void writeToBuf(PacketByteBuf buf) {
        for (NutrientType type : NutrientType.values()) {
            buf.writeFloat(level(type));
        }
        buf.writeInt(recentPoints.size());
        for (NutrientType type : recentPoints) {
            buf.writeInt(type.ordinal());
        }
        for (NutrientType type : NutrientType.values()) {
            buf.writeFloat(pointRemainder.getOrDefault(type, 0.0f));
        }
    }

    public static NutritionState readFromBuf(PacketByteBuf buf) {
        NutritionState state = new NutritionState();
        for (NutrientType type : NutrientType.values()) {
            state.nutrientLevels.put(type, buf.readFloat());
        }
        int points = buf.readInt();
        int limit = Math.min(points, MAX_UNITS);
        for (int i = 0; i < limit; i++) {
            int ord = buf.readInt();
            if (ord >= 0 && ord < NutrientType.values().length) {
                state.recentPoints.addLast(NutrientType.values()[ord]);
            }
        }
        for (int i = limit; i < points; i++) {
            buf.readInt();
        }
        for (NutrientType type : NutrientType.values()) {
            state.pointRemainder.put(type, Math.max(0.0f, buf.readFloat()));
        }
        return state;
    }

    public NutritionState copy() {
        NutritionState copy = new NutritionState();
        copy.nutrientLevels.putAll(this.nutrientLevels);
        copy.ticksUntilAbsorb = this.ticksUntilAbsorb;
        copy.recentPoints.addAll(this.recentPoints);
        copy.pointRemainder.putAll(this.pointRemainder);
        return copy;
    }

    private static final class DigestionEntry {
        private final EnumMap<NutrientType, Float> remaining;

        private DigestionEntry(EnumMap<NutrientType, Float> nutrients) {
            this.remaining = new EnumMap<>(nutrients);
        }

        private boolean isDone() {
            for (float value : remaining.values()) {
                if (value > 0.01f) {
                    return false;
                }
            }
            return true;
        }
    }
}
