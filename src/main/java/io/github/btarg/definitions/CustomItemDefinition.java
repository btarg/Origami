package io.github.btarg.definitions;

import io.github.btarg.util.parsers.EnchantmentParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unchecked")
public class CustomItemDefinition extends CustomDefinition implements ConfigurationSerializable {
    public Map<Enchantment, Integer> enchantments = new HashMap<>();
    public List<ItemFlag> flags = new ArrayList<>();
    public List<PotionEffectType> potionEffects = new ArrayList<>();
    public double damage = 0.0;

    public CustomItemDefinition(Map<String, Object> map) {
        super(map);

        this.id = null;
        if (this.baseMaterial == null) {
            Bukkit.getLogger().severe("Custom Items need a base material! Defaulting to a Command Block...");
            this.baseMaterial = Material.COMMAND_BLOCK;
        }

        this.leftClickCooldownTicks = Objects.requireNonNullElse((Integer) map.get("leftClickCooldown"), 0);
        this.rightClickCooldownTicks = Objects.requireNonNullElse((Integer) map.get("rightClickCooldown"), 0);


        this.enchantments = EnchantmentParser.parseEnchantments(Objects.requireNonNullElse((List<String>) map.get("enchantments"), new ArrayList<>()));
        this.flags = deserializeFlags(map);
        this.potionEffects = deserializePotionEffects(map);
        this.damage = Objects.requireNonNullElse((Double) map.get("damage"), 0.0);
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
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        List<String> enchantmentStrings = new ArrayList<>();
        for (var entry : this.enchantments.entrySet()) {
            enchantmentStrings.add(entry.getKey().getKey().value() + ";" + entry.getValue().toString());
        }
        map.put("enchantments", enchantmentStrings);

        map.put("potionEffects", this.potionEffects.stream().map(PotionEffectType::getName).toList());
        map.put("flags", this.flags.stream().map(ItemFlag::name).toList());

        map.put("damage", this.damage);
        map.putAll(super.serialize());
        return map;
    }
}
