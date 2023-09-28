package io.github.btarg.resourcepack;

import io.github.btarg.OrigamiMain;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;


public class ResourcePackGenerator {
    @Getter
    private static boolean isReady = false;

    public static void CreateZipFile(Runnable callback) {
        isReady = false;
        Bukkit.getLogger().info("Zipping resource pack...");
        Path pathFromConfig = FileUtils.getPackFile();
        if (pathFromConfig == null) return;

        if (Objects.requireNonNullElse((Boolean) OrigamiMain.config.get("generate-resource-pack"), true)) {
            if (pathFromConfig.toFile().exists()) {
                pathFromConfig.toFile().delete();
            }
            String packDir = (String) OrigamiMain.config.get("resource-packs.unzipped-resource-pack-path");
            if (packDir == null || !Path.of(packDir).toFile().isDirectory()) return;

            String finalPath = (String) OrigamiMain.config.get("resource-packs.zipped-resource-pack-path");
            if (finalPath == null) return;

            try {
                ZipFiles.zipFolder(packDir, finalPath);
                isReady = true;
                Bukkit.getLogger().info("Resource pack zipped!");
                callback.run();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }

    }

}
