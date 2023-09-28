package io.github.btarg.registry;

import io.github.btarg.definitions.CustomRecipeDefinition;
import io.github.btarg.util.items.ItemParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashMap;
import java.util.Map;

public class CustomRecipeRegistry {

    private static final Map<String, CustomRecipeDefinition> recipeDefinitions = new HashMap<>();

    public static void RegisterRecipe(CustomRecipeDefinition recipeDefinition) {
        recipeDefinitions.put(RegistryHelper.getRegistryPrefix() + recipeDefinition.namespacedKey.value(), recipeDefinition);

        boolean isShaped = !(recipeDefinition.shape == null || recipeDefinition.shape.isEmpty());

        try {

            ShapedRecipe shapedRecipe = new ShapedRecipe(recipeDefinition.namespacedKey, recipeDefinition.getResultItemStack());
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(recipeDefinition.namespacedKey, recipeDefinition.getResultItemStack());

            if (isShaped) {
                String[] shapeArray = recipeDefinition.shape.toArray(new String[0]);
                shapedRecipe.shape(shapeArray);
            }

            Material mat;
            ItemStack stack;

            // Parse ingredients from the key material map
            for (var entry : recipeDefinition.getIngredientMap().entrySet()) {
                mat = Material.matchMaterial(entry.getValue().toUpperCase());

                if (mat == null || mat.isEmpty()) {
                    stack = ItemParser.parseItemStack(entry.getValue());

                    if (isShaped) {
                        shapedRecipe.setIngredient(entry.getKey().charAt(0), new RecipeChoice.ExactChoice(stack));
                    } else {
                        shapelessRecipe.addIngredient(stack);
                    }

                } else {
                    if (isShaped) {
                        shapedRecipe.setIngredient(entry.getKey().charAt(0), mat);
                    } else {
                        shapelessRecipe.addIngredient(mat);
                    }
                }
            }

            if (isShaped) {
                Bukkit.addRecipe(shapedRecipe);
            } else {
                Bukkit.addRecipe(shapelessRecipe);
            }

            Bukkit.getLogger().info("Registered recipe: " + recipeDefinition.namespacedKey.value());
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

    }


    public static void ClearRecipeRegistry() {
        for (CustomRecipeDefinition recipe : recipeDefinitions.values()) {
            Bukkit.removeRecipe(recipe.namespacedKey);
        }
        recipeDefinitions.clear();
    }

}
