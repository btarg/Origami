package io.github.btarg.origami.definitions;

import io.github.btarg.origami.definitions.base.BaseCustomDefinition;
import io.github.btarg.origami.registry.CustomItemRegistry;
import io.github.btarg.origami.resourcepack.ResourcePackGenerator;
import io.github.btarg.origami.util.ComponentHelper;
import io.github.btarg.origami.util.NamespacedKeyHelper;
import io.github.btarg.origami.util.parsers.AttributeParser;
import io.github.btarg.origami.util.parsers.EnchantmentParser;
import io.github.btarg.origami.util.parsers.PotionEffectParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unchecked")
public class CustomItemDefinition extends BaseCustomDefinition {
    public Map<Enchantment, Integer> enchantments;
    public Map<Attribute, Map<UUID, AttributeModifier>> attributes;
    public List<ItemFlag> flags = new ArrayList<>();
    public List<PotionEffect> potionEffects = new ArrayList<>();
    public Integer durability;

    public CustomItemDefinition(Map<String, Object> map) {
        super(map);

        this.id = null;
        if (this.baseMaterial == null) {
            Bukkit.getLogger().severe("Custom Items need a base material! Defaulting to a Command Block...");
            this.baseMaterial = Material.COMMAND_BLOCK;
        }
        this.attributes = AttributeParser.parseAttributes(Objects.requireNonNullElse((List<String>) map.get("attributes"), new ArrayList<>()));
        this.durability = Objects.requireNonNullElse((Integer) map.get("durability"), (int) this.baseMaterial.getMaxDurability());
        this.enchantments = EnchantmentParser.parseEnchantments(Objects.requireNonNullElse((List<String>) map.get("enchantments"), new ArrayList<>()));
        this.flags = deserializeFlags(map);
        this.potionEffects = PotionEffectParser.parsePotionEffects(Objects.requireNonNullElse((List<String>) map.get("potionEffects"), new ArrayList<>()));
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

    @Override
    protected Integer getCustomModelData() {
        return ResourcePackGenerator.getItemOverride(this);
    }

    @Override
    protected ItemMeta getItemMeta(ItemStack itemStack) {
        ItemMeta meta = super.getItemMeta(itemStack);
        meta.getPersistentDataContainer().set(NamespacedKeyHelper.customItemTag, PersistentDataType.STRING, this.id);

        for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
            Enchantment key = entry.getKey();
            Integer value = entry.getValue();
            if (key != null) {
                meta.addEnchant(key, value, true);
            }
        }
        meta.addItemFlags(this.flags.toArray(new ItemFlag[0]));
        //TODO: add attributes here
        for (var entry : this.attributes.entrySet()) {
            Attribute attribute = entry.getKey();
            Map<UUID, AttributeModifier> attributeModifiers = entry.getValue();

            for (AttributeModifier modifier : attributeModifiers.values()) {
                meta.addAttributeModifier(attribute, modifier);
            }
        }

        return meta;
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
        Map<String, Object> map = super.serialize();

        List<String> enchantmentStrings = this.enchantments.entrySet().stream()
                .map(entry -> entry.getKey() + "(" + entry.getValue() + ")")
                .toList();
        List<String> potionEffectStrings = this.potionEffects.stream()
                .map(effect -> effect.getType() + "(" + effect.getDuration() + ", " + effect.getAmplifier() + ")")
                .toList();

        map.put("enchantments", enchantmentStrings);
        map.put("potionEffects", potionEffectStrings);
        map.put("flags", this.flags.stream().map(ItemFlag::name).toList());
        map.put("durability", this.durability);

        return map;
    }

}
