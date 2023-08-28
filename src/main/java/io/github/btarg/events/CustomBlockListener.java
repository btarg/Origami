package io.github.btarg.events;


import io.github.btarg.OrigamiMain;
import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.rendering.BrokenBlock;
import io.github.btarg.rendering.BrokenBlocksService;
import io.github.btarg.util.blocks.CustomBlockUtils;
import io.github.btarg.util.items.ItemTagHelper;
import io.github.btarg.util.items.ToolLevelHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CustomBlockListener implements Listener {

    private final Set<Material> transparentBlocks;
    private final BrokenBlocksService brokenBlocksService;

    public CustomBlockListener() {
        brokenBlocksService = OrigamiMain.brokenBlocksService; // Get the BrokenBlocksService instance
        transparentBlocks = new HashSet<>();
        transparentBlocks.add(Material.WATER);
        transparentBlocks.add(Material.LAVA);
        transparentBlocks.add(Material.AIR);
    }

    @EventHandler
    public void ItemFramePlace(HangingPlaceEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() == EntityType.ITEM_FRAME) {

            ItemMeta meta = event.getItemStack().getItemMeta();
            if (meta == null) return;

            String blockName = meta.getPersistentDataContainer().get(OrigamiMain.customItemTag, PersistentDataType.STRING);

            CustomBlockDefinition definition = CustomBlockRegistry.GetRegisteredBlock(blockName);
            if (definition == null) return;

            Block block = event.getBlock();
            World world = block.getWorld();

            // set the item frame's item to be an item frame with a custom model
            ItemStack modelItem = new ItemStack(Material.ITEM_FRAME, 1);
            ItemMeta modelMeta = modelItem.getItemMeta();
            modelMeta.setCustomModelData(definition.blockModelData);
            modelMeta.getPersistentDataContainer().set(OrigamiMain.customItemTag, PersistentDataType.STRING, definition.id);
            modelItem.setItemMeta(modelMeta);

            // if this is supposed to be a glow item frame, then we create a new entity
            if (definition.glowing) {

                Location exLocation = entity.getLocation();
                entity.remove();
                entity = SpawnGlowItemFrame(exLocation);

            }

            ItemFrame frame = (ItemFrame) entity;
            frame.setFacingDirection(BlockFace.UP);
            frame.setVisible(false);
            frame.setInvulnerable(true);
            frame.setFixed(true);
            frame.setSilent(true);
            frame.setCustomNameVisible(false);
            frame.setItem(modelItem, false);


            // check for entities (other than item frames) that are too close
            java.util.Collection<Entity> entities = world.getNearbyEntities(entity.getLocation(), 0.5, 0.5, 0.5);
            entities.removeIf(ent -> ent instanceof ItemFrame);

            if (!entities.isEmpty()) {
                event.setCancelled(true);
                entity.remove();
                return;
            }

            // set the item frame uuid to the same as the base block
            String block_uuid = entity.getUniqueId().toString();

            // Add the block to the database
            Location placedLocation = entity.getLocation();

            // if we failed to add a block because there is one already there, then we should cancel the item frame place event
            boolean added = CustomBlockDatabase.addBlockToDatabase(placedLocation, block_uuid);
            if (!added) {
                event.setCancelled(true);
                entity.remove();
                return;
            }
            world.setBlockData(entity.getLocation(), definition.baseBlock.createBlockData());

            if (definition.placeSound != null) {
                world.playSound(block.getLocation(), Sound.valueOf(definition.placeSound), 1, 1);
            }
        }
    }

    private Entity SpawnGlowItemFrame(Location location) {
        return location.getWorld().spawnEntity(location, EntityType.GLOW_ITEM_FRAME, CreatureSpawnEvent.SpawnReason.COMMAND);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {

        Block block = e.getClickedBlock();

        if (block != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() != null && e.getItem().getType() == Material.ITEM_FRAME) {
                return;
            }

            CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromBlock(block);
            if (definition == null) return;

            if (block.getType() == Material.SPAWNER && e.getPlayer().getInventory().getItemInMainHand().getType().name().endsWith("SPAWN_EGG")) {
                e.setCancelled(true);
                return;
            }

            if (!definition.rightClickCommands.isEmpty()) {
                CustomBlockFunctions.OnCustomBlockClicked(e, definition);
            }
        }
    }

    @EventHandler
    public void onCraftItemFrame(CraftItemEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GLOW_ITEM_FRAME) {
            for (ItemStack itemStack : event.getInventory().getMatrix()) {
                if (itemStack != null) {

                    if (ItemTagHelper.isCustomItem(itemStack)) {
                        event.setResult(Event.Result.DENY);
                        event.setCurrentItem(null);
                    }

                }
            }
        }
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        Block block = player.getTargetBlock(transparentBlocks, 5);
        Location blockPosition = block.getLocation();

        if (!brokenBlocksService.isBrokenBlock(blockPosition)) return;

        double distanceX = blockPosition.getX() - player.getLocation().getBlockX();
        double distanceY = blockPosition.getY() - player.getLocation().getBlockY();
        double distanceZ = blockPosition.getZ() - player.getLocation().getBlockZ();

        if (distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ >= 1024.0D) return;

        BrokenBlock brokenBlock = brokenBlocksService.getBrokenBlock(blockPosition);
        if (brokenBlock == null) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 4, -1, false, false));
        ItemStack playerHand = player.getInventory().getItemInMainHand();

        CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromBlock(block);
        if (definition == null) return;

        if (ToolLevelHelper.checkItemTypeByString(playerHand, definition.canBeMinedWith)) {
            brokenBlock.incrementDamage(player, ToolLevelHelper.getToolLevel(playerHand, true));
        } else {
            brokenBlock.incrementDamage(player, 0.5);
        }


    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Block block = event.getBlock();
        CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromBlock(block);
        if (definition == null) return;
        brokenBlocksService.createBrokenBlock(block, (double) definition.timeToBreak);

    }

    @EventHandler
    public void onBlockDamageStop(BlockDamageAbortEvent event) {
        brokenBlocksService.removeBrokenBlock(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockBroken(BlockBreakEvent e) {

        Entity linkedFrame = CustomBlockUtils.GetLinkedItemFrame(e.getBlock().getLocation());
        if (linkedFrame == null) return;
        CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromItemFrame(linkedFrame);
        if (definition == null) return;
        e.setDropItems(false);

        ItemStack itemUsed = e.getPlayer().getInventory().getItemInMainHand();

        if (ToolLevelHelper.getToolLevel(itemUsed, false) >= definition.toolLevelRequired && ToolLevelHelper.checkItemTypeByString(itemUsed, definition.canBeMinedWith)) {

            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {

                boolean silkTouch = false;
                if (itemUsed.hasItemMeta())
                    silkTouch = Objects.requireNonNull(itemUsed.getItemMeta()).hasEnchant(Enchantment.SILK_TOUCH);

                if (!silkTouch)
                    e.setExpToDrop(definition.dropExperience);

                CustomBlockFunctions.OnCustomBlockMined(e, definition);
                CustomBlockFunctions.DropBlockItems(e.getPlayer().getInventory().getItemInMainHand(), definition, e.getBlock());
            }
        }

        // Remove item frame and remove block from database
        CustomBlockFunctions.OnCustomBlockBroken(e.getBlock().getLocation(), definition.breakSound);
        linkedFrame.remove();

    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {

        for (Block block : e.blockList()) {
            if (CustomBlockDatabase.blockIsInDatabase(block.getLocation())) {

                Entity linkedFrame = CustomBlockUtils.GetLinkedItemFrame(block.getLocation());
                if (linkedFrame == null) return;
                CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromItemFrame(linkedFrame);
                if (definition == null) return;

                CustomBlockFunctions.DropBlockItems(null, definition, block);
                CustomBlockFunctions.OnCustomBlockBroken(block.getLocation(), definition.breakSound);

                linkedFrame.remove();

            }
        }

    }

    @EventHandler
    public void onFrameBroken(HangingBreakEvent e) {
        e.setCancelled(CustomBlockDatabase.blockIsInDatabase(e.getEntity().getLocation()));
    }

}