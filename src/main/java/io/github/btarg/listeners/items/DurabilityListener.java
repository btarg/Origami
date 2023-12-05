package io.github.btarg.listeners.items;

import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.registry.RegistryHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DurabilityListener implements Listener {
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        // Check for custom item
        ItemStack itemStack = event.getItem();
        CustomItemDefinition definition = RegistryHelper.getDefinitionFromItemstack(itemStack);
        if (definition == null) return;

        // Set custom durability
        short maxDurability = itemStack.getType().getMaxDurability();
        int damage = (int) Math.ceil((double) maxDurability / definition.durability);
        event.setDamage(damage);

        definition.executeEvent("onDamaged", event.getPlayer());
    }

    @EventHandler
    public void onItemBroken(PlayerItemBreakEvent event) {
        ItemStack itemStack = event.getBrokenItem();
        CustomItemDefinition definition = RegistryHelper.getDefinitionFromItemstack(itemStack);
        if (definition == null) return;
        definition.executeEvent("onBroken", event.getPlayer());
    }
}
