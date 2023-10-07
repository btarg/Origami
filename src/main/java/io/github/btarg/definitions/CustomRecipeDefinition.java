package io.github.btarg.definitions;

import io.github.btarg.util.parsers.ItemParser;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("CustomRecipe")
public class CustomRecipeDefinition implements ConfigurationSerializable {

    @Getter
    private final Map<String, String> ingredientMap;
    public NamespacedKey namespacedKey;
    public List<String> shape;
    public String result;
    public List<String> ingredients;
    public String type;
    public Integer experience;
    public Integer cookingTime;
    @Getter
    private List<ItemStack> resultItemStacks;
    @Getter
    private ItemStack resultItemStack;
    @Getter
    private CustomRecipeType recipeType;

    @SuppressWarnings("unchecked")
    public CustomRecipeDefinition(Map<String, Object> map) {

        this.shape = (List<String>) map.get("shape");
        this.ingredients = Objects.requireNonNullElse((List<String>) map.get("ingredients"), Collections.singletonList("A;AIR"));
        this.type = Objects.requireNonNullElse((String) map.get("type"), "CRAFTING").toUpperCase();
        this.recipeType = Objects.requireNonNullElse(CustomRecipeType.valueOf(type), CustomRecipeType.CRAFTING);
        // only needed for furnace-like recipes
        this.experience = Objects.requireNonNullElse((Integer) map.get("experience"), 0);
        this.cookingTime = Objects.requireNonNullElse((Integer) map.get("cookingTime"), 20);

        this.resultItemStacks = new ArrayList<>();
        // Try to parse item stack from string, return a stack of 1 air if we can't
        this.resultItemStack = ItemStack.empty();
        String itemStackString = Objects.requireNonNullElse((String) map.get("result"), "AIR(1)");
        try {
            if (!itemStackString.contains(",")) {
                ItemStack result = itemStackFromString(itemStackString);
                this.resultItemStacks.add(result);
                this.resultItemStack = result;

            } else {
                String[] splitSections;
                splitSections = StringUtils.split(itemStackString, ",");
                for (String splitStackString : splitSections) {
                    this.resultItemStacks.add(itemStackFromString(splitStackString));
                }
                this.resultItemStack = this.resultItemStacks.get(0);
            }

        } catch (IllegalArgumentException ex) {
            Bukkit.getLogger().warning("A recipe has an empty result. Please ensure that every recipe has a result other than AIR.");
        }

        this.ingredientMap = new HashMap<>();
        for (int i = 0; i < ingredients.size(); i++) {
            String[] split = StringUtils.split(ingredients.get(i), ";");

            // d;DIAMOND
            if (split.length == 2) {
                String key = String.valueOf(split[0].charAt(0));
                // shapeless recipes with accidental keys - we try to get the value
                if (shape == null) {
                    this.ingredientMap.put(String.valueOf(i), split[1]);
                } else {
                    this.ingredientMap.put(key, split[1]);
                }

            }
            // DIAMOND
            else if (split.length == 1) {
                if (shape == null) {
                    // shapeless recipe
                    this.ingredientMap.put(String.valueOf(i), split[0]);
                } else {
                    Bukkit.getLogger().warning("Shaped Recipe has no keys! Each ingredient in a Shaped Recipe should be formatted as <key>;<item>");
                }
            }

        }
        // we set this externally when loading from the file to be equal to the filename
        this.namespacedKey = null;
    }

    private ItemStack itemStackFromString(String input) {
        ItemStack result;
        result = ItemParser.parseItemStack(input);
        if (result == null) {
            result = ItemStack.empty();
        }
        return result;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("shape", this.shape);
        map.put("result", this.result);
        map.put("ingredients", this.ingredients);
        map.put("type", this.type);
        map.put("experience", this.experience);
        map.put("cookingTime", this.cookingTime);
        return map;
    }
}
