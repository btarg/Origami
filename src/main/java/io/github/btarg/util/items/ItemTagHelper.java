package io.github.btarg.util.items;

import io.github.btarg.util.NamespacedKeyHelper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ItemTagHelper {

    public static boolean isCustomItem(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            PersistentDataContainer container = Objects.requireNonNull(itemStack.getItemMeta()).getPersistentDataContainer();
            return !container.isEmpty() && container.has(NamespacedKeyHelper.customItemTag, PersistentDataType.STRING);
        }
        return false;
    }

    public static boolean isCustomBlock(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            PersistentDataContainer container = Objects.requireNonNull(itemStack.getItemMeta()).getPersistentDataContainer();
            return !container.isEmpty() && container.has(NamespacedKeyHelper.customBlockItemTag, PersistentDataType.STRING);
        }
        return false;
    }

}
