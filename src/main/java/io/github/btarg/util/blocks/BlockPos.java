package io.github.btarg.util.blocks;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class BlockPos implements Serializable {
    @Getter
    private double x;
    @Getter
    private double y;
    @Getter
    private double z;

    public BlockPos(double _x, double _y, double _z) {
        this.x = _x;
        this.y = _y;
        this.z = _z;
    }

    public BlockPos(@NotNull Location location) {
        this.x = location.blockX();
        this.y = location.blockY();
        this.z = location.blockZ();
    }

    public boolean CompareToVector(Vector vector) {
        return (this.x == vector.getX() && this.y == vector.getY() && this.z == vector.getZ());
    }

    public boolean Compare(BlockPos bpos) {
        return (this.x == bpos.getX() && this.y == bpos.getY() && this.z == bpos.getZ());
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

}
