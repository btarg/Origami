package io.github.btarg.registry;

import io.github.btarg.definitions.CustomBlockDefinition;
import org.bukkit.Bukkit;

import java.util.*;

public class CustomBlockRegistry {

    private static final Map<String, CustomBlockDefinition> blockDefinitions = new HashMap<>();

    public static List<String> GetBlockIDs() {
        return new ArrayList<>(blockDefinitions.keySet());
    }

    public static void RegisterBlock(CustomBlockDefinition blockDefinition) {
        blockDefinitions.put(RegistryHelper.getRegistryPrefix() + blockDefinition.id, blockDefinition);
        Bukkit.getLogger().info("Registered new block: " + blockDefinition.id);
    }

    public static CustomBlockDefinition GetRegisteredBlock(String blockId) {
        CustomBlockDefinition foundBlock = null;
        for (String bid : blockDefinitions.keySet()) {
            String withPrefix = RegistryHelper.getRegistryPrefix() + bid;
            if (Objects.equals(withPrefix, blockId)) {
                foundBlock = blockDefinitions.get(bid);
            }
        }

        return foundBlock;
    }

    public static void ClearBlockRegistry() {
        blockDefinitions.clear();
    }

}
