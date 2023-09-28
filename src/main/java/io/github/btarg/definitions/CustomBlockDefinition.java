package io.github.btarg.definitions;

import io.github.btarg.util.ComponentHelper;
import io.github.btarg.util.items.ItemParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("CustomBlock")
public class CustomBlockDefinition implements ConfigurationSerializable {

    private final Random random = new Random();
    public String id;
    public Material baseBlock;
    public Boolean glowing;
    public Integer blockItemModelData;
    public Integer blockModelData;
    public String displayName;
    public List<String> drops;
    public String dropLootTable;
    public Boolean isAffectedByFortune;
    public Boolean canBePushed;
    public List<String> rightClickCommands;
    public List<String> lore;
    public Integer dropExperience;
    public Integer toolLevelRequired;
    public List<String> canBeMinedWith;
    public Integer timeToBreak;
    public String breakSound;
    public String placeSound;

    @SuppressWarnings("unchecked")
    public CustomBlockDefinition(Map<String, Object> map) {
        this.id = null;
        String baseBlockString = Objects.requireNonNullElse((String) map.get("baseBlock"), "GLASS");
        this.baseBlock = Objects.requireNonNullElse(Material.matchMaterial(baseBlockString.toUpperCase()), Material.GLASS);
        if (!this.baseBlock.isBlock()) {
            this.baseBlock = Material.GLASS;
        }
        this.glowing = Objects.requireNonNullElse((Boolean) map.get("glowing"), false);

        this.blockItemModelData = Objects.requireNonNullElse((Integer) map.get("blockItemModelData"), 0);
        this.blockModelData = Objects.requireNonNullElse((Integer) map.get("blockModelData"), 0);
        this.displayName = Objects.requireNonNullElse((String) map.get("displayName"), "Custom Block");

        this.dropLootTable = (String) map.get("dropLootTable");
        this.drops = Objects.requireNonNullElse((List<String>) map.get("drops"), new ArrayList<>());

        this.isAffectedByFortune = Objects.requireNonNullElse((Boolean) map.get("isAffectedByFortune"), false);
        this.canBePushed = Objects.requireNonNullElse((Boolean) map.get("canBePushed"), true);

        this.rightClickCommands = Objects.requireNonNullElse((List<String>) map.get("rightClickCommands"), new ArrayList<>());
        this.lore = Objects.requireNonNullElse((List<String>) map.get("lore"), new ArrayList<>());
        this.dropExperience = Objects.requireNonNullElse((Integer) map.get("dropExperience"), 0);
        this.toolLevelRequired = Objects.requireNonNullElse((Integer) map.get("toolLevelRequired"), 0);
        this.canBeMinedWith = Objects.requireNonNullElse((List<String>) map.get("canBeMinedWith"), new ArrayList<>());
        this.timeToBreak = Objects.requireNonNullElse((Integer) map.get("timeToBreak"), 40);
        this.breakSound = (String) map.get("breakSound");
        this.placeSound = (String) map.get("placeSound");
    }

    public static CustomBlockDefinition deserialize(Map<String, Object> map) {
        return new CustomBlockDefinition(map);
    }

    public boolean dropBlock() {
        return dropLootTable.isEmpty();
    }

    public Component getDisplayName() {
        Component nameComponent = ComponentHelper.deserializeGenericComponent(displayName);
        return ComponentHelper.removeItalicsIfAbsent(nameComponent);
    }

    public List<Component> getLore() {
        List<Component> toReturn = new ArrayList<>();
        lore.forEach(loreString -> {
            Component loreComponent = ComponentHelper.deserializeGenericComponent(loreString);
            toReturn.add(ComponentHelper.removeItalicsIfAbsent(loreComponent));
        });
        return toReturn;
    }

    public Collection<ItemStack> getDrops(Player player) {
        if (player == null) return null;
        Collection<ItemStack> dropStacks = new ArrayList<>();

        ItemStack minedWith = player.getInventory().getItemInMainHand();
        if (this.dropLootTable != null) {
            LootTable lootTable = Bukkit.getServer().getLootTable(Objects.requireNonNull(NamespacedKey.fromString(dropLootTable)));
            LootContext context = new LootContext.Builder(player.getLocation())
                    .lootingModifier(minedWith.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS))
                    .killer(player)
                    .lootedEntity(player)
                    .build();
            if (lootTable == null) return null;

            dropStacks = lootTable.populateLoot(new Random(), context);
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
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("baseBlock", this.baseBlock.name());
        map.put("glowing", this.glowing);
        map.put("blockModelData", this.blockModelData);
        map.put("blockItemModelData", this.blockItemModelData);
        map.put("displayName", this.displayName);
        map.put("drops", this.drops);
        map.put("dropLootTable", this.dropLootTable);
        map.put("isAffectedByFortune", this.isAffectedByFortune);
        map.put("canBePushed", this.isAffectedByFortune);
        map.put("rightClickCommands", this.rightClickCommands);
        map.put("lore", this.lore);
        map.put("dropExperience", this.dropExperience);
        map.put("toolLevelRequired", this.toolLevelRequired);
        map.put("canBeMinedWith", this.canBeMinedWith);
        map.put("timeToBreak", this.timeToBreak);
        map.put("breakSound", this.breakSound);
        map.put("placeSound", this.placeSound);

        return map;
    }

}
