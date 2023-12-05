package io.github.btarg.resourcepack;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.registry.CustomItemRegistry;
import io.github.btarg.util.ComponentHelper;
import io.github.btarg.util.ContentPackHelper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.model.*;
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer;
import team.unnamed.creative.texture.Texture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


@SuppressWarnings({"PatternValidation", "UnstableApiUsage"})
public class ResourcePackGenerator {

    private static final Map<String, Integer> blockModelOverrideMap = new HashMap<>();
    private static final Map<String, Integer> itemModelOverrideMap = new HashMap<>();

    private static final List<ItemOverride> itemFrameOverrides = new ArrayList<>();

    public static int getBlockOverride(CustomBlockDefinition definition) {
        return Objects.requireNonNullElse(blockModelOverrideMap.get(definition.model), 1);
    }

    public static int getItemOverride(CustomItemDefinition definition) {
        return Objects.requireNonNullElse(itemModelOverrideMap.get(definition.model), 1);
    }

    public static ResourcePack generateResourcePack() throws IOException {

        File[] contentPacksList = ContentPackHelper.getAllContentPacks();

        ResourcePack resourcePack = ResourcePack.resourcePack();
        resourcePack.icon(Writable.copyInputStream(Objects.requireNonNull(OrigamiMain.getInstance().getResource("logo.png"))));
        String description = (String) OrigamiMain.config.get("resource-pack.pack-description");
        if (description == null || description.isBlank()) {
            description = "Powered by <color:#f51d5e>Origami</color>";
        }
        Component descriptionComponent = ComponentHelper.deserializeGenericComponent(description);
        resourcePack.packMeta(15, descriptionComponent);

        for (File dir : contentPacksList) {
            // dir is the current content pack folder
            Bukkit.getLogger().info("Found content pack: " + dir.getName());
            Path currentPackFolder = dir.toPath();
            packTextures(resourcePack, new File(dir, "textures").toPath());

            // Collect item overrides for vanilla item frame
            generateBlockModels(currentPackFolder, resourcePack);

            // Generate item overrides for each base material
            generateItemModels(currentPackFolder, resourcePack);
        }
        // Add item frame overrides
        Model.Builder itemFrameBuilder = Model.model().key(Key.key("minecraft:item/item_frame")).parent(Model.ITEM_GENERATED).textures(ModelTextures.builder().layers(ModelTexture.ofKey(Key.key("minecraft:item/item_frame"))).build());
        itemFrameOverrides.forEach(itemFrameBuilder::addOverride);
        resourcePack.model(itemFrameBuilder.build());

        return resourcePack;
    }

    private static void packTextures(ResourcePack resourcePack, Path texturesFolderPath) {
        try {
            if (!Files.exists(texturesFolderPath)) return;
            AtomicInteger fileCount = new AtomicInteger();

            try (Stream<Path> paths = Files.walk(texturesFolderPath, FileVisitOption.FOLLOW_LINKS)) {
                paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".png"))
                        .forEach(f -> {

                            // Use relativize to get the path relative to texturesFolderPath
                            // also, fuck windows, use a forward slash
                            String relativePath = texturesFolderPath.relativize(f).toString().replace("\\", "/");

                            Texture texture = Texture.texture()
                                    .key(Key.key(OrigamiMain.PREFIX + relativePath))
                                    .data(Writable.file(f.toFile()))
                                    .build();
                            resourcePack.texture(texture);
                            fileCount.getAndIncrement();

                        });
            }
            if (fileCount.get() == 0) {
                //TODO: add example texture
            }

            Bukkit.getLogger().info("Processed " + fileCount.get() + " PNG files.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static File getFile(Path parentFolder, String identifier, boolean isItem) throws IOException {
        File modelsFolder = new File(parentFolder.toFile(), "models");
        FileUtils.createParentDirectories(modelsFolder);

        String subfolder = isItem ? "item" : "block";
        File thisFolder = new File(modelsFolder, subfolder);
        FileUtils.createParentDirectories(thisFolder);

        return new File(thisFolder, identifier + ".json");
    }

    private static void generateBlockModels(Path parentFolder, ResourcePack pack) throws IOException {
        var definitions = CustomBlockRegistry.getBlockDefinitions(parentFolder.getFileName().toString());

        int i = 1;
        for (Map.Entry<String, CustomBlockDefinition> entry : definitions.entrySet()) {
            CustomBlockDefinition definition = entry.getValue();

            if (definition.model != null) {
                Key key = Key.key(entry.getKey());
                Model model;
                try {
                    model = ModelSerializer.INSTANCE.deserialize(new FileInputStream(getFile(parentFolder, definition.model, false)), key);
                    pack.model(model);
                    blockModelOverrideMap.put((definition.model), i);
                    itemFrameOverrides.add(ItemOverride.of(key, ItemPredicate.customModelData(i)));
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
                i++;
            }
        }
    }

    private static void generateItemModels(Path parentFolder, ResourcePack pack) throws IOException {
        var definitions = CustomItemRegistry.getItemDefinitions(parentFolder.getFileName().toString());
        Map<Material, Model.Builder> modelBuilders = new HashMap<>();

        int i = 1;
        for (Map.Entry<String, CustomItemDefinition> entry : definitions.entrySet()) {
            CustomItemDefinition definition = entry.getValue();

            if (definition.model != null) {
                Key key = Key.key(entry.getKey());
                try {
                    Model model = ModelSerializer.INSTANCE.deserialize(new FileInputStream(getFile(parentFolder, definition.model, true)), key);
                    pack.model(model);

                    itemModelOverrideMap.put(definition.model, i);

                    Key itemKey = Key.key("minecraft:item/" + definition.baseMaterial.name().toLowerCase());
                    modelBuilders.computeIfAbsent(definition.baseMaterial, k ->
                                    Model.model().key(itemKey).parent(Model.ITEM_GENERATED)
                                            .textures(ModelTextures.builder().layers(ModelTexture.ofKey(itemKey)).build()))
                            .addOverride(ItemOverride.of(key, ItemPredicate.customModelData(i)));

                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
                i++;
            }
        }

        modelBuilders.values().forEach(builder -> pack.model(builder.build()));
    }

}
