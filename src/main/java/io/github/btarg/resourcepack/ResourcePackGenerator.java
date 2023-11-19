package io.github.btarg.resourcepack;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.registry.CustomItemRegistry;
import net.kyori.adventure.key.Key;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.model.*;
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter;
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer;
import team.unnamed.creative.texture.Texture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public class ResourcePackGenerator {
    public static ResourcePack generateResourcePack() {

        ResourcePack resourcePack = ResourcePack.resourcePack();
        resourcePack.packMeta(15, "Origami Server Pack");
        File packFolder = new File(OrigamiMain.getInstance().getDataFolder(), "generated");

        try {
            FileUtils.createParentDirectories(packFolder);

            PackTextures(resourcePack);

            // Overwrite vanilla item frame model
            Model.Builder itemFrameBuilder = Model.model()
                    .key(Key.key("minecraft:item/item_frame"))
                    .parent(Model.ITEM_GENERATED)
                    .textures(ModelTextures.builder().layers(ModelTexture.ofKey(Key.key("minecraft:item/item_frame"))).build());

            // generate models and collect as overrides for item frame
            List<ItemOverride> itemFrameOverrides = new ArrayList<>(GenerateModels(resourcePack, CustomBlockRegistry.getBlockDefinitions(), false));
            itemFrameOverrides.addAll(GenerateModels(resourcePack, CustomItemRegistry.getItemDefinitions(), true));

            // add the overrides to the item frame and build it
            itemFrameOverrides.forEach(itemFrameBuilder::addOverride);
            resourcePack.model(itemFrameBuilder.build());

            // save resource pack for debugging
            MinecraftResourcePackWriter.minecraft().writeToDirectory(packFolder, resourcePack);

            return resourcePack;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void PackTextures(ResourcePack resourcePack) {
        try {
            Path texturesFolderPath = new File(OrigamiMain.getInstance().getDataFolder(), "textures").toPath();
            FileUtils.createParentDirectories(texturesFolderPath.toFile());
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
                                    .key(Key.key("origami", relativePath))
                                    .data(Writable.file(f.toFile()))
                                    .build();
                            resourcePack.texture(texture);
                            fileCount.getAndIncrement();

                        });
            }

            Bukkit.getLogger().info("Processed " + fileCount.get() + " PNG files.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static List<ItemOverride> GenerateModels(ResourcePack pack, Map<String, ? extends CustomDefinition> definitions, boolean isItem) throws IOException {
        List<ItemOverride> itemOverrides = new ArrayList<>();
        // Read JSON models from the models folder
        Iterator<? extends Map.Entry<String, ? extends CustomDefinition>> iterator = definitions.entrySet().iterator();
        for (int i = 1; iterator.hasNext(); i++) {
            Map.Entry<String, ? extends CustomDefinition> entry = iterator.next();
            if (entry.getValue().model != null) {
                Key key = Key.key(entry.getKey());
                // deserialize json
                Model model = ModelSerializer.INSTANCE.deserialize(new FileInputStream(getBlockModelFile(entry.getValue().model, isItem)), key);
                // add to the list of overrides for the vanilla item frame
                itemOverrides.add(ItemOverride.of(key, ItemPredicate.customModelData(i)));
                pack.model(model);
            }
        }
        return itemOverrides;

    }

    public static File getFile(String folderName, String identifier, boolean isItem) throws IOException {
        File mainFolder = new File(OrigamiMain.getInstance().getDataFolder(), folderName);
        FileUtils.createParentDirectories(mainFolder);

        String subfolder = isItem ? "item" : "block";
        File thisFolder = new File(mainFolder, subfolder);
        FileUtils.createParentDirectories(thisFolder);

        return new File(thisFolder, identifier + ".json");
    }

    public static File getBlockModelFile(String identifier, boolean isItem) throws IOException {
        return getFile("models", identifier, isItem);
    }

    public static File getTextureFile(String identifier, boolean isItem) throws IOException {
        return getFile("textures", identifier, isItem);
    }

}
