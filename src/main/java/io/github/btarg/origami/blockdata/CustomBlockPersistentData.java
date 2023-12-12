package io.github.btarg.origami.blockdata;

import io.github.btarg.origami.util.NamespacedKeyHelper;
import io.github.btarg.origami.util.datatypes.BlockPos;
import io.github.btarg.origami.util.datatypes.InformationDataType;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class CustomBlockPersistentData {

    public static boolean storeBlockInformation(Location blockLocation, String uuid) {
        Chunk chunk = blockLocation.getChunk();
        ChunkBlockInformation information = getBlockInformation(chunk);

        information.addBlock(uuid, blockLocation);
        chunk.getPersistentDataContainer().set(NamespacedKeyHelper.chunkDataKey, new InformationDataType(), information);
        return true;
    }

    public static void removeBlockFromStorage(Location location) {
        Chunk chunk = location.getChunk();
        ChunkBlockInformation information = getBlockInformation(chunk);

        information.removeBlock(location);
        chunk.getPersistentDataContainer().set(NamespacedKeyHelper.chunkDataKey, new InformationDataType(), information);
    }

    public static ChunkBlockInformation getBlockInformation(Chunk chunk) {
        ChunkBlockInformation information = chunk.getPersistentDataContainer().get(NamespacedKeyHelper.chunkDataKey, new InformationDataType());
        // initialise map if empty
        if (information == null) {
            information = new ChunkBlockInformation(new HashMap<>());
        }
        return information;
    }

    public static String getUUIDFromLocation(Location location) {
        ChunkBlockInformation information = getBlockInformation(location.getChunk());
        return information.getBlockUUID(location);
    }

    public static boolean blockIsInStorage(Location location) {
        return (getUUIDFromLocation(location) != null);
    }

    public static Map<BlockPos, String> getBlocksInStorage(Chunk chunk) {
        ChunkBlockInformation information = getBlockInformation(chunk);
        return information.getBlocksMap();
    }
}
