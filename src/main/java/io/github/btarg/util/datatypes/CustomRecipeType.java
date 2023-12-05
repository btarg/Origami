package io.github.btarg.util.datatypes;

public enum CustomRecipeType {
    CRAFTING("CRAFTING"),
    SMELTING("SMELTING"),
    BLASTING("BLASTING"),
    SMITHING("SMITHING"),
    CAMPFIRE_COOKING("CAMPFIRE_COOKING"),
    SMOKING("SMOKING"),
    STONECUTTING("STONECUTTING");

    private final String recipeTypeString;

    CustomRecipeType(String _recipeTypeString) {
        this.recipeTypeString = _recipeTypeString;
    }

    @Override
    public String toString() {
        return recipeTypeString;
    }
}
