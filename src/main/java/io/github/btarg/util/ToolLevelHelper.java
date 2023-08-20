package io.github.btarg.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ToolLevelHelper {
    public static Integer GetToolLevel(ItemStack itemstack) {
        if (itemstack == null) {
            return 0;
        }
        Material mat = itemstack.getType();
        int toolLevel = 0;
        String name = mat.name();
        if (name.startsWith("WOODEN")) {
            toolLevel = 1;
        } else if (name.startsWith("STONE")) {
            toolLevel = 2;
        } else if (name.startsWith("IRON")) {
            toolLevel = 3;
        } else if (name.startsWith("DIAMOND")) {
            toolLevel = 4;
        } else if (name.startsWith("NETHERITE")) {
            toolLevel = 5;
        }
        return toolLevel;
    }
}
