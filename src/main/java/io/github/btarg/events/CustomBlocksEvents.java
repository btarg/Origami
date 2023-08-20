package io.github.btarg.events;


import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.btarg.PluginMain;
import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.util.ToolLevelHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.Collection;
import java.util.Random;

public class CustomBlocksEvents implements Listener {

    @EventHandler
    public void ItemFramePlace(HangingPlaceEvent event) {
        Entity entity = event.getEntity();


        if (entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {

            NBTEntity nbtEntity = new NBTEntity(entity);
            String blockName = null;
            try {
                blockName = nbtEntity.getCompound("Item").getCompound("tag").getString(PluginMain.customBlockIDKey);
            } catch (NullPointerException e) {
                return;
            }

            // detect custom block placed
            if (blockName != null) {

                Block block = event.getBlock();
                World world = block.getWorld();

                java.util.Collection<Entity> entities = world.getNearbyEntities(entity.getLocation(), 0.5, 0.5, 0.5);
                if (!entities.isEmpty()) {
                    event.setCancelled(true);
                    return;
                }

                // set the item frame uuid to the same as the base block
                String block_uuid = entity.getUniqueId().toString();

                //TODO: replace with baseblock of choice
                world.setBlockData(entity.getLocation(), Material.SPAWNER.createBlockData());

                // Add the block to the database
                Location placedLocation = entity.getLocation();
                CustomBlockDatabase.addBlockToDatabase(placedLocation, block_uuid);
            }
        }
    }


    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem().getType() != null && e.getItem().getType() == Material.ITEM_FRAME) {
                return;
            }

            Entity linkedItemFrame = GetLinkedItemFrame(block);

            if (linkedItemFrame != null) {

                if (block.getType() == Material.SPAWNER && e.getPlayer().getInventory().getItemInMainHand().getType().name().endsWith("SPAWN_EGG")) {
                    e.setCancelled(true);
                    return;
                }


                NBTEntity nbtEntity = new NBTEntity(linkedItemFrame);
                NBTCompound compound = nbtEntity.getCompound("Item").getCompound("tag");
                if (compound.getBoolean("interactable")) {
                    OnCustomBlockClicked(e, compound.getString(PluginMain.customBlockIDKey));
                }
            }
        }
    }

    @EventHandler
    public void onCraftItemFrame(CraftItemEvent event) {
        if (event.getCurrentItem().getType() == Material.GLOW_ITEM_FRAME) {
            for (ItemStack itemStack : event.getInventory().getMatrix()) {
                if (itemStack != null) {

                    NBTItem nbtItem = new NBTItem(itemStack);
                    if (nbtItem.getCompound("tag") == null) {
                        return;
                    }

                    if (nbtItem.getCompound("tag").getString(PluginMain.customBlockIDKey) == null) {
                        event.setResult(Event.Result.DENY);
                        event.setCurrentItem(null);
                    }


                }
            }
        }
    }

    void DropBlockItems(String blockId, Block blockBroken) {
        World world = blockBroken.getWorld();
        CustomBlockDefinition definition = CustomBlockRegistry.GetRegisteredBlock(blockId);

        LootContext.Builder builder = new LootContext.Builder(blockBroken.getLocation());
        LootContext context = builder.build();

        Collection<ItemStack> stacks = definition.breakLootTable.populateLoot(new Random(), context);
        for (ItemStack stack : stacks) {
            world.dropItemNaturally(blockBroken.getLocation(), stack);
        }


    }

    @EventHandler
    public void onBlockBroken(BlockBreakEvent e) {

        Entity linkedFrame = GetLinkedItemFrame(e.getBlock());
        if (linkedFrame != null) {

            NBTEntity nbtEntity = new NBTEntity(linkedFrame);
            String blockId = nbtEntity.getCompound("Item").getCompound("tag").getString(PluginMain.customBlockIDKey);
            CustomBlockDefinition definition = CustomBlockRegistry.GetRegisteredBlock(blockId);

            if (ToolLevelHelper.GetToolLevel(e.getPlayer().getItemInUse()) >= definition.toolLevelRequired) {

                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    OnCustomBlockMined(e, blockId);
                    DropBlockItems(blockId, e.getBlock());
                }
            }


        } else {
            Bukkit.getLogger().warning("Could not get linked frame!");
        }
    }

    @EventHandler
    public void onBlockXP(BlockExpEvent e) {

        Entity linkedFrame = GetLinkedItemFrame(e.getBlock());
        if (linkedFrame == null) {
            return;
        }
        NBTEntity nbtEntity = new NBTEntity(linkedFrame);
        String blockId = nbtEntity.getCompound("Item").getCompound("tag").getString(PluginMain.customBlockIDKey);
        CustomBlockDefinition definition = CustomBlockRegistry.GetRegisteredBlock(blockId);

        e.setExpToDrop(definition.dropExperience);

        // Generic
        OnCustomBlockBroken(e.getBlock().getLocation());
        linkedFrame.remove();

    }

    @EventHandler
    public void onFrameBroken(HangingBreakEvent e) {

        // only allow destroying the frame rather than the base block if we explode it
        if (e.getCause().equals(HangingBreakEvent.RemoveCause.EXPLOSION)) {
            OnFrameRemovedGeneric(e.getEntity());
        } else {
            e.setCancelled(CustomBlockDatabase.blockIsInDatabase(e.getEntity().getLocation()));
        }


    }


    void OnFrameRemovedGeneric(Entity e) {

        if (e.getType() == EntityType.ITEM_FRAME || e.getType() == EntityType.GLOW_ITEM_FRAME) {

            // drop items
            NBTEntity nbtEntity = new NBTEntity(e);

            String blockId;
            try {
                blockId = nbtEntity.getCompound("Item").getCompound("tag").getString(PluginMain.customBlockIDKey);

            } catch (NullPointerException nullPointerException) {
                nullPointerException.printStackTrace();
                return;
            }

            if (blockId == null) {
                return;
            }

            World world = e.getWorld();
            Block baseBlock = world.getBlockAt(e.getLocation());

            // break base block
            baseBlock.breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
            DropBlockItems(blockId, baseBlock);

            // continue generic break events
            OnCustomBlockBroken(baseBlock.getLocation());


        }
    }

    void OnCustomBlockBroken(Location location) {
        CustomBlockDatabase.removeBlockFromDatabase(location, true);
    }

    public Entity GetLinkedItemFrame(Block block) {
        String check_uuid = CustomBlockDatabase.getBlockUUIDFromDatabase(block.getLocation());
        if (check_uuid != null && !check_uuid.isEmpty()) {

            for (Entity ent : block.getWorld().getNearbyEntities(block.getLocation(), 1.0, 1.0, 1.0)) {
                if (ent.getUniqueId().toString().equals(check_uuid)) {
                    return ent;
                }
            }
        }
        return null;
    }

    public void OnCustomBlockClicked(PlayerInteractEvent event, String blockName) {
        event.getPlayer().sendMessage("Clicked: " + blockName);
    }

    public void OnCustomBlockMined(BlockBreakEvent event, String blockName) {
        //event.getPlayer().sendMessage("Mined: " + blockName);
    }

}
