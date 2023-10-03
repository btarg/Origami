package io.github.btarg.registry;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.definitions.ItemDefinition;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class RegistryHelper {


    public static ItemStack CreateCustomBlockItemStack(ItemDefinition customBlockDefinition, int count) {

        if (customBlockDefinition == null) return null;

        ItemStack itemStack = new ItemStack(Material.ITEM_FRAME, count);

        // new item meta (name and lore)
        ItemMeta meta = itemStack.getItemMeta();
        Component name = customBlockDefinition.getDisplayName();
        meta.displayName(name);
        meta.lore(customBlockDefinition.getLore());
        meta.setCustomModelData(customBlockDefinition.modelData);

        meta.getPersistentDataContainer().set(OrigamiMain.customItemTag, PersistentDataType.STRING, customBlockDefinition.id);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public static ItemStack CreateCustomItemStack(CustomItemDefinition customItemDefinition, int count) {
        if (customItemDefinition == null) return null;

        ItemStack itemStack = new ItemStack(customItemDefinition.baseItem, count);

        // new item meta (name and lore)
        ItemMeta meta = itemStack.getItemMeta();
        Component name = customItemDefinition.getDisplayName();
        meta.displayName(name);
        meta.lore(customItemDefinition.getLore());
        meta.setCustomModelData(customItemDefinition.modelData);

        meta.getPersistentDataContainer().set(OrigamiMain.customItemTag, PersistentDataType.STRING, customItemDefinition.id);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public static ItemStack GetAnyItemStack(String itemId, int count) {
        itemId = itemId.substring(getRegistryPrefix().length());
        ItemStack stack = CreateCustomBlockItemStack(CustomBlockRegistry.GetRegisteredBlock(itemId), count);
        // not in the block registry, check item registry
        if (stack == null) {
            stack = CreateCustomItemStack(CustomItemRegistry.GetRegisteredItem(itemId), count);
        }
        return stack;
    }

    public static String getRegistryPrefix() {
        return "origami:";
    }

}
