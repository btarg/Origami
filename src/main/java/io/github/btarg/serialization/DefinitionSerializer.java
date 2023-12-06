package io.github.btarg.serialization;

import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.definitions.CustomRecipeDefinition;
import io.github.btarg.definitions.base.AbstractBaseDefinition;
import io.github.btarg.definitions.base.BaseCustomDefinition;
import io.github.btarg.util.ComponentHelper;
import io.github.btarg.util.ContentPackHelper;
import io.github.btarg.util.NamespacedKeyHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class DefinitionSerializer {

    private static final Map<Class<?>, CommandSender> exampleRegisterQueue = new HashMap<>();

    public static String getConfigDirectory(String parentDirectory, Class<?> clazz) {
        String dirName = null;
        if (clazz.equals(CustomBlockDefinition.class)) {
            dirName = "blocks";
        } else if (clazz.equals(CustomRecipeDefinition.class)) {
            dirName = "recipes";
        } else if (clazz.equals(CustomItemDefinition.class)) {
            dirName = "items";
        }
        if (dirName != null) {
            return Paths.get(ContentPackHelper.getContentPacksFolder().toString(), parentDirectory, dirName).toString();
        } else {
            return null;
        }

    }

    public void loadAndRegister(CommandSender sender, String parentDirectory, Class<?> definitionClass) {
        String pathString = getConfigDirectory(parentDirectory, definitionClass);
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

                        AbstractBaseDefinition loadedDefinition;
                        try {
                            loadedDefinition = getAnyDefinitionFromFile(cleanName, parentDirectory, definitionClass);
                        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                                 IllegalAccessException e) {
                            Bukkit.getLogger().severe("Could not load definition from file! See the stack trace for more information.");
                            e.printStackTrace();
                            fileCount.getAndIncrement();
                            return;
                        }

                        if (loadedDefinition != null) {
                            loadedDefinition.registerDefinition(sender);
                            fileCount.getAndIncrement();
                        }

                    });

            ComponentHelper.sendDecoratedChatMessage("Registered " + fileCount.get() + " definitions(s) from " + parentDirectory, sender);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerExample(Class<?> definitionClass, CommandSender sender) {
        AbstractBaseDefinition saveDef = null;
        try {
            saveDef = getDefaultDefinitionFromClass(definitionClass);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (saveDef != null) {
            saveDefinitionToFile(saveDef, "example");
            saveDef.registerDefinition(sender);
        }
    }

    public void loadAndRegister(CommandSender sender, Class<?> definitionClass) throws IOException {
        File[] contentPacksList = ContentPackHelper.getAllContentPacks();
        if (contentPacksList.length > 0) {
            for (File parentDirectory : contentPacksList) {
                loadAndRegister(sender, parentDirectory.getName(), definitionClass);
            }
        } else {
            exampleRegisterQueue.put(definitionClass, sender);
        }

    }

    public void registerQueuedExamples() {
        for (var example : exampleRegisterQueue.entrySet()) {
            registerExample(example.getKey(), example.getValue());
        }
        exampleRegisterQueue.clear();
    }

    public void saveDefinitionToFile(AbstractBaseDefinition def, String parentDirectory) {
        if (def == null) return;
        String saveDir = getConfigDirectory(parentDirectory, def.getClass());
        if (def instanceof CustomRecipeDefinition definition) {
            // Recipes
            if (definition.namespacedKey != null) {
                String fileName = definition.namespacedKey.value();
                File file = getFile(saveDir, fileName);
                FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

                for (var entry : definition.serialize().entrySet()) {
                    conf.set(entry.getKey(), entry.getValue());
                }

                SaveConfig(conf, file);
            }
        } else if (def instanceof BaseCustomDefinition definition) {
            // Blocks and items
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
        String fullFileName = !fileName.endsWith(".yml") ? fileName + ".yml" : fileName;
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

    public AbstractBaseDefinition getAnyDefinitionFromFile(String fileName, String parentDirectory, Class<?> definitionClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        File file = getFile(getConfigDirectory(parentDirectory, definitionClass), fileName);
        FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
        var constructor = definitionClass.getConstructor(Map.class);
        var values = conf.getValues(true);
        // Call constructor with the map as the value
        Object obj = constructor.newInstance(values);

        if (obj instanceof BaseCustomDefinition definition) {
            definition.id = FilenameUtils.removeExtension(fileName);
            definition.contentPack = FilenameUtils.getBaseName(parentDirectory);
            return definition;
        } else if (obj instanceof CustomRecipeDefinition definition) {
            String name = FilenameUtils.removeExtension(fileName);
            definition.namespacedKey = NamespacedKeyHelper.getUniqueNamespacedKey(name);
            definition.contentPack = FilenameUtils.getBaseName(parentDirectory);
            return definition;

        }
        return null;
    }

    public AbstractBaseDefinition getDefaultDefinitionFromClass(Class<?> definitionClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var constructor = definitionClass.getConstructor(Map.class);
        Object obj = constructor.newInstance(new HashMap<>());
        if (obj instanceof AbstractBaseDefinition definition) {
            return definition.getDefaultDefinition();
        }
        return null;
    }

}
