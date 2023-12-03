package io.github.btarg.listeners.items;

import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.registry.CustomItemRegistry;
import io.github.btarg.util.NamespacedKeyHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class DurabilityListener implements Listener {
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        // Check for custom item
        ItemStack itemStack = event.getItem();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta.isUnbreakable()) return;
        String customItemString = meta.getPersistentDataContainer().get(NamespacedKeyHelper.customItemTag, PersistentDataType.STRING);
        if (customItemString == null || customItemString.isEmpty()) return;
        CustomItemDefinition definition = CustomItemRegistry.getRegisteredItem(customItemString);
        if (definition == null) return;

        short maxDurability = itemStack.getType().getMaxDurability();
        int damage = (int) Math.ceil((double) maxDurability / definition.durability);
        event.setDamage(damage);
    }
}
