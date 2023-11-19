package io.github.btarg.registry;

import io.github.btarg.definitions.CustomItemDefinition;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.*;

public class CustomItemRegistry {

    @Getter
    private static Map<String, CustomItemDefinition> itemDefinitions = new HashMap<>();

    public static List<String> GetItemIds() {
        return new ArrayList<>(itemDefinitions.keySet());
    }

    public static void RegisterItem(CustomItemDefinition itemDefinition) {
        itemDefinitions.put(RegistryHelper.getRegistryPrefix() + itemDefinition.id, itemDefinition);
        Bukkit.getLogger().info("Registered item: " + itemDefinition.id);
    }

    public static CustomItemDefinition GetRegisteredItem(String itemId) {
        if (itemId == null) return null;

        if (!itemId.startsWith(RegistryHelper.getRegistryPrefix())) {
            itemId = RegistryHelper.getRegistryPrefix() + itemId;
        }

        CustomItemDefinition foundItem = null;
        for (String registered : itemDefinitions.keySet()) {
            if (Objects.equals(registered, itemId)) {
                foundItem = itemDefinitions.get(registered);
            }
        }

        return foundItem;
    }

    public static void ClearItemRegistry() {
        itemDefinitions.clear();
    }

}
