package io.github.btarg.util.blocks;

import io.github.btarg.OrigamiMain;
import io.github.btarg.blockdata.CustomBlockPersistentData;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.util.NamespacedKeyHelper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;

public class CustomBlockUtils {

    public static CustomBlockDefinition getDefinitionFromBlock(Block block) {
        if (!CustomBlockPersistentData.blockIsInStorage(block.getLocation())) return null;
        Entity linkedItemDisplay = getLinkedItemDisplay(block.getLocation());
        if (linkedItemDisplay == null) return null;
        return getDefinitionFromItemDisplay(linkedItemDisplay);
    }

    public static CustomBlockDefinition getDefinitionFromItemDisplay(Entity itemDisplayEntity) {
        ItemStack item = ((ItemDisplay) itemDisplayEntity).getItemStack();
        String blockId = item.getItemMeta().getPersistentDataContainer().get(NamespacedKeyHelper.customBlockItemTag, PersistentDataType.STRING);

        if (blockId != null) {
            if (!blockId.startsWith(OrigamiMain.PREFIX)) {
                blockId = OrigamiMain.PREFIX + blockId;
            }
            return CustomBlockRegistry.GetRegisteredBlock(blockId);
        }

        return null;
    }


    public static Display getLinkedItemDisplay(Location location) {
        String check_uuid = CustomBlockPersistentData.getUUIDFromLocation(location);
        if (check_uuid != null && !check_uuid.isEmpty()) {

            for (Entity ent : location.getChunk().getEntities()) {
                if (ent.getUniqueId().toString().equals(check_uuid) && ent instanceof Display) {
                    return (Display) ent;
                }
            }
        }
        return null;
    }

    public static Location getDisplayLocationFromBlock(Location location) {
        Location entityLocation = location.clone();
        entityLocation.setX(location.getX() + 0.5);
        entityLocation.setY(location.getY() + 0.5002); // prevent z-fighting
        entityLocation.setZ(location.getZ() + 0.5);

        return entityLocation;
    }

    public static Transformation getDisplayTransformation(Display ent) {
        Transformation t = ent.getTransformation();
        return new Transformation(t.getTranslation(), t.getLeftRotation(), t.getScale().add(0.0002f, 0.0004f, 0.0002f), t.getRightRotation());
    }
}
