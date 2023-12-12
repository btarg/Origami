package io.github.btarg.origami.events;

import io.github.btarg.origami.definitions.CustomBlockDefinition;
import io.github.btarg.origami.definitions.CustomItemDefinition;
import io.github.btarg.origami.registry.RegistryHelper;
import io.github.btarg.origami.util.blocks.CustomBlockUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

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
                if (!noBlock)
                    blockDefinition.executeEvent(EventNames.ON_RIGHT_CLICK.toString(), player);

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
                // Handle other cases if necessary
                break;
        }
    }

}
