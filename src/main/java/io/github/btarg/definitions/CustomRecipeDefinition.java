package io.github.btarg.definitions;

import io.github.btarg.util.items.ItemParser;
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
    @Getter
    private ItemStack resultItemStack;

    @SuppressWarnings("unchecked")
    public CustomRecipeDefinition(Map<String, Object> map) {

        this.shape = (List<String>) map.get("shape");
        this.ingredients = Objects.requireNonNullElse((List<String>) map.get("ingredients"), Collections.singletonList("A;AIR"));

        // Try to parse item stack from string, return a stack of 1 air if we can't
        ItemStack resultStack = ItemStack.empty();
        try {
            resultStack = ItemParser.parseItemStack(Objects.requireNonNullElse((String) map.get("result"), "AIR(1)"));
            if (resultStack == null) {
                resultStack = ItemStack.empty();
            }
        } catch (IllegalArgumentException ex) {
            Bukkit.getLogger().warning("A recipe has an empty result. Please ensure that every recipe has a result other than AIR.");
        }
        this.ingredientMap = new HashMap<>();
        for (int i = 0; i < ingredients.size(); i++) {
            String[] split = StringUtils.split(ingredients.get(i), ";");

            // d;DIAMOND
            if (split.length == 2) {
                // we can only have a string with a length of 1
                String key = String.valueOf(split[0].charAt(0));
                this.ingredientMap.put(key, split[1]);
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


    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("shape", this.shape);
        map.put("result", this.result);
        map.put("ingredients", this.ingredients);
        return map;
    }
}
