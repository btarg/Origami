package io.github.btarg.resourcepack;

import io.github.btarg.OrigamiMain;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    public static String calcSHA1(Path filePath) throws
            IOException, NoSuchAlgorithmException {

        if (filePath == null) throw new IOException();

        byte[] input = Files.readAllBytes(filePath);

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(input);
        byte[] digest = md.digest();

        StringBuilder hexString = new StringBuilder();

        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public static String currentSHA1() {
        String calculated = "";
        try {
            calculated = calcSHA1(getPackFile());
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
        return calculated;
    }

    public static Path getPackFile() {
        String pathString = (String) OrigamiMain.config.get("resource-packs.zipped-resource-pack-path");
        if (pathString != null && !pathString.isEmpty())
            return Path.of(pathString);
        return null;
    }
}
