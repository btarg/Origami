package io.github.btarg.blockdata;

import io.github.btarg.OrigamiMain;
import io.github.btarg.util.VectorHelper;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CustomBlockDatabase {

    private static final List<World> worldsInitialised = new ArrayList<>();
    private static HashMap<World.Environment, BlocksList> blocksListHashMap;

    private static String filePath(World world) {
        return OrigamiMain.getPlugin(OrigamiMain.class).getDataFolder().getPath() + File.separator + "_data" + File.separator + world.getName() + "-" + world.getEnvironment().name();
    }

    public static void initWorld(World world) {
        if (!worldsInitialised.contains(world)) {
            Bukkit.getLogger().info("Loading custom block database for world: " + world.getName());
            worldsInitialised.add(world);
            loadData(world);
        }
    }

    public static void loadData(World world) {
        try {

            FileInputStream stream = FileUtils.openInputStream(new File(filePath(world)));

            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(stream));
            BlocksList data = (BlocksList) in.readObject();
            in.close();

            if (data == null) {
                data = new BlocksList();
            }

            if (blocksListHashMap == null) {
                blocksListHashMap = new HashMap<>();
            }

            if (blocksListHashMap.containsKey(world.getEnvironment())) {
                blocksListHashMap.replace(world.getEnvironment(), data);
            } else {
                blocksListHashMap.put(world.getEnvironment(), data);
            }


        } catch (IOException e) {

            // create file if not exists
            try {
                FileUtils.openOutputStream(new File(filePath(world)));
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            blocksListHashMap = new HashMap<>();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            blocksListHashMap = null;
        }
    }

    public static HashMap<Vector, String> getBlocksInDatabase(World world) {
        HashMap<Vector, String> blocksInDatabase = new HashMap<>();
        loadData(world);
        if (blocksListHashMap != null && blocksListHashMap.containsKey(world.getEnvironment())) {
            blocksInDatabase = blocksListHashMap.get(world.getEnvironment()).blocksInDatabase;
        }
        return blocksInDatabase;
    }

    public static String getBlockUUIDFromDatabase(Location location) {
        for (Map.Entry<Vector, String> entry : getBlocksInDatabase(location.getWorld()).entrySet()) {

            if (VectorHelper.CompareVectors(entry.getKey().toBlockVector(), VectorHelper.ToBlockVectorWhole(location.toVector()))) {
                return entry.getValue();
            }
        }
        return "";
    }


    public static Boolean blockIsInDatabase(Location location) {
        String uuid = getBlockUUIDFromDatabase(location);
        return !uuid.isEmpty();
    }


    // When adding blocks to DB, use the hashmap - then we serialize the current environment's value to the correct file
    public static void addBlockToDatabase(Location location, String UUIDstring, Boolean save) {

        if (blocksListHashMap == null) return;

        World.Environment environment = location.getWorld().getEnvironment();

        // save only whole numbers into vector
        Vector locationVector = VectorHelper.ToBlockVectorWhole(location.toVector());


        // initialise with an empty blockslist which we can edit later
        if (!blocksListHashMap.containsKey(environment)) {
            blocksListHashMap.put(environment, new BlocksList());
        }


        // if the list is null then we create a new one
        if (!blocksListHashMap.get(environment).blocksInDatabase.containsKey(locationVector)) {
            blocksListHashMap.get(environment).blocksInDatabase.put(locationVector, UUIDstring);
        } else {
            blocksListHashMap.get(environment).blocksInDatabase.replace(locationVector, UUIDstring);
        }


        // save after adding
        if (save)
            saveData(location.getWorld());

    }

    public static void addBlockToDatabase(Location location, String UUIDstring) {
        addBlockToDatabase(location, UUIDstring, true);
    }


    public static void removeBlockFromDatabase(Location location, Boolean save) {

        if (blocksListHashMap == null) return;
        if (!blocksListHashMap.containsKey(location.getWorld().getEnvironment())) return;
        if (blocksListHashMap.get(location.getWorld().getEnvironment()).blocksInDatabase == null) return;

        Vector locationToRemove = null;

        for (Vector loc : blocksListHashMap.get(location.getWorld().getEnvironment()).blocksInDatabase.keySet()) {

            if (VectorHelper.CompareVectors(loc.toBlockVector(), location.toVector().toBlockVector())) {
                locationToRemove = loc;
                break;
            }

        }

        if (locationToRemove != null) {

            blocksListHashMap.get(location.getWorld().getEnvironment()).blocksInDatabase.remove(locationToRemove);

            if (save)
                saveData(location.getWorld());

        }
    }


    public static void saveData(World world) {

        if (blocksListHashMap == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(OrigamiMain.getPlugin(OrigamiMain.class), () -> {

            try {
                FileOutputStream stream = FileUtils.openOutputStream(new File(filePath(world)));
                BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(stream));

                out.writeObject(blocksListHashMap.get(world.getEnvironment()));
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }
}
