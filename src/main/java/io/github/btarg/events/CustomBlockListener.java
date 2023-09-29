package io.github.btarg.events;


import io.github.btarg.OrigamiMain;
import io.github.btarg.blockdata.CustomBlockPersistentData;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.rendering.BrokenBlock;
import io.github.btarg.rendering.BrokenBlocksService;
import io.github.btarg.util.blocks.CustomBlockUtils;
import io.github.btarg.util.items.ItemTagHelper;
import io.github.btarg.util.items.ToolLevelHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
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
import org.bukkit.util.Transformation;

import java.util.HashSet;
import java.util.List;
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

        ItemStack blockItem = event.getItemStack();
        ItemMeta meta = blockItem.getItemMeta();

        if (meta == null) return;
        String blockName = meta.getPersistentDataContainer().get(OrigamiMain.customItemTag, PersistentDataType.STRING);
        if (blockName == null) return;
        CustomBlockDefinition definition = CustomBlockRegistry.GetRegisteredBlock(blockName);
        if (definition == null) return;

        event.setCancelled(true);

        Block block = event.getBlock();
        World world = block.getWorld();
        Location placedLocation = event.getEntity().getLocation();

        // check for entities (other than item displays) that are too close
        java.util.Collection<Entity> entities = world.getNearbyEntities(placedLocation, 0.5, 0.5, 0.5);
        entities.removeIf(ent -> ent instanceof Display);

        if (!entities.isEmpty()) {
            return;
        }
        // Get block position, with slight offset as scaling the model on the Y axis scales downward
        Location blockLocation = new Location(world, placedLocation.getBlockX(), placedLocation.getBlockY() + 0.00015f, placedLocation.getBlockZ());
        Entity entity = world.spawn(CustomBlockUtils.getDisplayLocationFromBlock(blockLocation), ItemDisplay.class, ent -> {
            ent.setItemStack(blockItem);
            ent.setPersistent(true);
            ent.setInvulnerable(true);
            Transformation t = ent.getTransformation();
            Transformation transformation = new Transformation(t.getTranslation(), t.getLeftRotation(), t.getScale().add(0.0001f, 0.0003f, 0.0001f), t.getRightRotation());

            ent.setTransformation(transformation);
        });

        // set the item frame uuid to the same as the base block
        String block_uuid = entity.getUniqueId().toString();

        // if we failed to add a block because there is one already there, then we should cancel the item frame place event
        boolean added = CustomBlockPersistentData.storeBlockInformation(placedLocation, block_uuid);
        if (!added) {
            entity.remove();
            return;
        }
        world.setBlockData(placedLocation, definition.baseBlock.createBlockData());

        if (definition.placeSound != null) {
            world.playSound(block.getLocation(), Sound.valueOf(definition.placeSound), 1, 1);
        }
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

        Entity linkedFrame = CustomBlockUtils.getLinkedItemDisplay(e.getBlock().getLocation());
        if (linkedFrame == null) return;
        CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromItemDisplay(linkedFrame);
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
                CustomBlockFunctions.DropBlockItems(e.getPlayer(), definition, e.getBlock());
            }
        }

        // Remove item frame and remove block from database
        CustomBlockFunctions.OnCustomBlockBroken(e.getBlock().getLocation(), definition.breakSound);
        linkedFrame.remove();

    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {

        for (Block block : e.blockList()) {
            if (CustomBlockPersistentData.blockIsInStorage(block.getLocation())) {

                Entity linkedFrame = CustomBlockUtils.getLinkedItemDisplay(block.getLocation());
                if (linkedFrame == null) return;
                CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromItemDisplay(linkedFrame);
                if (definition == null) return;

                CustomBlockFunctions.DropBlockItems(e.getEntity(), definition, block);

                // remove without saving for better performance
                CustomBlockPersistentData.removeBlockFromStorage(block.getLocation());

                linkedFrame.remove();

            }
        }

    }

    @EventHandler
    public void onFrameBroken(HangingBreakEvent e) {
        e.setCancelled(CustomBlockPersistentData.blockIsInStorage(e.getEntity().getLocation()));
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        onPistonMove(e, e.getBlocks());
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        // move with sticky piston
        if (e.isSticky()) onPistonMove(e, e.getBlocks());
    }


    private void onPistonMove(BlockPistonEvent e, List<Block> blocks) {

        for (int i = blocks.size(); i-- > 0; ) {
            Block block = blocks.get(i);

            Entity linkedFrame = CustomBlockUtils.getLinkedItemDisplay(block.getLocation());
            if (linkedFrame == null) return;

            CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromItemDisplay(linkedFrame);
            if (definition == null) return;
            if (!definition.canBePushed) {
                e.setCancelled(true);
                return;
            }

            // get relative direction from original block of the moved block
            Location newLoc = block.getLocation().add(e.getDirection().getDirection()).toBlockLocation();

            // remove the old database entry and add the new one
            CustomBlockPersistentData.removeBlockFromStorage(block.getLocation());
            CustomBlockPersistentData.storeBlockInformation(newLoc, linkedFrame.getUniqueId().toString());

            // move the item frame to new location
            linkedFrame.teleport(CustomBlockUtils.getDisplayLocationFromBlock(newLoc));

        }
    }

}