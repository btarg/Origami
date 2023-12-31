package io.github.btarg.origami.gui;

import io.github.btarg.origami.OrigamiMain;
import io.github.btarg.origami.registry.CustomBlockRegistry;
import io.github.btarg.origami.registry.CustomItemRegistry;
import io.github.btarg.origami.util.NamespacedKeyHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CreativeMenu implements Listener {

    private static final NamespacedKey pageChangeKey = new NamespacedKey(OrigamiMain.getInstance(), "pageChange");
    private int currentPage = 1;

    public void openCreativeMenu(Player player, String contentPack, int page) {
        if (contentPack == null) return;
        List<ItemStack> allCustomItemStacks = getAllCustomItemStacks(contentPack);
        int totalItems = allCustomItemStacks.size();

        int pageSize = Math.min(totalItems, 45); // Assuming 45 slots per page
        int startIndex = (page - 1) * pageSize;

        Inventory gui = Bukkit.createInventory(null, 54, Component.text(contentPack + " - Page " + page + "/" + totalPages(totalItems, pageSize)));

        for (int i = startIndex; i < Math.min(startIndex + pageSize, totalItems); i++) {
            gui.setItem(i - startIndex, allCustomItemStacks.get(i));
        }

        // previous page
        if (page > 1) gui.setItem(45, createNavigationButton(-1, contentPack));
        // close button
        gui.setItem(49, createButton(Material.BARRIER, Component.text("Close", TextColor.color(Color.RED.getRGB())), totalPages(totalItems, pageSize) + 1, contentPack));
        // next page
        if (page < totalPages(totalItems, pageSize)) {
            gui.setItem(53, createNavigationButton(1, contentPack));
        }

        currentPage = page;
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() == null) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null) {
                Player player = (Player) event.getWhoClicked();

                int pageChange = getButtonPageChange(clickedItem);
                String contentPack = getContentPackFromItem(clickedItem);
                int maxPages = totalPages(getAllCustomItemStacks(contentPack).size(), Math.min(getAllCustomItemStacks(contentPack).size(), 45));
                int newPage = currentPage + pageChange;

                if (pageChange != 0 && contentPack != null && newPage > 0 && newPage <= maxPages) {
                    openCreativeMenu(player, contentPack, newPage);
                } else if (clickedItem.getType() == Material.BARRIER && pageChange > maxPages) {
                    player.closeInventory();
                } else if (pageChange == 0) {
                    giveMaxStack(player, clickedItem.clone());
                }
            }
        }
    }

    private void giveMaxStack(Player player, ItemStack item) {
        ItemStack maxStack = item.clone();
        maxStack.setAmount(maxStack.getMaxStackSize());
        player.getInventory().addItem(maxStack);
        player.updateInventory();
    }

    private String getContentPackFromItem(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            return pdc.has(NamespacedKeyHelper.contentPackKey, PersistentDataType.STRING)
                    ? pdc.get(NamespacedKeyHelper.contentPackKey, PersistentDataType.STRING)
                    : null;
        }
        return null;
    }

    private int getButtonPageChange(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            return pdc.has(pageChangeKey, PersistentDataType.INTEGER)
                    ? pdc.get(pageChangeKey, PersistentDataType.INTEGER)
                    : 0;
        }
        return 0;
    }

    private ItemStack createNavigationButton(int pageChange, String contentPack) {
        return createButton(Material.ARROW, Component.text(pageChange > 0 ? "Next Page" : "Previous Page", TextColor.color(Color.WHITE.getRGB())), pageChange, contentPack);
    }

    private ItemStack createButton(Material material, Component buttonName, int pageChange, String contentPack) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(buttonName);
        meta.getPersistentDataContainer().set(pageChangeKey, PersistentDataType.INTEGER, pageChange);
        meta.getPersistentDataContainer().set(NamespacedKeyHelper.contentPackKey, PersistentDataType.STRING, contentPack);
        item.setItemMeta(meta);
        return item;
    }

    private List<ItemStack> getAllCustomItemStacks(String contentPack) {
        List<ItemStack> allCustomItemStacks = new ArrayList<>();
        CustomBlockRegistry.getBlockDefinitions(contentPack).values().forEach(blockDef -> allCustomItemStacks.add(blockDef.createCustomItemStack(1)));
        CustomItemRegistry.getItemDefinitions(contentPack).values().forEach(itemDef -> allCustomItemStacks.add(itemDef.createCustomItemStack(1)));
        return allCustomItemStacks;
    }

    private int totalPages(int totalItems, int pageSize) {
        return (int) Math.ceil((double) totalItems / pageSize);
    }
}
