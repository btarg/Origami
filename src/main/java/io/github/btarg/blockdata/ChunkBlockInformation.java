package io.github.btarg.blockdata;

import io.github.btarg.util.blocks.BlockPos;
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
        this.blocksMap.put(new BlockPos(location), uuid);
    }

    public void removeBlock(Location location) {
        if (this.blocksMap == null) return;
        for (var entry : this.blocksMap.keySet()) {
            if (entry.Compare(new BlockPos(location))) {
                this.blocksMap.remove(entry);
            }
        }
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
