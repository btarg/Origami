package io.github.btarg.serialization;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomRecipeDefinition;
import io.github.btarg.definitions.DefaultDefinitions;
import io.github.btarg.registry.CustomRecipeRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public class RecipeConfig {

    private static String getConfigDirectory() {
        return OrigamiMain.Instance.getDataFolder() + File.separator + "recipes" + File.separator;
    }

    private File getFile(String fileName) {
        String fullFileName = fileName;

        if (!fileName.endsWith(".yml")) {
            fullFileName = fileName + ".yml";
        }

        File file = new File(getConfigDirectory(), fullFileName);
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

    public CustomRecipeDefinition getRecipeFromFile(String fileName) {
        File file = getFile(fileName);
        FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

        CustomRecipeDefinition definition = (CustomRecipeDefinition) conf.get("recipe");
        if (definition == null) return null;
        if (definition.namespacedKey == null) {
            definition.namespacedKey = new NamespacedKey(OrigamiMain.Instance, FilenameUtils.removeExtension(fileName));
        }

        return definition;
    }

    public void loadAndRegisterRecipes(CommandSender sender) {
        Path recipesDirectory = Paths.get(getConfigDirectory());

        if (!recipesDirectory.toFile().exists()) {
            recipesDirectory.toFile().mkdirs();
        }

        try (Stream<Path> paths = Files.walk(recipesDirectory)) {
            AtomicInteger fileCount = new AtomicInteger();
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".yml"))
                    .forEach(f -> {
                        String cleanName = FilenameUtils.removeExtension(f.getFileName().toString());
                        CustomRecipeDefinition loadedDefinition = getRecipeFromFile(cleanName);
                        if (loadedDefinition != null) {

                            CustomRecipeRegistry.RegisterRecipe(loadedDefinition);

                            if (sender != null) {
                                sender.sendMessage("Registered recipe: " + loadedDefinition.namespacedKey.value());
                            }
                            fileCount.getAndIncrement();

                        }
                    });
            if (fileCount.get() == 0) {
                Bukkit.getLogger().warning("No recipe definitions found! Creating a new example recipe definition.");

                CustomRecipeDefinition definition = DefaultDefinitions.getDefaultRecipeDefinition();

                // save to file
                saveRecipeDefinitionToFile(definition);
                CustomRecipeRegistry.RegisterRecipe(definition);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAndRegisterRecipes() {
        loadAndRegisterRecipes(null);
    }

    public void saveRecipeDefinitionToFile(CustomRecipeDefinition definition) {
        if (definition.namespacedKey != null) {
            String fileName = definition.namespacedKey.value();
            File file = getFile(fileName);
            FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
            conf.set("recipe", definition);

            SaveConfig(conf, file);
        }
    }

    private void SaveConfig(FileConfiguration conf, File file) {
        try {
            conf.save(file);
            Bukkit.getLogger().info("Saved recipe definition: " + file.getPath());
        } catch (IOException e) {
            Bukkit.getLogger().warning("Unable to save " + file.getPath()); // shouldn't really happen, but save throws the exception
        }
    }

}
