package io.github.btarg.serialization;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.*;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.registry.CustomItemRegistry;
import io.github.btarg.registry.CustomRecipeRegistry;
import io.github.btarg.util.NamespacedKeyHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class DefinitionSerializer {

    public static String getConfigDirectory(Class<?> clazz) {
        String dirName = null;
        if (clazz.equals(CustomBlockDefinition.class)) {
            dirName = "blocks";
        } else if (clazz.equals(CustomRecipeDefinition.class)) {
            dirName = "recipes";
        } else if (clazz.equals(CustomItemDefinition.class)) {
            dirName = "items";
        }
        if (dirName != null) {
            return OrigamiMain.getInstance().getDataFolder() + File.separator + dirName + File.separator;
        } else {
            return null;
        }

    }

    public void loadAndRegister(CommandSender sender, Class<?> definitionClass) {
        String pathString = getConfigDirectory(definitionClass);
        if (pathString == null) return;

        Path definitionDirectory;
        try {
            definitionDirectory = Paths.get(pathString);
        } catch (InvalidPathException e) {
            e.printStackTrace();
            return;
        }

        if (!definitionDirectory.toFile().exists()) {
            try {
                definitionDirectory.toFile().mkdirs();
            } catch (SecurityException ex) {
                ex.printStackTrace();
                return;
            }
        }

        try (Stream<Path> paths = Files.walk(definitionDirectory)) {
            AtomicInteger fileCount = new AtomicInteger();
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".yml"))
                    .forEach(f -> {
                        String cleanName = FilenameUtils.removeExtension(f.getFileName().toString());

                        Object loadedDefinition = null;
                        try {
                            loadedDefinition = getAnyDefinitionFromFile(cleanName, definitionClass);
                        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                                 IllegalAccessException e) {
                            Bukkit.getLogger().severe("Could not load definition from file! See the stack trace for more information.");
                            e.printStackTrace();
                            fileCount.getAndIncrement();
                            return;
                        }

                        if (loadedDefinition != null) {

                            if (loadedDefinition instanceof CustomBlockDefinition blockDefinition) {
                                CustomBlockRegistry.RegisterBlock(blockDefinition);
                                if (sender != null) {
                                    sender.sendMessage("Registered block: " + blockDefinition.id);
                                }
                            } else if (loadedDefinition instanceof CustomRecipeDefinition recipeDefinition) {
                                CustomRecipeRegistry.RegisterRecipe(recipeDefinition);
                                if (sender != null) {
                                    sender.sendMessage("Registered recipe: " + recipeDefinition.namespacedKey.value());
                                }
                            } else if (loadedDefinition instanceof CustomItemDefinition itemDefinition) {
                                CustomItemRegistry.RegisterItem(itemDefinition);
                                if (sender != null) {
                                    sender.sendMessage("Registered item: " + itemDefinition.id);
                                }
                            }
                            fileCount.getAndIncrement();
                        }

                    });
            if (fileCount.get() == 0) {

                ConfigurationSerializable saveDef = DefaultDefinitions.getDefaultDefinition(definitionClass);
                // save to file
                saveDefinitionToFile(saveDef);

                if (saveDef instanceof CustomBlockDefinition blockDefinition) {
                    CustomBlockRegistry.RegisterBlock(blockDefinition);
                } else if (saveDef instanceof CustomRecipeDefinition recipeDefinition) {
                    CustomRecipeRegistry.RegisterRecipe(recipeDefinition);
                } else if (saveDef instanceof CustomItemDefinition itemDefinition) {
                    CustomItemRegistry.RegisterItem(itemDefinition);
                }
            } else if (sender != null) {
                sender.sendMessage("Registered " + fileCount.get() + " definitions(s)!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAndRegister(Class definitionClass) {
        loadAndRegister(null, definitionClass);
    }

    public void saveDefinitionToFile(ConfigurationSerializable def) {
        if (def == null) return;
        String saveDir = getConfigDirectory(def.getClass());
        if (def instanceof CustomRecipeDefinition definition) {
            if (definition.namespacedKey != null) {
                String fileName = definition.namespacedKey.value();
                File file = getFile(saveDir, fileName);
                FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

                for (var entry : definition.serialize().entrySet()) {
                    conf.set(entry.getKey(), entry.getValue());
                }

                SaveConfig(conf, file);
            }
        } else if (def instanceof CustomDefinition definition) {
            if (definition.id != null) {
                String fileName = definition.id;
                File file = getFile(saveDir, fileName);
                FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

                for (var entry : definition.serialize().entrySet()) {
                    conf.set(entry.getKey(), entry.getValue());
                }

                SaveConfig(conf, file);
            }
        }

    }

    private void SaveConfig(FileConfiguration conf, File file) {
        try {
            conf.save(file);
            Bukkit.getLogger().info("Saved definition: " + file.getPath());
        } catch (IOException e) {
            Bukkit.getLogger().warning("Unable to save " + file.getPath()); // shouldn't really happen, but save throws the exception
        }
    }

    private File getFile(String directory, String fileName) {
        String fullFileName = fileName;

        if (!fileName.endsWith(".yml")) {
            fullFileName = fileName + ".yml";
        }

        File file = new File(directory, fullFileName);
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

    public ConfigurationSerializable getAnyDefinitionFromFile(String fileName, Class<?> definitionClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        File file = getFile(getConfigDirectory(definitionClass), fileName);
        FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
        var constructor = definitionClass.getConstructor(Map.class);
        var values = conf.getValues(true);
        if (values == null) return null;
        if (constructor == null) return null;
        Object obj = constructor.newInstance(values);
        if (obj == null) return null;

        if (obj instanceof CustomDefinition definition) {
            definition.id = FilenameUtils.removeExtension(fileName);
            return definition;
        } else if (obj instanceof CustomRecipeDefinition definition) {
            String name = FilenameUtils.removeExtension(fileName);
            definition.namespacedKey = NamespacedKeyHelper.getUniqueNamespacedKey(name);
            return definition;

        }
        return null;
    }

}
