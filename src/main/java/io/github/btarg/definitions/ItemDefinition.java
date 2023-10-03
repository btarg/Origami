package io.github.btarg.definitions;

import io.github.btarg.util.ComponentHelper;
import net.kyori.adventure.text.Component;
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
    public Integer modelData;


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

    // Override me!
    public @NotNull Map<String, Object> serialize() {
        return new HashMap<>();
    }

}
