package io.github.btarg.origami.rendering;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BrokenBlocksService {

    private static final Map<Location, BrokenBlock> brokenBlocks = new HashMap<>();

    public void createBrokenBlock(Block block, double time) {
        if (isBrokenBlock(block.getLocation())) return;

        BrokenBlock brokenBlock = new BrokenBlock(block, time);
        brokenBlocks.put(block.getLocation(), brokenBlock);
    }

    public void removeBrokenBlock(Location location) {
        if (isBrokenBlock(location))
            brokenBlocks.remove(location);
    }

    public BrokenBlock getBrokenBlock(Location location) {
        if (!isBrokenBlock(location)) return null;
        return brokenBlocks.get(location);
    }

    public boolean isBrokenBlock(Location location) {
        return brokenBlocks.containsKey(location);
    }
}