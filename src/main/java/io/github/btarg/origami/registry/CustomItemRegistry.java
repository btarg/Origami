package io.github.btarg.origami.registry;

import io.github.btarg.origami.OrigamiMain;
import io.github.btarg.origami.definitions.CustomItemDefinition;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomItemRegistry {

    private static final Map<String, CustomItemDefinition> itemDefinitions = new HashMap<>();

    public static Map<String, CustomItemDefinition> getItemDefinitions(String contentPack) {
        Map<String, CustomItemDefinition> filteredItemDefinitions = new HashMap<>();

        itemDefinitions.forEach((key, customItemDefinition) -> {
            if (customItemDefinition != null && customItemDefinition.contentPack.equals(contentPack)) {
                filteredItemDefinitions.put(key, customItemDefinition);
            }
        });

        return filteredItemDefinitions;
    }

    public static List<String> getItemIds() {
        return new ArrayList<>(itemDefinitions.keySet());
    }

    public static void registerItem(CustomItemDefinition itemDefinition) {
        String itemId = OrigamiMain.PREFIX + "item/" + itemDefinition.id;
        itemDefinitions.put(itemId, itemDefinition);
        Bukkit.getLogger().info("Registered item: " + itemDefinition.id);
    }

    public static CustomItemDefinition getRegisteredItem(String itemId) {
        if (itemId == null) return null;

        if (!itemId.startsWith(OrigamiMain.PREFIX + "item/")) {
            itemId = OrigamiMain.PREFIX + "item/" + itemId;
        }

        return itemDefinitions.get(itemId);
    }

    public static void clearItemRegistry() {
        itemDefinitions.clear();
    }
}
