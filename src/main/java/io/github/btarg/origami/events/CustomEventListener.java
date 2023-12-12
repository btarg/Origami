package io.github.btarg.origami.events;

import io.github.btarg.origami.OrigamiMain;
import io.github.btarg.origami.definitions.CustomBlockDefinition;
import io.github.btarg.origami.definitions.CustomItemDefinition;
import io.github.btarg.origami.registry.RegistryHelper;
import io.github.btarg.origami.util.blocks.CustomBlockUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class CustomEventListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        Block block = e.getClickedBlock();
        Player player = e.getPlayer();

        CustomBlockDefinition blockDefinition = CustomBlockUtils.getDefinitionFromBlock(block);
        boolean noBlock = blockDefinition == null;
        CustomItemDefinition itemDefinition = RegistryHelper.getDefinitionFromItemstack(e.getItem());
        boolean noItem = itemDefinition == null;
        if (noBlock && noItem) return;
        e.setCancelled(true);
        switch (e.getAction()) {
            case RIGHT_CLICK_AIR:
                if (!noItem) {
                    itemDefinition.executeEvent(EventNames.ON_RIGHT_CLICK.toString(), player);
                    itemDefinition.executeEvent(EventNames.ON_RIGHT_CLICK_AIR.toString(), player);
                }
                break;

            case RIGHT_CLICK_BLOCK:
                if (!noBlock) {
                    blockDefinition.executeEvent(EventNames.ON_RIGHT_CLICK.toString(), player);
                }

                if (!noItem) {
                    itemDefinition.executeEvent(EventNames.ON_RIGHT_CLICK_BLOCK.toString(), player);
                    itemDefinition.executeEvent(EventNames.ON_RIGHT_CLICK.toString(), player);
                }
                break;

            case LEFT_CLICK_AIR:
                if (!noItem) {
                    itemDefinition.executeEvent(EventNames.ON_LEFT_CLICK.toString(), player);
                    itemDefinition.executeEvent(EventNames.ON_LEFT_CLICK_AIR.toString(), player);
                }

                break;

            case LEFT_CLICK_BLOCK:
                if (!noBlock)
                    blockDefinition.executeEvent(EventNames.ON_LEFT_CLICK.toString(), player);
                if (!noItem) {
                    itemDefinition.executeEvent(EventNames.ON_LEFT_CLICK_BLOCK.toString(), player);
                    itemDefinition.executeEvent(EventNames.ON_LEFT_CLICK.toString(), player);
                }
                break;

            default:
                e.setCancelled(false);
                break;
        }
    }

    @EventHandler
    public void onCustomFoodEaten(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        CustomItemDefinition itemDefinition = RegistryHelper.getDefinitionFromItemstack(e.getItem());
        if (itemDefinition == null) return;
        e.setCancelled(true);
        itemDefinition.executeEvent(EventNames.ON_CONSUMED.toString(), player);
        //TODO: food saturation level
        for (PotionEffect effect : itemDefinition.potionEffects) {
            player.addPotionEffect(effect);
        }
        int amount = Math.max(e.getItem().getAmount() - 1, 0);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.getInventory().getItem(Objects.requireNonNull(e.getHand())).setAmount(amount);
                player.updateInventory();
            }
        }.runTaskLater(OrigamiMain.getInstance(), 1);
    }

}
