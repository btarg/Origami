package io.github.btarg.util;

import io.github.btarg.OrigamiMain;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ContentPackHelper {

    @Getter
    private static final File contentPacksFolder = new File(OrigamiMain.getInstance().getDataFolder(), "custom");

    public static File[] getAllContentPacks() throws IOException {
        FileUtils.createParentDirectories(contentPacksFolder);
        return Arrays.stream(Objects.requireNonNull(contentPacksFolder.listFiles())).filter(File::isDirectory).toArray(File[]::new);
    }

}
