package io.github.btarg.origami.gui;

import io.github.btarg.origami.OrigamiMain;
import io.github.btarg.origami.registry.CustomBlockRegistry;
import io.github.btarg.origami.registry.CustomItemRegistry;
import io.github.btarg.origami.util.NamespacedKeyHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
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
        List<ItemStack> allCustomItemStacks = getAllCustomItemStacks(contentPack);
        int totalItems = allCustomItemStacks.size();

        int pageSize = Math.min(totalItems, 45); // Assuming 45 slots per page
        int startIndex = (page - 1) * pageSize;

        Inventory gui = Bukkit.createInventory(null, 54, Component.text(contentPack + " - Page " + page + "/" + totalPages(totalItems, pageSize)));

        for (int i = startIndex; i < Math.min(startIndex + pageSize, totalItems); i++) {
            gui.setItem(i - startIndex, allCustomItemStacks.get(i));
        }

        if (page > 1) gui.setItem(45, createNavigationButton(-1, contentPack));
        gui.setItem(49, createCloseButton(totalPages(totalItems, pageSize) + 1));
        if (startIndex < totalItems) gui.setItem(53, createNavigationButton(1, contentPack));

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
                if (pageChange != 0) {
                    if (contentPack != null) {
                        int maxPages = totalPages(getAllCustomItemStacks(contentPack).size(), Math.min(getAllCustomItemStacks(contentPack).size(), 45));
                        if (Math.abs(pageChange) <= maxPages) {
                            openCreativeMenu(player, contentPack, currentPage + pageChange);
                        } else {
                            player.closeInventory();
                        }
                    }
                } else if (clickedItem.getType() == Material.BARRIER) {
                    int maxPages = totalPages(getAllCustomItemStacks(contentPack).size(), Math.min(getAllCustomItemStacks(contentPack).size(), 45));
                    if (pageChange > maxPages) {
                        player.closeInventory();
                    }
                } else {
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
            return (pdc.has(NamespacedKeyHelper.contentPackKey, PersistentDataType.STRING))
                    ? pdc.get(NamespacedKeyHelper.contentPackKey, PersistentDataType.STRING)
                    : null;
        }
        return null;
    }

    private int getButtonPageChange(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            return (pdc.has(pageChangeKey, PersistentDataType.INTEGER))
                    ? pdc.get(pageChangeKey, PersistentDataType.INTEGER)
                    : 0;
        }
        return 0;
    }

    private ItemStack createNavigationButton(int pageChange, String contentPack) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(pageChange > 0 ? "Next Page" : "Previous Page").style(Style.empty()));
        meta.getPersistentDataContainer().set(pageChangeKey, PersistentDataType.INTEGER, pageChange);
        meta.getPersistentDataContainer().set(NamespacedKeyHelper.contentPackKey, PersistentDataType.STRING, contentPack);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCloseButton(int pageChange) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Close").color(TextColor.color(Color.RED.getRGB())));
        meta.getPersistentDataContainer().set(pageChangeKey, PersistentDataType.INTEGER, pageChange);
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
