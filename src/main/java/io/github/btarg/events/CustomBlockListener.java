package io.github.btarg.events;


import io.github.btarg.OrigamiMain;
import io.github.btarg.blockdata.CustomBlockPersistentData;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.rendering.BrokenBlock;
import io.github.btarg.rendering.BrokenBlocksService;
import io.github.btarg.util.NamespacedKeyHelper;
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

import java.util.*;

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
    public void onCustomBlockPlaced(HangingPlaceEvent event) {

        ItemStack blockItem = event.getItemStack();
        ItemMeta meta = blockItem.getItemMeta();

        if (meta == null) return;
        String blockName = meta.getPersistentDataContainer().get(NamespacedKeyHelper.customBlockItemTag, PersistentDataType.STRING);
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
        Location blockLocation = new Location(world, placedLocation.getBlockX(), placedLocation.getBlockY(), placedLocation.getBlockZ());
        Entity entity = world.spawn(CustomBlockUtils.getDisplayLocationFromBlock(blockLocation), ItemDisplay.class, ent -> {
            ent.setItemStack(blockItem);
            ent.setPersistent(true);
            ent.setInvulnerable(true);
            Transformation transformation = CustomBlockUtils.getDisplayTransformation(ent);
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
        world.setBlockData(placedLocation, definition.baseMaterial.createBlockData());

        if (definition.placeSound != null) {
            try {
                world.playSound(block.getLocation(), Sound.valueOf(definition.placeSound), 1, 1);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Block being placed does not have valid place sound: " + definition.id);
            }

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

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 2, -1, false, false));
        ItemStack playerHand = player.getInventory().getItemInMainHand();

        CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromBlock(block);
        if (definition == null) return;
        //TODO: properly determine the tool strength for breaking
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

        Entity linkedItemDisplay = CustomBlockUtils.getLinkedItemDisplay(e.getBlock().getLocation());
        if (linkedItemDisplay == null) return;
        CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromItemDisplay(linkedItemDisplay);
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
        linkedItemDisplay.remove();

    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {

        for (Block block : e.blockList()) {
            if (CustomBlockPersistentData.blockIsInStorage(block.getLocation())) {

                Entity linkedItemDisplay = CustomBlockUtils.getLinkedItemDisplay(block.getLocation());
                if (linkedItemDisplay == null) return;
                CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromItemDisplay(linkedItemDisplay);
                if (definition == null) return;

                CustomBlockFunctions.DropBlockItems(e.getEntity(), definition, block);

                // remove without saving for better performance
                CustomBlockPersistentData.removeBlockFromStorage(block.getLocation());

                linkedItemDisplay.remove();

            }
        }

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

        Map<Location, Display> pushedTempList = new HashMap<>();

        for (int i = blocks.size(); i-- > 0; ) {
            Block block = blocks.get(i);

            Display linkedItemDisplay = CustomBlockUtils.getLinkedItemDisplay(block.getLocation());
            if (linkedItemDisplay == null) continue;

            CustomBlockDefinition definition = CustomBlockUtils.getDefinitionFromItemDisplay(linkedItemDisplay);
            if (definition == null) continue;
            if (!definition.canBePushed) {
                e.setCancelled(true);
                return;
            }

            // get relative direction from original block of the moved block
            Location newLoc = block.getLocation().add(e.getDirection().getDirection()).toBlockLocation();
            pushedTempList.put(newLoc, linkedItemDisplay);

            CustomBlockPersistentData.removeBlockFromStorage(block.getLocation());

        }
        for (var pushedBlockEntry : pushedTempList.entrySet()) {
            CustomBlockPersistentData.storeBlockInformation(pushedBlockEntry.getKey(), pushedBlockEntry.getValue().getUniqueId().toString());
            // move the item display to new location
            pushedBlockEntry.getValue().teleport(CustomBlockUtils.getDisplayLocationFromBlock(pushedBlockEntry.getKey()));
            pushedBlockEntry.getValue().setTransformation(CustomBlockUtils.getDisplayTransformation(pushedBlockEntry.getValue()));
        }
    }

}