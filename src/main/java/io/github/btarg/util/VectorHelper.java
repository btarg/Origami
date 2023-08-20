package io.github.btarg.util;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class VectorHelper {
    public static boolean CompareVectors(BlockVector vector1, BlockVector vector2) {
        return (vector1.getBlockX() == vector2.getBlockX() && vector1.getBlockY() == vector2.getBlockY() && vector1.getBlockZ() == vector2.getBlockZ());
    }

    public static BlockVector ToBlockVectorWhole(Vector location) {
        return new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
