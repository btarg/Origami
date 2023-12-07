package io.github.btarg.origami.util.items;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ToolLevelHelper {
    public static double getToolLevel(ItemStack itemStack, boolean getToolMultiplier) {
        if (itemStack == null) {
            return 1.0;
        }
        Material mat = itemStack.getType();
        double toolLevel = 1.0;
        String name = mat.name();

        // if getToolMultiplier is true then we get a multiplier for use in rendering the break animation
        if (name.startsWith("WOODEN")) {
            toolLevel = !getToolMultiplier ? 1 : 1.5;
        } else if (name.startsWith("STONE")) {
            toolLevel = 2;
        } else if (name.startsWith("IRON")) {
            toolLevel = !getToolMultiplier ? 2.5 : 3;
        } else if (name.startsWith("DIAMOND")) {
            toolLevel = !getToolMultiplier ? 4 : 3;
        } else if (name.startsWith("GOLDEN")) {
            toolLevel = !getToolMultiplier ? 1 : 3;
        } else if (name.startsWith("NETHERITE")) {
            toolLevel = !getToolMultiplier ? 5 : 3.5;
        }
        return toolLevel;
    }

    public static Boolean checkItemTypeByString(ItemStack itemStack, List<String> canMine) {
        if (itemStack == null || canMine == null || canMine.isEmpty()) {
            return true;
        }
        Material mat = itemStack.getType();

        for (String checkString : canMine) {
            switch (checkString.toLowerCase().trim()) {
                case "pickaxes":
                    return Tag.ITEMS_PICKAXES.isTagged(mat);
                case "axes":
                    return Tag.ITEMS_AXES.isTagged(mat);
                case "shovels":
                    return Tag.ITEMS_SHOVELS.isTagged(mat);
                case "hoes":
                    return Tag.ITEMS_HOES.isTagged(mat);
                case "swords":
                    return Tag.ITEMS_SWORDS.isTagged(mat);
                default:
                    return true;
            }
        }
        return true;

    }
}
