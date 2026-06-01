package ceobe.canteenclient.entity;

public enum IngredientType {
    VEGETABLE("蔬菜"),
    MEAT("肉类"),
    SEAFOOD("海鲜"),
    DAIRY("乳制品"),
    GRAIN("谷物"),
    FRUIT("水果"),
    OTHER("其他"),
    CUSTOM("自定义");

    private final String displayName;

    IngredientType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
