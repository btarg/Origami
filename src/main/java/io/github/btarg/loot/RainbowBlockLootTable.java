package io.github.btarg.loot;

import io.github.btarg.PluginMain;
import io.github.btarg.registry.CustomBlockRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class RainbowBlockLootTable implements LootTable {

    private final PluginMain plugin = PluginMain.getPlugin(PluginMain.class);
    private final NamespacedKey key = new NamespacedKey(plugin, "rainbow_block_loot");
    private Collection<ItemStack> items = new ArrayList<>();

    @Override
    public Collection<ItemStack> populateLoot(Random random, LootContext lootContext) {
        items = new ArrayList<>();
        items.add(CustomBlockRegistry.CreateCustomBlockItemStack("rainbow_block", 1));
        return items;
    }

    @Override
    public void fillInventory(Inventory inventory, Random random, LootContext lootContext) {

    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
