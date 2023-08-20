package io.github.btarg.blockdata;

import org.bukkit.util.Vector;

import java.io.Serializable;
import java.util.HashMap;

public class BlocksList implements Serializable {

    /**
     * Format: Block position vector, Block UUID
     */
    public HashMap<Vector, String> blocksInDatabase;

    public BlocksList() {
        this.blocksInDatabase = new HashMap<>();
    }


}