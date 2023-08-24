package io.github.btarg.blockdata;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.DefaultDefinitions;
import io.github.btarg.registry.CustomBlockRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public class BlockConfig {

    private final Plugin plugin;

    public BlockConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static String getBlockConfigDirectory(Plugin plugin) {
        if (plugin == null) return null;
        return plugin.getDataFolder() + File.separator + "blocks" + File.separator;
    }

    private File getFile(String fileName) {
        String fullFileName = fileName;

        if (!fileName.endsWith(".yml")) {
            fullFileName = fileName + ".yml";
        }

        File file = new File(getBlockConfigDirectory(this.plugin), fullFileName);
        try {
            if (!file.exists()) {
                FileUtils.createParentDirectories(file);
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public CustomBlockDefinition getBlockDefinitionFromFile(String fileName) {
        File file = getFile(fileName);
        FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

        CustomBlockDefinition definition = (CustomBlockDefinition) conf.get("block");
        if (definition == null) {
            return null;
        }
        if (definition.id == null) {
            definition.id = FilenameUtils.removeExtension(fileName);
        }

        return definition;
    }

    public void loadAndRegisterBlocks(CommandSender sender) {
        Path blocksDirectory = Paths.get(getBlockConfigDirectory(OrigamiMain.getPlugin(OrigamiMain.class)));

        if (!blocksDirectory.toFile().exists()) {
            blocksDirectory.toFile().mkdirs();
        }

        try (Stream<Path> paths = Files.walk(blocksDirectory)) {
            AtomicInteger fileCount = new AtomicInteger();
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".yml"))
                    .forEach(f -> {
                        String cleanName = FilenameUtils.removeExtension(f.getFileName().toString());
                        CustomBlockDefinition loadedDefinition = getBlockDefinitionFromFile(cleanName);
                        if (loadedDefinition != null) {

                            CustomBlockRegistry.RegisterBlock(loadedDefinition);

                            if (sender != null) {
                                sender.sendMessage("Registered block: " + loadedDefinition.id);
                            }
                            fileCount.getAndIncrement();

                        }
                    });
            if (fileCount.get() == 0) {
                Bukkit.getLogger().warning("No block definitions found! Creating a new example block definition.");

                CustomBlockDefinition definition = DefaultDefinitions.getDefaultBlockDefinition();

                // save to file
                saveBlockDefinitionToFile(definition);
                CustomBlockRegistry.RegisterBlock(definition);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAndRegisterBlocks() {
        loadAndRegisterBlocks(null);
    }

    public void saveBlockDefinitionToFile(CustomBlockDefinition definition) {
        if (definition.id != null) {
            String fileName = definition.id;
            File file = getFile(fileName);
            FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
            conf.set("block", definition);
            SaveConfig(conf, file);
        }
    }

    private void SaveConfig(FileConfiguration conf, File file) {
        try {
            conf.save(file);
            Bukkit.getLogger().info("Saved block definition: " + file.getPath());
        } catch (IOException e) {
            Bukkit.getLogger().warning("Unable to save " + file.getPath()); // shouldn't really happen, but save throws the exception
        }
    }

}
