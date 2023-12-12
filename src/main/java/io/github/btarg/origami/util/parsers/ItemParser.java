package io.github.btarg.origami.util.parsers;

import io.github.btarg.origami.OrigamiMain;
import io.github.btarg.origami.registry.RegistryHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemParser {
    public static ItemStack parseItemStack(String input) throws IllegalArgumentException {
        // Split the input string by "(" and ")"
        String[] parts = input.trim().split("\\(");

        // Default to 1 so that if we don't specify a count we still get an item stack
        int amount = 1;
        String materialName = parts[0];

        try {
            // Attempt to parse the number between "(" and ")"
            if (parts.length == 2) {
                amount = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in input.");
        }


        if (materialName.startsWith(OrigamiMain.PREFIX)) {
            return RegistryHelper.getAnyItemStack(materialName, amount);

        } else {
            // Convert the material name to uppercase (assuming it's in uppercase in the input)
            Material material = Material.matchMaterial(materialName.toUpperCase());

            if (material == null) {
                throw new IllegalArgumentException("Invalid material name: " + materialName);
            }

            return new ItemStack(material, amount);
        }

    }

}