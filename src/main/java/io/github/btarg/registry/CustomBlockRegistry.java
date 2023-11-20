package io.github.btarg.registry;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomBlockDefinition;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.*;

public class CustomBlockRegistry {

    @Getter
    private static Map<String, CustomBlockDefinition> blockDefinitions = new HashMap<>();

    public static List<String> GetBlockIDs() {
        return new ArrayList<>(blockDefinitions.keySet());
    }

    public static void RegisterBlock(CustomBlockDefinition blockDefinition) {
        blockDefinitions.put(OrigamiMain.PREFIX + blockDefinition.id, blockDefinition);
        Bukkit.getLogger().info("Registered block: " + blockDefinition.id);
    }

    public static CustomBlockDefinition GetRegisteredBlock(String blockId) {
        if (blockId == null) return null;

        if (!blockId.startsWith(OrigamiMain.PREFIX)) {
            blockId = OrigamiMain.PREFIX + blockId;
        }

        CustomBlockDefinition foundBlock = null;
        for (String registered : blockDefinitions.keySet()) {
            if (Objects.equals(registered, blockId)) {
                foundBlock = blockDefinitions.get(registered);
            }
        }

        return foundBlock;
    }

    public static void ClearBlockRegistry() {
        blockDefinitions.clear();
    }

}
