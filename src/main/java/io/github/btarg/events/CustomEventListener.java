package io.github.btarg.events;

import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.registry.CustomItemRegistry;
import io.github.btarg.util.NamespacedKeyHelper;
import io.github.btarg.util.blocks.CustomBlockUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomEventListener implements Listener {

    @EventHandler
    public void onCustomItemUsed(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        ItemMeta meta = e.getItem().getItemMeta();
        String customItemString = meta.getPersistentDataContainer().get(NamespacedKeyHelper.customItemTag, PersistentDataType.STRING);
        if (customItemString == null || customItemString.isEmpty()) return;
        CustomItemDefinition definition = CustomItemRegistry.getRegisteredItem(customItemString);
        if (definition == null) return;

        switch (e.getAction()) {
            case RIGHT_CLICK_AIR:
                definition.executeEvent("onRightClick", player);
                definition.executeEvent("onRightClickAir", player);
                break;

            case RIGHT_CLICK_BLOCK:
                definition.executeEvent("onRightClick", player);
                definition.executeEvent("onRightClickBlock", player);
                break;

            case LEFT_CLICK_AIR:
                definition.executeEvent("onLeftClick", player);
                definition.executeEvent("onLeftClickAir", player);
                break;

            case LEFT_CLICK_BLOCK:
                definition.executeEvent("onLeftClick", player);
                definition.executeEvent("onLeftClickBlock", player);
                break;

            default:
                // Handle other cases if necessary
                break;
        }
    }

    @EventHandler
    public void onCustomBlockInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player player = e.getPlayer();
        if (block == null) return;

        CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromBlock(block);
        if (definition == null) return;

        switch (e.getAction()) {
            case RIGHT_CLICK_BLOCK:
                definition.executeEvent("onRightClick", player);
                break;

            case LEFT_CLICK_BLOCK:
                definition.executeEvent("onLeftClick", player);
                break;

            default:
                break;
        }
    }
}
