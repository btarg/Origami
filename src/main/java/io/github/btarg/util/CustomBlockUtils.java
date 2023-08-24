package io.github.btarg.util;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import io.github.btarg.OrigamiMain;
import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.util.items.ItemTagHelper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class CustomBlockUtils {

    public static CustomBlockDefinition getDefinitionFromBlock(Block block) {
        if (!CustomBlockDatabase.blockIsInDatabase(block.getLocation())) return null;
        Entity linkedFrame = GetLinkedItemFrame(block.getLocation());
        if (linkedFrame == null) return null;
        return getDefinitionFromItemFrame(linkedFrame);
    }

    public static CustomBlockDefinition getDefinitionFromItemFrame(Entity itemFrame) {
        NBTCompound nbtCompound = ItemTagHelper.getItemTagFromItemFrame(itemFrame);
        if (nbtCompound == null) return null;
        String blockId = nbtCompound.getString(OrigamiMain.customBlockIDKey);
        return CustomBlockRegistry.GetRegisteredBlock(blockId);
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
