package io.github.btarg.definitions;

import io.github.btarg.util.ComponentHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unchecked")
public class CustomDefinition implements ConfigurationSerializable {

    public String id;
    public String displayName;
    public List<String> lore;
    public List<String> rightClickCommands;
    public List<String> leftClickCommands;
    public String model;
    public Integer rightClickCooldownTicks;
    public Integer leftClickCooldownTicks;
    public Material baseMaterial;


    public CustomDefinition(Map<String, Object> map) {

        String baseMaterialString = (String) map.getOrDefault("baseMaterial", map.getOrDefault("baseBlock", map.get("baseItem")));
        this.baseMaterial = (baseMaterialString != null) ? Material.matchMaterial(baseMaterialString.trim().toUpperCase()) : null;

        this.model = (String) map.get("model"); // allow null models for default base material instead
        this.displayName = Objects.requireNonNullElse((String) map.get("displayName"), "Custom Item");
        this.rightClickCommands = Objects.requireNonNullElse((List<String>) map.get("rightClickCommands"), new ArrayList<>());
        this.leftClickCommands = Objects.requireNonNullElse((List<String>) map.get("leftClickCommands"), new ArrayList<>());
        this.lore = Objects.requireNonNullElse((List<String>) map.get("lore"), new ArrayList<>());
        this.leftClickCooldownTicks = Objects.requireNonNullElse((Integer) map.get("leftClickCooldown"), 0);
        this.rightClickCooldownTicks = Objects.requireNonNullElse((Integer) map.get("rightClickCooldown"), 0);
    }

    public Component getDisplayName() {
        Component nameComponent = ComponentHelper.deserializeGenericComponent(displayName);
        return ComponentHelper.removeItalicsIfAbsent(nameComponent);
    }

    public List<Component> getLore() {
        List<Component> toReturn = new ArrayList<>();
        lore.forEach(loreString -> {
            Component loreComponent = ComponentHelper.deserializeGenericComponent(loreString);
            toReturn.add(ComponentHelper.removeItalicsIfAbsent(loreComponent));
        });
        return toReturn;
    }

    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("baseMaterial", this.baseMaterial.toString());
        map.put("displayName", this.displayName);
        map.put("lore", this.lore);
        map.put("rightClickCommands", this.rightClickCommands);
        map.put("model", this.model);
        map.put("leftClickCooldown", this.leftClickCooldownTicks);
        map.put("rightClickCooldown", this.rightClickCooldownTicks);
        return map;
    }

}
