package io.github.btarg.origami.registry;

import io.github.btarg.origami.OrigamiMain;
import io.github.btarg.origami.definitions.CustomBlockDefinition;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBlockRegistry {

    private static final Map<String, CustomBlockDefinition> blockDefinitions = new HashMap<>();

    public static Map<String, CustomBlockDefinition> getBlockDefinitions(String contentPack) {
        Map<String, CustomBlockDefinition> filteredBlockDefinitions = new HashMap<>();

        blockDefinitions.forEach((key, customBlockDefinition) -> {
            if (customBlockDefinition.contentPack != null && customBlockDefinition.contentPack.equals(contentPack)) {
                filteredBlockDefinitions.put(key, customBlockDefinition);
            }
        });

        return filteredBlockDefinitions;
    }

    public static List<String> getBlockIds() {
        return new ArrayList<>(blockDefinitions.keySet());
    }

    public static void registerBlock(CustomBlockDefinition itemDefinition) {
        String itemId = OrigamiMain.PREFIX + "block/" + itemDefinition.id;
        blockDefinitions.put(itemId, itemDefinition);
        Bukkit.getLogger().info("Registered block: " + itemDefinition.id);
    }

    public static CustomBlockDefinition getRegisteredBlock(String itemId) {
        if (itemId == null) return null;

        if (!itemId.startsWith(OrigamiMain.PREFIX + "block/")) {
            itemId = OrigamiMain.PREFIX + "block/" + itemId;
        }

        return blockDefinitions.get(itemId);
    }

    public static void clearBlockRegistry() {
        blockDefinitions.clear();
    }
}
