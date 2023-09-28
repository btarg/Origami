package io.github.btarg.util.loot;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface LootTableHelper {

    List<ItemStack> getBlockDrops(String dropLootTable, Location loc, ItemStack minedWith);

}
