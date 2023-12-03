package io.github.btarg.web;

import io.github.btarg.OrigamiMain;
import io.javalin.Javalin;
import net.kyori.adventure.text.Component;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import team.unnamed.creative.BuiltResourcePack;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class JavalinServer {
    private static final File generatedZipFile = new File(OrigamiMain.getInstance().getDataFolder(), "generated/pack.zip");
    private static final String downloadEndpoint = "/dl/";
    public static Javalin javalin;
    public static String resourcePackHash;
    private static boolean isRunning = false;
    private static Integer port;

    public static void initAndServe(ResourcePack resourcePack) {
        port = Objects.requireNonNullElse((Integer) OrigamiMain.config.get("http-port"), 8008);

        Bukkit.getScheduler().runTaskAsynchronously(OrigamiMain.getInstance(), () -> {
            if (!isRunning) {
                javalin = Javalin.create(config -> {
                    config.showJavalinBanner = false;
                }).start(port);
                isRunning = true;
            }

            BuiltResourcePack builtResourcePack = MinecraftResourcePackWriter.minecraft().build(resourcePack);
            resourcePackHash = builtResourcePack.hash();

            try {
                FileUtils.createParentDirectories(generatedZipFile);
                MinecraftResourcePackWriter.minecraft().writeToZipFile(generatedZipFile, resourcePack);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            if (javalin == null) {
                Bukkit.getLogger().severe("Javalin server not started!");
                return;
            }

            javalin.get(downloadEndpoint + resourcePackHash, ctx -> {
                try {
                    ctx.result(FileUtils.readFileToByteArray(generatedZipFile)).contentType("application/zip");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Bukkit.getLogger().info("Hosting resource pack at http://localhost:" + port + downloadEndpoint + resourcePackHash);

        });
    }

    public static void sendResourcePack(Player player) {
        String ipAddress = StringUtils.defaultIfEmpty(Bukkit.getServer().getIp(), "localhost");
        Integer port = Objects.requireNonNullElse((Integer) OrigamiMain.config.get("http-port"), 8008);

        if (resourcePackHash == null || resourcePackHash.isBlank()) {
            player.kick(Component.text("The server is still loading!\nTry rejoining in a second."));
        }

        try {
            player.setResourcePack("http://" + ipAddress + ":" + port + downloadEndpoint + resourcePackHash, resourcePackHash);
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }
}
