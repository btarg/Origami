package io.github.btarg.events;

import io.github.btarg.blockdata.CustomBlockPersistentData;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.RegistryHelper;
import io.github.btarg.util.items.CommandRunner;
import io.github.btarg.util.items.CooldownManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CustomBlockFunctions {
    public static void OnCustomBlockBroken(Location location, String breakSound) {

        CustomBlockPersistentData.removeBlockFromStorage(location);

        if (breakSound != null && !breakSound.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(breakSound);
                location.getWorld().playSound(location, Sound.valueOf(breakSound), 1, 1);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Block being broken does not have a valid sound!");
            }
        }
    }

    public static void OnCustomBlockClicked(PlayerInteractEvent event, CustomBlockDefinition definition) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        int cooldown = CooldownManager.getCooldown(event.getPlayer(), definition.id);
        float cooldownSeconds = ((float) cooldown / 20f);
        if (cooldown > 0) {
            event.getPlayer().sendMessage(Component.text("You cannot interact with this block for another " + cooldownSeconds + " seconds."));
            event.setCancelled(true);
            return;
        }
        CommandRunner.runCommands(definition.rightClickCommands, event.getPlayer().getUniqueId().toString());
    }

    public static void OnCustomBlockMined(BlockBreakEvent event, CustomBlockDefinition definition) {
        //event.getPlayer().sendMessage("Mined: " + blockName);
    }

    public static void DropBlockItems(Entity entity, CustomBlockDefinition definition, Block blockBroken) {
        if (definition == null) return;
        Player player = null;
        if (entity instanceof Player) {
            player = (Player) entity;
        }

        World world = blockBroken.getWorld();
        boolean silkTouch = (player != null && player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH));

        if (silkTouch || definition.dropBlock()) {
            ItemStack blockItem = RegistryHelper.CreateCustomBlockItemStack(definition, 1);
            world.dropItemNaturally(blockBroken.getLocation(), blockItem);
        } else {

            for (ItemStack stack : definition.getDrops(player, blockBroken.getLocation())) {
                world.dropItemNaturally(blockBroken.getLocation(), stack);
            }

        }

    }
}
