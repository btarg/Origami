package io.github.btarg.definitions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SerializableAs("CustomItem")
public class CustomItemDefinition extends ItemDefinition implements ConfigurationSerializable {

    @SuppressWarnings("unchecked")
    public CustomItemDefinition(Map<String, Object> map) {
        this.id = null;
        String baseBlockString = (String) map.get("baseItem");
        if (baseBlockString != null) {
            this.baseMaterial = Material.matchMaterial(baseBlockString.trim().toUpperCase());
        }
        if (this.baseMaterial == null || this.baseMaterial.isBlock() && !this.baseMaterial.equals(Material.COMMAND_BLOCK)) {
            Bukkit.getLogger().severe("Custom Items cannot be based on a regular block! Defaulting to a Command Block...");
            this.baseMaterial = Material.COMMAND_BLOCK;
        }

        this.modelData = Objects.requireNonNullElse((Integer) map.get("modelData"), 0);
        this.displayName = Objects.requireNonNullElse((String) map.get("displayName"), "Custom Item");

        this.rightClickCommands = Objects.requireNonNullElse((List<String>) map.get("rightClickCommands"), new ArrayList<>());
        this.lore = Objects.requireNonNullElse((List<String>) map.get("lore"), new ArrayList<>());

        this.interactionCooldownTicks = Objects.requireNonNullElse((Integer) map.get("cooldownTicks"), 0);

    }

    public static CustomItemDefinition deserialize(Map<String, Object> map) {
        return new CustomItemDefinition(map);
    }

}
