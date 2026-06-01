package ceobe.canteenclient.entity;

public  class IngredientMeta {
    public final IngredientType type;
    public final String name;

    public IngredientMeta(String name, IngredientType type) {
        this.name = name;
        this.type = type;
    }
}
