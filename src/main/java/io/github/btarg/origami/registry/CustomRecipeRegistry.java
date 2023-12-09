package io.github.btarg.origami.registry;

import io.github.btarg.origami.definitions.CustomRecipeDefinition;
import io.github.btarg.origami.util.NamespacedKeyHelper;
import io.github.btarg.origami.util.datatypes.CustomRecipeType;
import io.github.btarg.origami.util.parsers.ItemParser;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;

public class CustomRecipeRegistry {

    private static final List<Recipe> registeredRecipes = new ArrayList<>();

    public static void registerRecipe(CustomRecipeDefinition recipeDefinition) {
        if (recipeDefinition.getResultItemStack() == null) return;

        String recipeId = recipeDefinition.namespacedKey.value();
        boolean isShaped = !(recipeDefinition.shape == null || recipeDefinition.shape.isEmpty());

        try {
            CustomRecipeType recipeType = recipeDefinition.getRecipeType();
            if (!recipeType.equals(CustomRecipeType.CRAFTING)) {

                if (recipeType.equals(CustomRecipeType.SMITHING)) {
                    if (isShaped) {
                        Bukkit.getLogger().warning("Smithing recipes cannot be shaped!");
                        return;
                    }
                    registerSmithingRecipe(recipeDefinition);

                } else {

                    if (isShaped) {
                        Bukkit.getLogger().warning("Cooking/Stonecutting recipes cannot be shaped!");
                        return;
                    }

                    List<Recipe> recipeList = switch (recipeType) {
                        case STONECUTTING -> registerStonecuttingRecipe(recipeDefinition);
                        default -> registerCookingRecipe(recipeDefinition);
                    };

                    if (!recipeList.isEmpty()) {
                        recipeList.forEach(r -> {
                            addRecipe(r);
                            Bukkit.getLogger().info("Registered recipe: " + recipeId);
                        });
                    }
                }
            } else {
                Recipe recipe = registerCraftingRecipe(recipeDefinition, isShaped);
                if (recipe != null) {
                    addRecipe(recipe);
                    Bukkit.getLogger().info("Registered crafting recipe: " + recipeId);
                }
            }

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    private static void registerSmithingRecipe(CustomRecipeDefinition recipeDefinition) {
        SmithingRecipe recipe = createSmithingRecipe(recipeDefinition);
        if (recipe != null) {
            addRecipe(recipe);
            Bukkit.getLogger().info("Registered Smithing recipe: " + recipeDefinition.namespacedKey.value());
        }
    }

    private static SmithingRecipe createSmithingRecipe(CustomRecipeDefinition recipeDefinition) {
        String[] ingredients = recipeDefinition.getIngredientMap().values().toArray(new String[0]);
        if (ingredients.length != 3) {
            Bukkit.getLogger().warning("Error registering recipe " + recipeDefinition.namespacedKey.value() + ": Smithing recipes must have 3 total ingredients!");
            return null;
        }
        RecipeChoice templateChoice = getRecipeChoice(ingredients[0]);
        RecipeChoice baseChoice = getRecipeChoice(ingredients[1]);
        RecipeChoice additionChoice = getRecipeChoice(ingredients[2]);

        return new SmithingTransformRecipe(recipeDefinition.namespacedKey, recipeDefinition.getResultItemStack(), templateChoice, baseChoice, additionChoice);
    }

    private static List<Recipe> registerStonecuttingRecipe(CustomRecipeDefinition recipeDefinition) {
        List<Recipe> output = new ArrayList<>();
        for (var entry : recipeDefinition.getIngredientMap().entrySet()) {
            RecipeChoice choice = getRecipeChoice(entry.getValue());
            recipeDefinition.getResultItemStacks().forEach(itemStack -> {
                NamespacedKey namespacedKey = NamespacedKeyHelper.getUniqueNamespacedKey(recipeDefinition.namespacedKey.value());
                output.add(new StonecuttingRecipe(namespacedKey, itemStack, choice));
            });
        }
        return output;
    }

    private static List<Recipe> registerCookingRecipe(CustomRecipeDefinition recipeDefinition) {
        List<Recipe> output = new ArrayList<>();
        for (var entry : recipeDefinition.getIngredientMap().entrySet()) {
            NamespacedKey namespacedKey = NamespacedKeyHelper.getUniqueNamespacedKey(recipeDefinition.namespacedKey.value());
            RecipeChoice choice = getRecipeChoice(entry.getValue());
            CookingRecipe<?> recipe = createCookingRecipe(recipeDefinition, namespacedKey, choice);
            if (recipe != null) {
                output.add(recipe);
            }
        }
        return output;
    }

    private static CookingRecipe<?> createCookingRecipe(CustomRecipeDefinition recipeDefinition, NamespacedKey namespacedKey, RecipeChoice choice) {
        switch (recipeDefinition.getRecipeType()) {
            case SMELTING -> {
                return new FurnaceRecipe(namespacedKey, recipeDefinition.getResultItemStack(), choice, recipeDefinition.experience, recipeDefinition.cookingTime);
            }
            case BLASTING -> {
                return new BlastingRecipe(namespacedKey, recipeDefinition.getResultItemStack(), choice, recipeDefinition.experience, recipeDefinition.cookingTime);
            }
            case SMOKING -> {
                return new SmokingRecipe(namespacedKey, recipeDefinition.getResultItemStack(), choice, recipeDefinition.experience, recipeDefinition.cookingTime);
            }
            case CAMPFIRE_COOKING -> {
                return new CampfireRecipe(namespacedKey, recipeDefinition.getResultItemStack(), choice, recipeDefinition.experience, recipeDefinition.cookingTime);
            }
            default -> {
                return null;
            }
        }
    }

    private static Recipe registerCraftingRecipe(CustomRecipeDefinition recipeDefinition, boolean isShaped) {
        Recipe recipe = isShaped ? createShapedRecipe(recipeDefinition) : createShapelessRecipe(recipeDefinition);
        addRecipe(recipe);
        Bukkit.getLogger().info("Registered crafting recipe: " + recipeDefinition.namespacedKey.value());
        return recipe;
    }

    private static ShapedRecipe createShapedRecipe(CustomRecipeDefinition recipeDefinition) {
        ShapedRecipe shapedRecipe = new ShapedRecipe(recipeDefinition.namespacedKey, recipeDefinition.getResultItemStack());
        String[] shapeArray = recipeDefinition.shape.toArray(new String[0]);
        shapedRecipe.shape(shapeArray);
        for (var entry : recipeDefinition.getIngredientMap().entrySet()) {
            RecipeChoice choice = getRecipeChoice(entry.getValue());
            shapedRecipe.setIngredient(entry.getKey().charAt(0), choice);
        }
        if (!recipeDefinition.group.isBlank()) {
            shapedRecipe.setGroup(recipeDefinition.group);
        }
        return shapedRecipe;
    }

    private static ShapelessRecipe createShapelessRecipe(CustomRecipeDefinition recipeDefinition) {
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(recipeDefinition.namespacedKey, recipeDefinition.getResultItemStack());
        for (var entry : recipeDefinition.getIngredientMap().entrySet()) {
            RecipeChoice choice = getRecipeChoice(entry.getValue());
            shapelessRecipe.addIngredient(choice);
        }
        if (!recipeDefinition.group.isBlank()) {
            shapelessRecipe.setGroup(recipeDefinition.group);
        }
        return shapelessRecipe;
    }

    private static void addRecipe(Recipe recipe) {
        if (!isRecipeRegistered(recipe)) {
            Bukkit.addRecipe(recipe, true);
            registeredRecipes.add(recipe);
        }
    }

    private static boolean isRecipeRegistered(Recipe recipe) {
        return registeredRecipes.stream().anyMatch(r -> r.getResult().equals(recipe.getResult()));
    }

    public static void clearRecipeRegistry() {
        registeredRecipes.stream()
                .filter(recipe -> recipe instanceof Keyed)
                .map(recipe -> (Keyed) recipe)
                .forEach(kr -> Bukkit.removeRecipe(kr.getKey()));

        registeredRecipes.clear();
    }

    private static RecipeChoice getRecipeChoice(String ingredientString) {
        Material mat = Material.matchMaterial(ingredientString.toUpperCase());
        if (mat == null || mat.isEmpty()) {
            ItemStack stack = ItemParser.parseItemStack(ingredientString);
            return new RecipeChoice.ExactChoice(stack);
        } else {
            return new RecipeChoice.MaterialChoice(mat);
        }
    }
}
