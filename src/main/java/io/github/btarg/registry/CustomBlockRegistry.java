package io.github.btarg.registry;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomBlockDefinition;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CustomBlockRegistry {

    private static final Map<String, CustomBlockDefinition> blockDefinitions = new HashMap<>();

    public static List<String> GetBlockIDs() {
        return new ArrayList<>(blockDefinitions.keySet());
    }

    public static void RegisterBlock(CustomBlockDefinition blockDefinition) {
        blockDefinitions.put(blockDefinition.id, blockDefinition);
        Bukkit.getLogger().info("Registered new block: " + blockDefinition.id);
    }

    public static CustomBlockDefinition GetRegisteredBlock(String blockId) {
        CustomBlockDefinition foundBlock = null;
        for (String bid : blockDefinitions.keySet()) {
            if (Objects.equals(bid, blockId)) {
                foundBlock = blockDefinitions.get(bid);
            }
        }
//        if (foundBlock != null)
//            Bukkit.getLogger().info("Found block: " + foundBlock.id);

        return foundBlock;
    }

    public static ItemStack CreateCustomBlockItemStack(String blockID, int count) {
        CustomBlockDefinition definition = GetRegisteredBlock(blockID);
        return CreateCustomBlockItemStack(definition, count);
    }

    public static void ClearBlockRegistry() {
        blockDefinitions.clear();
    }

    public static ItemStack CreateCustomBlockItemStack(CustomBlockDefinition customBlockDefinition, int count) {

        ItemStack frame = new ItemStack(Material.ITEM_FRAME, count);

        // new item meta (name and lore)
        ItemMeta meta = frame.getItemMeta();


        Component name = customBlockDefinition.getDisplayName();
        meta.displayName(name);

        meta.lore(customBlockDefinition.getLore());

        // add custom block id to the item so that we can tell it's a custom item
        meta.getPersistentDataContainer().set(OrigamiMain.customItemTag, PersistentDataType.STRING, customBlockDefinition.id);

        frame.setItemMeta(meta);

        // start editing nbt
        NBTItem nbtItem = new NBTItem(frame);

        nbtItem.setInteger("CustomModelData", customBlockDefinition.blockModelData);
        NBTCompound entityTag = nbtItem.addCompound("EntityTag");

        // item frame properties
        entityTag.setString("id", "minecraft:item_frame");
        entityTag.setBoolean("Facing", true);
        entityTag.setBoolean("Invulnerable", true);
        entityTag.setBoolean("Invisible", true);
        entityTag.setBoolean("Silent", true);
        entityTag.setBoolean("Fixed", true);

        // item within frame - shows block
        NBTCompound itemTag = entityTag.addCompound("Item");
        itemTag.setString("id", "minecraft:item_frame");
        itemTag.setInteger("Count", 1);
        NBTCompound itemTag2 = itemTag.addCompound("tag");
        itemTag2.setInteger("CustomModelData", customBlockDefinition.blockItemModelData);

        itemTag2.setString(OrigamiMain.customBlockIDKey, customBlockDefinition.id);

        return nbtItem.getItem();
    }
}
