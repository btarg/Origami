package io.github.btarg.definitions;

import io.github.btarg.util.ComponentHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CustomBlockDefinition implements ConfigurationSerializable {

    public String id;
    public Material baseBlock;
    public Boolean glowing;
    public Integer blockItemModelData;
    public Integer blockModelData;
    public String displayName;
    public Boolean dropBlock;
    public List<String> rightClickCommands;
    public List<String> lore;
    public Integer dropExperience;
    public Integer toolLevelRequired;
    public List<String> canBeMinedWith;
    public Double timeToBreak;
    public String breakSound;
    public String placeSound;

    @SuppressWarnings("unchecked")
    public CustomBlockDefinition(Map<String, Object> map) {
        this.id = null;
        this.baseBlock = Objects.requireNonNullElse(Material.valueOf((String) map.get("baseBlock")), Material.GLASS);
        if (!this.baseBlock.isBlock()) {
            this.baseBlock = Material.GLASS;
        }
        this.glowing = Objects.requireNonNullElse((Boolean) map.get("glowing"), false);

        this.blockItemModelData = Objects.requireNonNullElse((Integer) map.get("blockItemModelData"), 0);
        this.blockModelData = Objects.requireNonNullElse((Integer) map.get("blockModelData"), 0);
        this.displayName = Objects.requireNonNullElse((String) map.get("displayName"), "Custom Block");
        this.dropBlock = Objects.requireNonNullElse((Boolean) map.get("dropBlock"), true);
        this.rightClickCommands = Objects.requireNonNullElse((List<String>) map.get("rightClickCommands"), new ArrayList<>());
        this.lore = Objects.requireNonNullElse((List<String>) map.get("lore"), new ArrayList<>());
        this.dropExperience = Objects.requireNonNullElse((Integer) map.get("dropExperience"), 0);
        this.toolLevelRequired = Objects.requireNonNullElse((Integer) map.get("toolLevelRequired"), 0);
        this.canBeMinedWith = Objects.requireNonNullElse((List<String>) map.get("canBeMinedWith"), new ArrayList<>());
        this.timeToBreak = Objects.requireNonNullElse(Double.valueOf(map.get("timeToBreak").toString()), 5d); //wtf is this
        this.breakSound = (String) map.get("breakSound");
        this.placeSound = (String) map.get("placeSound");
    }

    public static CustomBlockDefinition deserialize(Map<String, Object> map) {
        return new CustomBlockDefinition(map);
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

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("baseBlock", this.baseBlock.name());
        map.put("glowing", this.glowing);
        map.put("blockModelData", this.blockModelData);
        map.put("blockItemModelData", this.blockItemModelData);
        map.put("displayName", this.displayName);
        map.put("dropBlock", this.dropBlock);
        map.put("rightClickCommands", this.rightClickCommands);
        map.put("lore", this.lore);
        map.put("dropExperience", this.dropExperience);
        map.put("toolLevelRequired", this.toolLevelRequired);
        map.put("canBeMinedWith", this.canBeMinedWith);
        map.put("timeToBreak", this.timeToBreak);
        map.put("breakSound", this.breakSound);
        map.put("placeSound", this.placeSound);

        return map;
    }

}
