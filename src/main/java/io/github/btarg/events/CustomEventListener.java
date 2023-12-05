package io.github.btarg.events;

import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.registry.RegistryHelper;
import io.github.btarg.util.blocks.CustomBlockUtils;
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

        switch (e.getAction()) {
            case RIGHT_CLICK_AIR:
                if (!noItem) {
                    itemDefinition.executeEvent("onRightClick", player);
                    itemDefinition.executeEvent("onRightClickAir", player);
                }
                break;

            case RIGHT_CLICK_BLOCK:
                if (!noBlock)
                    blockDefinition.executeEvent("onRightClick", player);

                if (!noItem) {
                    itemDefinition.executeEvent("onRightClickBlock", player);
                    itemDefinition.executeEvent("onRightClick", player);
                }
                break;

            case LEFT_CLICK_AIR:
                if (!noItem) {
                    itemDefinition.executeEvent("onLeftClick", player);
                    itemDefinition.executeEvent("onLeftClickAir", player);
                }

                break;

            case LEFT_CLICK_BLOCK:
                if (!noBlock)
                    blockDefinition.executeEvent("onLeftClick", player);
                if (!noItem) {
                    itemDefinition.executeEvent("onLeftClickBlock", player);
                    itemDefinition.executeEvent("onLeftClick", player);
                }
                break;

            default:
                // Handle other cases if necessary
                break;
        }
    }

}
