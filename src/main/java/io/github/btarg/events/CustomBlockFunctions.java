package io.github.btarg.events;

import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
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

    public static void OnCustomBlockMined(BlockBreakEvent event, CustomBlockDefinition definition, Boolean silkTouch) {
        //event.getPlayer().sendMessage("Mined: " + blockName);
    }

    static void DropBlockItems(CustomBlockDefinition definition, Block blockBroken, Boolean silkTouch) {
        if (definition == null) return;

        World world = blockBroken.getWorld();

        if (silkTouch || definition.dropBlock) {
            ItemStack blockItem = CustomBlockRegistry.CreateCustomBlockItemStack(definition, 1);
            world.dropItemNaturally(blockBroken.getLocation(), blockItem);
        } else {
            // TODO: drop loot
        }

    }
}
