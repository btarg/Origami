package io.github.btarg.definitions;

import io.github.btarg.util.ComponentHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDefinition implements ConfigurationSerializable {

    public String id;
    public String displayName;
    public List<String> lore;
    public List<String> rightClickCommands;
    public List<String> leftClickCommands;
    public Integer modelData;
    public Integer rightClickCooldownTicks;
    public Integer leftClickCooldownTicks;
    public Material baseMaterial;


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
        map.put("baseMaterial", this.baseMaterial);
        map.put("displayName", this.displayName);
        map.put("lore", this.lore);
        map.put("rightClickCommands", this.rightClickCommands);
        map.put("modelData", this.modelData);
        map.put("leftClickCooldown", this.leftClickCooldownTicks);
        map.put("rightClickCooldown", this.rightClickCooldownTicks);
        return map;
    }

}
