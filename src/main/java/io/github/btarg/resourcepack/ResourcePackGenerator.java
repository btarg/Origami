package io.github.btarg.resourcepack;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.registry.CustomItemRegistry;
import io.github.btarg.util.ComponentHelper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


@SuppressWarnings({"PatternValidation", "UnstableApiUsage"})
public class ResourcePackGenerator {

    private static final Map<String, Integer> modelOverrideMap = new HashMap<>();

    public static int getOverrideByModelName(String modelName) {
        return Objects.requireNonNullElse(modelOverrideMap.get(modelName), 1);
    }

    public static ResourcePack generateResourcePack() {

        ResourcePack resourcePack = ResourcePack.resourcePack();
        String description = (String) OrigamiMain.config.get("resource-pack.pack-description");
        if (description == null || description.isBlank()) {
            description = "Powered by <color:#f51d5e>Origami</color>";
        }
        Component descriptionComponent = ComponentHelper.deserializeGenericComponent(description);
        resourcePack.packMeta(15, descriptionComponent);
        File packFolder = new File(OrigamiMain.getInstance().getDataFolder(), "generated");

        try {
            packTextures(resourcePack);
            resourcePack.icon(Writable.copyInputStream(Objects.requireNonNull(OrigamiMain.getInstance().getResource("logo.png"))));

            // Overwrite vanilla item frame model
            Model.Builder itemFrameBuilder = Model.model()
                    .key(Key.key("minecraft:item/item_frame"))
                    .parent(Model.ITEM_GENERATED)
                    .textures(ModelTextures.builder().layers(ModelTexture.ofKey(Key.key("minecraft:item/item_frame"))).build());

            // generate models and collect as overrides for item frame
            List<ItemOverride> itemFrameOverrides = new ArrayList<>(generateModels(resourcePack, CustomBlockRegistry.getBlockDefinitions(), false));

            if (itemFrameOverrides.isEmpty()) {
                //TODO: add example block model
            }
            itemFrameOverrides.addAll(generateModels(resourcePack, CustomItemRegistry.getItemDefinitions(), true));
            if (itemFrameOverrides.isEmpty()) {
                //TODO: add example item model
            }

            // add the overrides to the item frame and build it
            itemFrameOverrides.forEach(itemFrameBuilder::addOverride);
            resourcePack.model(itemFrameBuilder.build());

            // save resource pack for debugging
            if (OrigamiMain.config.getBoolean("resource-pack.save-generated-pack")) {
                // create "generated" folder
                FileUtils.createParentDirectories(packFolder);
                // save pack to folder
                MinecraftResourcePackWriter.minecraft().writeToDirectory(packFolder, resourcePack);
            }

            return resourcePack;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void packTextures(ResourcePack resourcePack) {
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

    private static List<ItemOverride> generateModels(ResourcePack pack, Map<String, ? extends CustomDefinition> definitions, boolean isItem) throws IOException {
        List<ItemOverride> itemOverrides = new ArrayList<>();
        // Read JSON models from the models folder
        var iterator = definitions.entrySet().iterator();
        for (int i = 1; iterator.hasNext(); i++) {
            Map.Entry<String, ? extends CustomDefinition> entry = iterator.next();
            if (entry.getValue().model != null) {
                Key key = Key.key(entry.getKey());
                // deserialize json
                Model model = ModelSerializer.INSTANCE.deserialize(new FileInputStream(getBlockModelFile(entry.getValue().model, isItem)), key);
                // add to the list of overrides for the vanilla item frame
                itemOverrides.add(ItemOverride.of(key, ItemPredicate.customModelData(i)));
                // add to a map so we can get it later
                modelOverrideMap.put(entry.getValue().model, i);
                pack.model(model);
            }
        }
        return itemOverrides;

    }

    private static File getFile(String folderName, String identifier, boolean isItem) throws IOException {
        File mainFolder = new File(OrigamiMain.getInstance().getDataFolder(), folderName);
        FileUtils.createParentDirectories(mainFolder);

        String subfolder = isItem ? "item" : "block";
        File thisFolder = new File(mainFolder, subfolder);
        FileUtils.createParentDirectories(thisFolder);

        return new File(thisFolder, identifier + ".json");
    }

    private static File getBlockModelFile(String identifier, boolean isItem) throws IOException {
        return getFile("models", identifier, isItem);
    }

}
