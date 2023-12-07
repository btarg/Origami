package io.github.btarg.origami.definitions;

import io.github.btarg.origami.OrigamiMain;
import io.github.btarg.origami.definitions.base.BaseCustomDefinition;
import io.github.btarg.origami.util.ComponentHelper;
import io.github.btarg.origami.util.parsers.ItemParser;
import io.github.btarg.origami.registry.CustomBlockRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CustomBlockDefinition extends BaseCustomDefinition {

    private final Random random = new Random();
    public Integer brightness;
    public List<String> drops;
    public String dropLootTable;
    public Boolean isAffectedByFortune;
    public Boolean canBePushed;
    public Integer dropExperience;
    public Integer toolLevelRequired;
    public List<String> canBeMinedWith;
    public Integer timeToBreak;
    public String breakSound;
    public String placeSound;

    @SuppressWarnings("unchecked")
    public CustomBlockDefinition(Map<String, Object> map) {
        super(map);
        this.id = null;
        if (this.baseMaterial == null || !this.baseMaterial.isBlock()) {
            Bukkit.getLogger().severe("Custom Blocks require a base block to be set! Defaulting to glass...");
            this.baseMaterial = Material.GLASS;
        }
        this.brightness = Objects.requireNonNullElse((Integer) map.get("brightness"), this.baseMaterial.createBlockData().getLightEmission());
        this.dropLootTable = (String) map.get("dropLootTable");
        this.drops = Objects.requireNonNullElse((List<String>) map.get("drops"), new ArrayList<>());
        this.isAffectedByFortune = Objects.requireNonNullElse((Boolean) map.get("isAffectedByFortune"), false);
        this.canBePushed = Objects.requireNonNullElse((Boolean) map.get("canBePushed"), true);
        this.dropExperience = Objects.requireNonNullElse((Integer) map.get("dropExperience"), 0);
        this.toolLevelRequired = Objects.requireNonNullElse((Integer) map.get("toolLevelRequired"), 0);
        this.canBeMinedWith = Objects.requireNonNullElse((List<String>) map.get("canBeMinedWith"), new ArrayList<>());
        this.timeToBreak = Objects.requireNonNullElse((Integer) map.get("timeToBreak"), 40);
        this.breakSound = (String) map.get("breakSound");
        this.placeSound = (String) map.get("placeSound");
    }

    @Override
    public void registerDefinition(CommandSender sender) {
        CustomBlockRegistry.registerBlock(this);
        if (sender != null) {
            ComponentHelper.sendDecoratedChatMessage("Registered block: " + this.id, sender);
        }
    }

    public boolean dropBlock() {
        return (dropLootTable == null || dropLootTable.isEmpty()) && (drops == null || drops.isEmpty());
    }

    public Collection<ItemStack> getDrops(Entity entity, Location loc) {
        Collection<ItemStack> dropStacks = new ArrayList<>();

        if (entity instanceof Player player) {
            ItemStack minedWith = player.getInventory().getItemInMainHand();

            if (this.dropLootTable != null) {
                dropStacks = OrigamiMain.getLootTableHelper().getBlockDrops(this.dropLootTable, loc, minedWith);
            }
            if (this.drops != null) {
                for (String dropString : this.drops) {
                    ItemStack dropStack = ItemParser.parseItemStack(dropString);
                    if (dropStack == null) continue;

                    int amount = dropStack.getAmount();
                    if (minedWith.getEnchantments().containsKey(Enchantment.LOOT_BONUS_BLOCKS) && this.isAffectedByFortune) {
                        amount = getFortuneCount(amount, minedWith.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
                    }
                    dropStacks.add(new ItemStack(dropStack.getType(), amount));
                }
            }
        }
        return dropStacks;
    }

    public int getFortuneCount(int amount, int fortune) {
        if (fortune > 0) {
            int i = random.nextInt(fortune + 2) - 1;

            if (i < 0) {
                i = 0;
            }

            return amount * (i + 1);
        } else {
            return amount;
        }
    }

    @Override
    public CustomBlockDefinition getDefaultDefinition() {
        Bukkit.getLogger().warning("No block definitions found! Creating a new example block definition.");

        this.id = "rainbow_block";
        this.baseMaterial = Material.GLASS;
        this.model = "rainbow";
        this.displayName = "&cR&6a&ei&an&9b&bo&5w &6B&el&ao&9c&bk";
        this.addEvent("onRightClick", Collections.singletonList("tellraw @s {\"text\":\"The block reverberates majestically.\",\"italic\":true,\"color\":\"gray\"}"), 20);
        this.lore = Collections.singletonList("<rainbow>It shimmers beautifully in the sunlight.</rainbow>");
        this.drops = List.of("DIAMOND(1)");
        this.isAffectedByFortune = true;
        this.dropExperience = 0;
        this.toolLevelRequired = 2;
        this.canBeMinedWith = Collections.singletonList("pickaxes");
        this.timeToBreak = 40;
        this.breakSound = Sound.BLOCK_AMETHYST_BLOCK_BREAK.toString();
        this.placeSound = Sound.BLOCK_AMETHYST_BLOCK_PLACE.toString();
        return this;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("brightness", this.brightness);
        map.put("drops", this.drops);
        map.put("dropLootTable", this.dropLootTable);
        map.put("isAffectedByFortune", this.isAffectedByFortune);
        map.put("canBePushed", this.isAffectedByFortune);
        map.put("dropExperience", this.dropExperience);
        map.put("toolLevelRequired", this.toolLevelRequired);
        map.put("canBeMinedWith", this.canBeMinedWith);
        map.put("timeToBreak", this.timeToBreak);
        map.put("breakSound", this.breakSound);
        map.put("placeSound", this.placeSound);
        map.putAll(super.serialize());
        return map;
    }

}
