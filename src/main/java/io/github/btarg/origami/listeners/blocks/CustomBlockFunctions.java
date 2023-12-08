package io.github.btarg.origami.listeners.blocks;

import io.github.btarg.origami.definitions.CustomBlockDefinition;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomBlockFunctions {

    public static void dropBlockItems(Entity entity, CustomBlockDefinition definition, Block blockBroken) {
        if (definition == null) return;
        if (entity instanceof Player player) {
            World world = blockBroken.getWorld();
            boolean silkTouch = (player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH));

            if (silkTouch || definition.dropBlock()) {
                ItemStack blockItem = definition.createCustomItemStack(1);
                world.dropItemNaturally(blockBroken.getLocation(), blockItem);
            } else {

                for (ItemStack stack : definition.getDrops(player, blockBroken.getLocation())) {
                    world.dropItemNaturally(blockBroken.getLocation(), stack);
                }
            }
        }
    }
}
