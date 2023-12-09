package io.github.btarg.origami.definitions;

import io.github.btarg.origami.definitions.base.BaseCustomDefinition;
import io.github.btarg.origami.registry.CustomItemRegistry;
import io.github.btarg.origami.resourcepack.ResourcePackGenerator;
import io.github.btarg.origami.util.ComponentHelper;
import io.github.btarg.origami.util.NamespacedKeyHelper;
import io.github.btarg.origami.util.parsers.EnchantmentParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unchecked")
public class CustomItemDefinition extends BaseCustomDefinition {
    public Map<Enchantment, Integer> enchantments;
    public List<ItemFlag> flags = new ArrayList<>();
    public List<PotionEffectType> potionEffects = new ArrayList<>();
    public Integer durability;

    public CustomItemDefinition(Map<String, Object> map) {
        super(map);

        this.id = null;
        if (this.baseMaterial == null) {
            Bukkit.getLogger().severe("Custom Items need a base material! Defaulting to a Command Block...");
            this.baseMaterial = Material.COMMAND_BLOCK;
        }

        this.durability = Objects.requireNonNullElse((Integer) map.get("durability"), (int) this.baseMaterial.getMaxDurability());
        this.enchantments = EnchantmentParser.parseEnchantments(Objects.requireNonNullElse((List<String>) map.get("enchantments"), new ArrayList<>()));
        this.flags = deserializeFlags(map);
        this.potionEffects = deserializePotionEffects(map);
    }

    private List<ItemFlag> deserializeFlags(Map<String, Object> map) {
        List<String> itemFlagNames = (List<String>) map.get("flags");
        if (itemFlagNames == null) return new ArrayList<>();

        return new ArrayList<>(itemFlagNames.stream()
                .map(flagName -> {
                    // Make it case-insensitive and trim whitespace
                    flagName = flagName.trim().toUpperCase();
                    return ItemFlag.valueOf(flagName);
                })
                .toList());
    }

    private List<PotionEffectType> deserializePotionEffects(Map<String, Object> map) {
        List<String> potionEffectNames = (List<String>) map.get("potionEffects");
        List<PotionEffectType> potionEffectList = new ArrayList<>();

        if (potionEffectNames != null) {
            for (String potionEffectName : potionEffectNames) {
                PotionEffectType potionEffectType = PotionEffectType.getByName(potionEffectName);
                if (potionEffectType != null) {
                    potionEffectList.add(potionEffectType);
                }
            }
        }
        return potionEffectList;
    }

    @Override
    protected Integer getCustomModelData() {
        return ResourcePackGenerator.getItemOverride(this);
    }

    @Override
    protected ItemMeta getItemMeta(ItemStack itemStack) {
        ItemMeta meta = super.getItemMeta(itemStack);
        meta.getPersistentDataContainer().set(NamespacedKeyHelper.customItemTag, PersistentDataType.STRING, this.id);
        return meta;
    }

    @Override
    public ItemStack createCustomItemStack(int count) {
        ItemStack itemStack = new ItemStack(Material.ITEM_FRAME, count);
        ItemMeta meta = getItemMeta(itemStack);
        for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
            Enchantment key = entry.getKey();
            Integer value = entry.getValue();
            if (key != null) {
                meta.addEnchant(key, value, true);
            }
        }
        //TODO: add attributes here
        meta.addItemFlags(this.flags.toArray(new ItemFlag[0]));

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public CustomItemDefinition getDefaultDefinition() {
        return null;
    }

    @Override
    public void registerDefinition(CommandSender sender) {
        CustomItemRegistry.registerItem(this);
        if (sender != null) {
            ComponentHelper.sendDecoratedChatMessage("Registered item: " + this.id, sender);
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        List<String> enchantmentStrings = new ArrayList<>();
        for (var entry : this.enchantments.entrySet()) {
            enchantmentStrings.add(entry.getKey().getKey().value() + ";" + entry.getValue().toString());
        }
        map.put("enchantments", enchantmentStrings);
        map.put("potionEffects", this.potionEffects.stream().map(PotionEffectType::getName).toList());
        map.put("flags", this.flags.stream().map(ItemFlag::name).toList());
        map.put("durability", this.durability);

        map.putAll(super.serialize());
        return map;
    }
}
