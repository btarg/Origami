package io.github.btarg.registry;

import io.github.btarg.definitions.CustomRecipeDefinition;
import io.github.btarg.definitions.CustomRecipeType;
import io.github.btarg.util.NamespacedKeyHelper;
import io.github.btarg.util.items.ItemParser;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomRecipeRegistry {

    private static final List<Recipe> registeredRecipes = new ArrayList<>();

    public static void RegisterRecipe(CustomRecipeDefinition recipeDefinition) {
        if (recipeDefinition.getResultItemStack() == null) return;

        String recipeId = recipeDefinition.namespacedKey.value();

        boolean isShaped = !(recipeDefinition.shape == null || recipeDefinition.shape.isEmpty());

        try {
            if (!recipeDefinition.getRecipeType().equals(CustomRecipeType.CRAFTING)) {

                if (recipeDefinition.getRecipeType().equals(CustomRecipeType.SMITHING)) {
                    if (isShaped) {
                        Bukkit.getLogger().warning("Smithing recipes cannot be shaped!");
                        return;
                    }
                    Recipe recipe = RegisterSmithingRecipe(recipeDefinition);
                    if (recipe == null) return;
                    addRecipe(recipe);
                    Bukkit.getLogger().info("Registered Smithing recipe: " + recipeId);


                } else {

                    if (isShaped) {
                        Bukkit.getLogger().warning("Cooking/Stonecutting recipes cannot be shaped!");
                        return;
                    }

                    List<Recipe> recipeList;
                    if (Objects.equals(recipeDefinition.getRecipeType().toString(), CustomRecipeType.STONECUTTING.toString())) {
                        recipeList = RegisterStonecuttingRecipe(recipeDefinition);
                    } else {
                        // is cooking recipe
                        recipeList = RegisterCookingRecipe(recipeDefinition);
                    }

                    if (recipeList.isEmpty()) return;
                    for (Recipe recipe : recipeList) {
                        if (recipe == null) continue;
                        addRecipe(recipe);
                        Bukkit.getLogger().info("Registered recipe: " + recipeId);

                    }
                }
            } else {

                Recipe recipe = RegisterCraftingRecipe(recipeDefinition, isShaped);
                if (recipe == null) return;
                addRecipe(recipe);
                Bukkit.getLogger().info("Registered crafting recipe: " + recipeId);

            }

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

    }

    private static Recipe RegisterSmithingRecipe(CustomRecipeDefinition recipeDefinition) {
        SmithingRecipe recipe = null;

        RecipeChoice baseChoice;
        RecipeChoice additionChoice;
        RecipeChoice templateChoice;

        // Parse ingredients from the key material map
        // only 3 ingredients allowed for smithing
        String[] ingredients = recipeDefinition.getIngredientMap().values().toArray(new String[0]);
        if (ingredients.length != 3) {
            Bukkit.getLogger().warning("Error registering recipe " + recipeDefinition.namespacedKey.value() + ": Smithing recipes must have 3 total ingredients!");
            return null;
        }
        templateChoice = GetRecipeChoice(ingredients[0]);
        baseChoice = GetRecipeChoice(ingredients[1]);
        additionChoice = GetRecipeChoice(ingredients[2]);

        if (templateChoice == null || baseChoice == null || additionChoice == null) return null;

        recipe = new SmithingTransformRecipe(recipeDefinition.namespacedKey, recipeDefinition.getResultItemStack(), templateChoice, baseChoice, additionChoice);

        return recipe;
    }

    private static RecipeChoice GetRecipeChoice(String ingredientString) {
        Material mat;
        ItemStack stack;
        RecipeChoice choice;
        mat = Material.matchMaterial(ingredientString.toUpperCase());

        if (mat == null || mat.isEmpty()) {
            stack = ItemParser.parseItemStack(ingredientString);
            choice = new RecipeChoice.ExactChoice(stack);

        } else {
            choice = new RecipeChoice.MaterialChoice(mat);
        }
        return choice;
    }

    private static List<Recipe> RegisterStonecuttingRecipe(CustomRecipeDefinition recipeDefinition) {
        List<Recipe> output = new ArrayList<>();
        // Parse ingredients from the key material map
        // here each possible ingredient is treated as a new recipe because there can only be 1 ingredient per smelting recipe
        for (var entry : recipeDefinition.getIngredientMap().entrySet()) {

            RecipeChoice choice = GetRecipeChoice(entry.getValue());

            // For stonecutting recipes, we can have multiple outputs, create a new recipe for each output
            // We also generate the namespacedkey here
            recipeDefinition.getResultItemStacks().forEach(itemStack -> {
                NamespacedKey namespacedKey = NamespacedKeyHelper.getUniqueNamespacedKey(recipeDefinition.namespacedKey.value());
                Recipe recipe = new StonecuttingRecipe(namespacedKey, itemStack, choice);
                output.add(recipe);
            });

        }
        return output;
    }

    private static List<Recipe> RegisterCookingRecipe(CustomRecipeDefinition recipeDefinition) {
        CookingRecipe<?> recipe = null;
        List<Recipe> output = new ArrayList<>();
        // Parse ingredients from the key material map
        // here each possible ingredient is treated as a new recipe because there can only be 1 ingredient per smelting recipe
        for (var entry : recipeDefinition.getIngredientMap().entrySet()) {
            // Since we might add one recipe per ingredient, we need unique namespacedkeys for every one of them
            NamespacedKey namespacedKey = NamespacedKeyHelper.getUniqueNamespacedKey(recipeDefinition.namespacedKey.value());

            RecipeChoice choice = GetRecipeChoice(entry.getValue());

            switch (recipeDefinition.getRecipeType()) {
                case SMELTING ->
                        recipe = new FurnaceRecipe(namespacedKey, recipeDefinition.getResultItemStack(), choice, recipeDefinition.experience, recipeDefinition.cookingTime);
                case BLASTING ->
                        recipe = new BlastingRecipe(namespacedKey, recipeDefinition.getResultItemStack(), choice, recipeDefinition.experience, recipeDefinition.cookingTime);
                case SMOKING ->
                        recipe = new SmokingRecipe(namespacedKey, recipeDefinition.getResultItemStack(), choice, recipeDefinition.experience, recipeDefinition.cookingTime);
                case CAMPFIRE_COOKING ->
                        recipe = new CampfireRecipe(namespacedKey, recipeDefinition.getResultItemStack(), choice, recipeDefinition.experience, recipeDefinition.cookingTime);
            }

            output.add(recipe);
        }
        return output;
    }

    private static Recipe RegisterCraftingRecipe(CustomRecipeDefinition recipeDefinition, boolean isShaped) {
        ShapedRecipe shapedRecipe = new ShapedRecipe(recipeDefinition.namespacedKey, recipeDefinition.getResultItemStack());
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(recipeDefinition.namespacedKey, recipeDefinition.getResultItemStack());

        if (isShaped) {
            String[] shapeArray = recipeDefinition.shape.toArray(new String[0]);
            shapedRecipe.shape(shapeArray);
        }

        // Parse ingredients from the key material map
        for (var entry : recipeDefinition.getIngredientMap().entrySet()) {
            RecipeChoice choice = GetRecipeChoice(entry.getValue());
            if (isShaped) {
                shapedRecipe.setIngredient(entry.getKey().charAt(0), choice);
            } else {
                shapelessRecipe.addIngredient(choice);
            }
        }

        if (isShaped) return shapedRecipe;
        else return shapelessRecipe;
    }

    private static void addRecipe(Recipe recipe) {
        Bukkit.addRecipe(recipe, true);
        registeredRecipes.add(recipe);
    }

    public static void ClearRecipeRegistry() {
        for (Recipe recipe : registeredRecipes) {
            if (recipe instanceof Keyed kr) {
                Bukkit.removeRecipe(kr.getKey());
            }
        }
        registeredRecipes.clear();
    }

}
