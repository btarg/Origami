package io.github.btarg.util.blocks;

import io.github.btarg.OrigamiMain;
import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.registry.RegistryHelper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CustomBlockUtils {

    public static CustomBlockDefinition getDefinitionFromBlock(Block block) {
        if (!CustomBlockDatabase.blockIsInDatabase(block.getLocation())) return null;
        Entity linkedFrame = getLinkedItemDisplay(block.getLocation());
        if (linkedFrame == null) return null;
        return getDefinitionFromItemDisplay(linkedFrame);
    }

    public static CustomBlockDefinition getDefinitionFromItemDisplay(Entity itemDisplayEntity) {
        ItemStack item = ((ItemDisplay) itemDisplayEntity).getItemStack();
        String blockId = item.getItemMeta().getPersistentDataContainer().get(OrigamiMain.customItemTag, PersistentDataType.STRING);

        if (blockId != null) {
            if (!blockId.startsWith(RegistryHelper.getRegistryPrefix())) {
                blockId = RegistryHelper.getRegistryPrefix() + blockId;
            }
            return CustomBlockRegistry.GetRegisteredBlock(blockId);
        }

        return null;
    }


    public static Entity getLinkedItemDisplay(Location location) {
        String check_uuid = CustomBlockDatabase.getBlockUUIDFromDatabase(location);
        if (check_uuid != null && !check_uuid.isEmpty()) {

            for (Entity ent : location.getWorld().getNearbyEntities(location, 1.0, 1.0, 1.0)) {
                if (ent.getUniqueId().toString().equals(check_uuid)) {
                    return ent;
                }
            }
        }
        return null;
    }

    public static Location getDisplayLocationFromBlock(Location location) {
        Location entityLocation = location.clone();
        entityLocation.setX(location.getX() + 0.5);
        entityLocation.setY(location.getY() + 0.5);
        entityLocation.setZ(location.getZ() + 0.5);

        return entityLocation;
    }
}
