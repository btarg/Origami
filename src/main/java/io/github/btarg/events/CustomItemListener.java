package io.github.btarg.events;

import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.registry.CustomItemRegistry;
import io.github.btarg.util.NamespacedKeyHelper;
import io.github.btarg.util.items.CommandRunner;
import io.github.btarg.util.items.CooldownManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomItemListener implements Listener {
    @EventHandler
    public void onItemRightClick(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        ItemMeta meta = e.getItem().getItemMeta();
        String customItemString = meta.getPersistentDataContainer().get(NamespacedKeyHelper.customItemTag, PersistentDataType.STRING);
        if (customItemString == null || customItemString.isEmpty()) return;

        int cooldown = CooldownManager.getCooldown(e.getPlayer(), customItemString);
        float cooldownSeconds = ((float) cooldown / 20f);
        if (cooldown > 0) {
            e.getPlayer().sendMessage(Component.text("You cannot use this item for another " + cooldownSeconds + " seconds."));
            return;
        }

        CustomItemDefinition definition = CustomItemRegistry.GetRegisteredItem(customItemString);
        if (definition == null) return;
        CommandRunner.runCommands(definition.rightClickCommands, e.getPlayer().getUniqueId().toString());
        CooldownManager.addCooldown(e.getPlayer(), customItemString, definition.interactionCooldownTicks);
    }
}
