package io.github.btarg.definitions;

import io.github.btarg.util.items.ItemParser;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("CustomRecipe")
public class CustomRecipeDefinition implements ConfigurationSerializable {

    public NamespacedKey namespacedKey;
    public boolean isShaped;
    public List<String> shape;
    public String result;
    public List<String> ingredients;

    @Getter
    private Map<Character, String> ingredientMap;

    @Getter
    private ItemStack resultItemStack;

    @SuppressWarnings("unchecked")
    public CustomRecipeDefinition(Map<String, Object> map) {
        this.shape = Objects.requireNonNullElse((List<String>) map.get("shape"), Arrays.asList("AAA", "AAA", "AAA"));
        this.isShaped = Objects.requireNonNullElse((Boolean) map.get("isShaped"), false);
        this.ingredients = Objects.requireNonNullElse((List<String>) map.get("ingredients"), Collections.singletonList("A;AIR"));

        // Try to parse item stack from string, return a stack of 1 air if we can't
        try {
            this.resultItemStack = ItemParser.parseItemStack(Objects.requireNonNullElse((String) map.get("result"), "AIR(1)"));
            if (this.resultItemStack == null) {
                this.resultItemStack = new ItemStack(Material.AIR, 1);
            }
        } catch (IllegalArgumentException ex) {
            this.resultItemStack = new ItemStack(Material.AIR, 1);
        }
        this.ingredientMap = new HashMap<>();
        for (String recipeKey : ingredients) {
            String[] split = StringUtils.split(recipeKey, ";");
            char key = split[0].charAt(0);
            String material = split[1];

            this.ingredientMap.put(key, material);

        }
        // we set this externally when loading from the file to be equal to the filename
        this.namespacedKey = null;
    }


    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("shape", this.shape);
        map.put("isShaped", this.isShaped);
        map.put("result", this.result);
        map.put("ingredients", this.ingredients);
        return map;
    }
}
