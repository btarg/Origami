package io.github.btarg.listeners.items;

import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.registry.CustomItemRegistry;
import io.github.btarg.util.NamespacedKeyHelper;
import io.github.btarg.util.items.CommandRunner;
import io.github.btarg.util.items.CooldownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomItemListener implements Listener {

    private long messageLastSent = 0L;

    @EventHandler
    public void onItemRightClick(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            clickCommands(e, true);
        } else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            clickCommands(e, false);
        }

    }

    private void clickCommands(PlayerInteractEvent e, boolean rightClick) {
        ItemMeta meta = e.getItem().getItemMeta();
        String customItemString = meta.getPersistentDataContainer().get(NamespacedKeyHelper.customItemTag, PersistentDataType.STRING);
        if (customItemString == null || customItemString.isEmpty()) return;

        int cooldown;
        if (rightClick) {
            cooldown = CooldownManager.getCooldown(e.getPlayer(), customItemString + "_right");
        } else {
            cooldown = CooldownManager.getCooldown(e.getPlayer(), customItemString + "_left");
        }
        float cooldownSeconds = ((float) cooldown / 20f);
        if (cooldown > 0) {
            if (System.currentTimeMillis() - messageLastSent > 500L) {
                e.getPlayer().sendMessage(Component.text("You cannot do this again for another " + cooldownSeconds + " seconds.").style(Style.style(TextDecoration.ITALIC)));
                messageLastSent = System.currentTimeMillis();
            }
            return;
        }

        CustomItemDefinition definition = CustomItemRegistry.getRegisteredItem(customItemString);
        if (definition == null) return;

        if (rightClick) {
            CommandRunner.runCommands(definition.rightClickCommands, e.getPlayer().getUniqueId().toString());
            CooldownManager.addCooldown(e.getPlayer(), customItemString + "_right", definition.rightClickCooldownTicks);
        } else {
            CommandRunner.runCommands(definition.leftClickCommands, e.getPlayer().getUniqueId().toString());
            CooldownManager.addCooldown(e.getPlayer(), customItemString + "_left", definition.leftClickCooldownTicks);
        }

    }
}
