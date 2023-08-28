package io.github.btarg.util.items;

import io.github.btarg.OrigamiMain;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ItemTagHelper {

    public static boolean isCustomItem(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            PersistentDataContainer container = Objects.requireNonNull(itemStack.getItemMeta()).getPersistentDataContainer();
            return !container.isEmpty() && container.has(OrigamiMain.customItemTag, PersistentDataType.STRING);
        }
        return false;
    }

}
