package io.github.btarg.origami.util.datatypes;

import lombok.Getter;
import org.bukkit.Location;
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
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public boolean Compare(BlockPos bpos) {
        return (this.x == bpos.getX() && this.y == bpos.getY() && this.z == bpos.getZ());
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

}
