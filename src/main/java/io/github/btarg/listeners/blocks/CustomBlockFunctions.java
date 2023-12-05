package io.github.btarg.listeners.blocks;

import io.github.btarg.blockdata.CustomBlockPersistentData;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.RegistryHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomBlockFunctions {

    public static void onCustomBlockBroken(Location location, String breakSound) {

        CustomBlockPersistentData.removeBlockFromStorage(location);

        if (breakSound != null && !breakSound.isEmpty()) {
            try {
                location.getWorld().playSound(location, Sound.valueOf(breakSound), 1, 1);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Block being broken does not have a valid sound!");
            }
        }
    }

    public static void dropBlockItems(Entity entity, CustomBlockDefinition definition, Block blockBroken) {
        if (definition == null) return;
        if (entity instanceof Player player) {
            World world = blockBroken.getWorld();
            boolean silkTouch = (player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH));

            if (silkTouch || definition.dropBlock()) {
                ItemStack blockItem = RegistryHelper.createCustomItemStack(definition, 1);
                world.dropItemNaturally(blockBroken.getLocation(), blockItem);
            } else {

                for (ItemStack stack : definition.getDrops(player, blockBroken.getLocation())) {
                    world.dropItemNaturally(blockBroken.getLocation(), stack);
                }
            }
        }
    }
}
