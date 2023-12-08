package io.github.btarg.origami.registry;

import io.github.btarg.origami.OrigamiMain;
import io.github.btarg.origami.definitions.CustomItemDefinition;
import io.github.btarg.origami.util.NamespacedKeyHelper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class RegistryHelper {

    public static CustomItemDefinition getDefinitionFromItemstack(ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta.isUnbreakable()) return null;
        String customItemString = meta.getPersistentDataContainer().get(NamespacedKeyHelper.customItemTag, PersistentDataType.STRING);
        if (customItemString == null || customItemString.isEmpty()) return null;
        return CustomItemRegistry.getRegisteredItem(customItemString);
    }

    public static ItemStack getAnyItemStack(String itemId, int count) {
        if (itemId.startsWith(OrigamiMain.PREFIX) && !itemId.contains("/")) {
            itemId = itemId.replace(OrigamiMain.PREFIX, "");
        }

        ItemStack stack = CustomBlockRegistry.getRegisteredBlock(itemId).createCustomItemStack(count);
        return (stack != null) ? stack : CustomItemRegistry.getRegisteredItem(itemId).createCustomItemStack(count);
    }
}
