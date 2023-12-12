package io.github.btarg.origami.util.parsers;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class PotionEffectParser {

    public static List<PotionEffect> parsePotionEffects(List<String> effectStrings) {
        if (effectStrings == null) return new ArrayList<>();

        List<PotionEffect> parsedEffects = new ArrayList<>();

        for (String input : effectStrings) {
            // Split the input string by "(" and ")"
            String[] parts = input.trim().split("\\(");

            // Default to 1 so that if we don't specify a count we still get an item stack
            int level = 1;
            int duration = 20; // Default duration

            String effectName = parts[0];

            try {
                // Attempt to parse the duration and level between "(" and ")"
                if (parts.length == 2) {
                    String[] durationLevel = parts[1].replaceAll("[^0-9,]", "").split(",");
                    if (durationLevel.length == 2) {
                        duration = Integer.parseInt(durationLevel[0]);
                        level = Integer.parseInt(durationLevel[1]);
                    }
                }

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format in input.");
            }

            PotionEffectType potionEffectType = PotionEffectType.getByName(effectName);
            if (potionEffectType != null) {
                PotionEffect effect = potionEffectType.createEffect(duration, level);
                parsedEffects.add(effect);
            }
        }

        return parsedEffects;
    }
}
