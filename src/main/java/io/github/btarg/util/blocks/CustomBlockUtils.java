package io.github.btarg.util.blocks;

import io.github.btarg.OrigamiMain;
import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.registry.RegistryHelper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CustomBlockUtils {

    public static CustomBlockDefinition getDefinitionFromBlock(Block block) {
        if (!CustomBlockDatabase.blockIsInDatabase(block.getLocation())) return null;
        Entity linkedFrame = GetLinkedItemFrame(block.getLocation());
        if (linkedFrame == null) return null;
        return getDefinitionFromItemFrame(linkedFrame);
    }

    public static CustomBlockDefinition getDefinitionFromItemFrame(Entity itemFrameEntity) {
        ItemStack item = ((ItemFrame) itemFrameEntity).getItem();

        String blockId = item.getItemMeta().getPersistentDataContainer().get(OrigamiMain.customItemTag, PersistentDataType.STRING);

        if (blockId != null) {
            if (!blockId.startsWith(RegistryHelper.getRegistryPrefix())) {
                blockId = RegistryHelper.getRegistryPrefix() + blockId;
            }
            return CustomBlockRegistry.GetRegisteredBlock(blockId);
        }


        return null;
    }


    public static Entity GetLinkedItemFrame(Location location) {
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
}
