package name.modid.nutrition;

public enum NutrientType {
    VITAMIN_A(0xFFC83D),
    VITAMIN_B6(0xFF8B5CF6),
    VITAMIN_C(0xFF8A33),
    VITAMIN_E(0xFF4CAF7A),
    VITAMIN_K(0xFF66BB6A),
    FIBER(0xFFC9A46A),
    SPIKY(0xFF2FA84F);

    private final int color;

    NutrientType(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }
}
