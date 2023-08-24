package io.github.btarg.util.items;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import io.github.btarg.OrigamiMain;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

    public static NBTCompound getItemTagFromItemFrame(Entity entity) {

        if (entity.getType().equals(EntityType.ITEM_FRAME) || entity.getType().equals(EntityType.GLOW_ITEM_FRAME)) {
            NBTEntity nbtEntity = new NBTEntity(entity);
            return nbtEntity.getCompound("Item").getCompound("tag");
        }
        return null;

    }
}
