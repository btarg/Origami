package io.github.btarg.origami.blockdata;

import io.github.btarg.origami.util.datatypes.BlockPos;
import lombok.Getter;
import org.bukkit.Location;

import java.io.Serializable;
import java.util.Map;

public class ChunkBlockInformation implements Serializable {
    @Getter
    private Map<BlockPos, String> blocksMap;

    public ChunkBlockInformation(Map<BlockPos, String> map) {
        this.blocksMap = map;
    }

    public void addBlock(String uuid, Location location) {
        if (this.blocksMap == null) return;
        this.blocksMap.put(new BlockPos(location), uuid);
    }

    public void removeBlock(Location location) {
        if (this.blocksMap == null) return;
        this.blocksMap.keySet().removeIf(entry -> entry.Compare(new BlockPos(location)));
    }

    public String getBlockUUID(Location location) {
        BlockPos bpos = new BlockPos(location);
        if (this.blocksMap == null) return null;
        for (var entry : this.blocksMap.entrySet()) {
            if (entry.getKey().Compare(bpos)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
