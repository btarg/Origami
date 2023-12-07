package io.github.btarg.origami.util;

import io.github.btarg.origami.OrigamiMain;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ContentPackHelper {

    public static File getContentPacksFolder() {
        File folder = new File(OrigamiMain.getInstance().getDataFolder(), "custom");
        try {
            FileUtils.createParentDirectories(folder);
            if (!folder.isDirectory()) {
                // If it's not a directory, attempt to delete the file and recreate as a directory
                folder.delete();
                folder.mkdirs();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return folder;
    }


    public static File[] getAllContentPacks() {
        return Arrays.stream(Objects.requireNonNull(getContentPacksFolder().listFiles(File::isDirectory))).toArray(File[]::new);
    }
}

