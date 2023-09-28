package io.github.btarg.events;

import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.RegistryHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CustomBlockFunctions {
    public static void OnCustomBlockBroken(Location location, String breakSound) {

        CustomBlockDatabase.removeBlockFromDatabase(location, true);

        if (breakSound != null && !breakSound.isEmpty())
            location.getWorld().playSound(location, Sound.valueOf(breakSound), 1, 1);
    }

    public static void OnCustomBlockClicked(PlayerInteractEvent event, CustomBlockDefinition definition) {

        if (event.getHand() == EquipmentSlot.OFF_HAND && event.getItem() == null) {
            return;
        }

        for (String command : definition.rightClickCommands) {
            if (command.startsWith("/")) {
                command = command.substring(1);
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute as " + event.getPlayer().getName() + " run " + command);
        }

    }

    public static void OnCustomBlockMined(BlockBreakEvent event, CustomBlockDefinition definition) {
        //event.getPlayer().sendMessage("Mined: " + blockName);
    }

    public static void DropBlockItems(Player player, CustomBlockDefinition definition, Block blockBroken) {
        if (definition == null) return;

        World world = blockBroken.getWorld();
        boolean silkTouch = (player != null && player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH));

        if (silkTouch || definition.dropBlock()) {
            ItemStack blockItem = RegistryHelper.CreateCustomBlockItemStack(definition, 1);
            world.dropItemNaturally(blockBroken.getLocation(), blockItem);
        } else {

            for (ItemStack stack : definition.getDrops(player)) {
                world.dropItemNaturally(blockBroken.getLocation(), stack);
            }

        }

    }
}
