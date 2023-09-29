package io.github.btarg.blockdata;

import io.github.btarg.OrigamiMain;
import io.github.btarg.util.blocks.BlockPos;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class CustomBlockPersistentData {

    public static boolean storeBlockInformation(Location blockLocation, String uuid) {
        Chunk chunk = blockLocation.getChunk();
        ChunkBlockInformation information = getBlockInformation(chunk);

        information.addBlock(uuid, blockLocation);
        chunk.getPersistentDataContainer().set(OrigamiMain.chunkDataKey, new InformationDataType(), information);
        return true;
    }

    public static void removeBlockFromStorage(Location location) {
        Chunk chunk = location.getChunk();
        ChunkBlockInformation information = getBlockInformation(chunk);

        information.removeBlock(location);
        chunk.getPersistentDataContainer().set(OrigamiMain.chunkDataKey, new InformationDataType(), information);
    }

    public static ChunkBlockInformation getBlockInformation(Chunk chunk) {
        ChunkBlockInformation information = chunk.getPersistentDataContainer().get(OrigamiMain.chunkDataKey, new InformationDataType());
        // initialise map if empty
        if (information == null) {
            information = new ChunkBlockInformation(new HashMap<>());
        }
        return information;
    }

    public static String getUUIDFromLocation(Location location) {
        ChunkBlockInformation information = getBlockInformation(location.getChunk());
        if (information == null) return null;
        return information.getBlockUUID(location);
    }

    public static boolean blockIsInStorage(Location location) {
        return (getUUIDFromLocation(location) != null);
    }

    public static Map<BlockPos, String> getBlocksInStorage(Chunk chunk) {
        ChunkBlockInformation information = getBlockInformation(chunk);
        if (information == null) return null;
        return information.getBlocksMap();
    }
}
