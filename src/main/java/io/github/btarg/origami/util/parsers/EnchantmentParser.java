package io.github.btarg.origami.util.parsers;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class EnchantmentParser {

    public static Map<Enchantment, Integer> parseEnchantments(List<String> enchantmentNames) {
        if (enchantmentNames == null) return new HashMap<>();

        Map<Enchantment, Integer> parsedEnchantments = new HashMap<>();

        for (String input : enchantmentNames) {
            // Split the input string by "(" and ")"
            String[] parts = input.trim().split("\\(");

            // Default to 1 so that if we don't specify a count we still get an item stack
            int level = 1;
            String enchantmentName = parts[0];

            try {
                // Attempt to parse the number between "(" and ")"
                if (parts.length == 2) {
                    level = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                }

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format in input.");
            }
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName.toLowerCase()));
            if (enchantment == null) {
                enchantment = Enchantment.getByName(enchantmentName.toUpperCase());
            }

            parsedEnchantments.put(enchantment, level);

        }

        return parsedEnchantments;
    }
}