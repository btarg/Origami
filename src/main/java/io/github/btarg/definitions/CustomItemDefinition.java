package io.github.btarg.definitions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("CustomItem")
public class CustomItemDefinition extends ItemDefinition implements ConfigurationSerializable {

    public Material baseItem;

    @SuppressWarnings("unchecked")
    public CustomItemDefinition(Map<String, Object> map) {
        this.id = null;
        String baseBlockString = (String) map.get("baseItem");
        if (baseBlockString != null) {
            this.baseItem = Material.matchMaterial(baseBlockString.trim().toUpperCase());
        }
        if (this.baseItem == null || this.baseItem.isBlock() && !this.baseItem.equals(Material.COMMAND_BLOCK)) {
            Bukkit.getLogger().severe("Custom Items cannot be based on a regular block! Defaulting to a Command Block...");
            this.baseItem = Material.COMMAND_BLOCK;
        }

        this.modelData = Objects.requireNonNullElse((Integer) map.get("modelData"), 0);
        this.displayName = Objects.requireNonNullElse((String) map.get("displayName"), "Custom Item");

        this.rightClickCommands = Objects.requireNonNullElse((List<String>) map.get("rightClickCommands"), new ArrayList<>());
        this.lore = Objects.requireNonNullElse((List<String>) map.get("lore"), new ArrayList<>());

    }

    public static CustomItemDefinition deserialize(Map<String, Object> map) {
        return new CustomItemDefinition(map);
    }


    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("baseItem", this.baseItem.name());
        map.put("modelData", this.modelData);
        map.put("displayName", this.displayName);
        map.put("rightClickCommands", this.rightClickCommands);
        map.put("lore", this.lore);

        return map;
    }

}
