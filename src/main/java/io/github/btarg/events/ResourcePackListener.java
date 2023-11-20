package io.github.btarg.events;

import io.github.btarg.OrigamiMain;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.PluginDisableEvent;
import team.unnamed.creative.BuiltResourcePack;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter;
import team.unnamed.creative.server.ResourcePackServer;

import java.io.IOException;
import java.util.Objects;

public class ResourcePackListener implements Listener {
    private static ResourcePackServer server;
    private static String resourcePackHash;

    public static void serveResourcePack(ResourcePack resourcePack) {

        // Start resource pack host server
        Integer port = Objects.requireNonNullElse((Integer) OrigamiMain.config.get("http-port"), 8008);
        BuiltResourcePack builtResourcePack = MinecraftResourcePackWriter.minecraft().build(resourcePack);
        resourcePackHash = builtResourcePack.hash();

        try {
            server = ResourcePackServer.server()
                    .address("127.0.0.1", port)
                    .pack(builtResourcePack)
                    .path("/" + resourcePackHash)
                    .build();

            server.start();
            Bukkit.getLogger().info("Hosting resource pack at http://localhost:" + port + "/" + resourcePackHash);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        String ipAddress = StringUtils.defaultIfEmpty(Bukkit.getServer().getIp(), "localhost");
        Integer port = Objects.requireNonNullElse((Integer) OrigamiMain.config.get("http-port"), 8008);

        if (resourcePackHash == null) {
            event.getPlayer().kick(Component.text("The server is still loading!\nTry rejoining in a second."));
        }

        try {
            event.getPlayer().setResourcePack("http://" + ipAddress + ":" + port + "/" + resourcePackHash, resourcePackHash);
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
        }

    }

    @EventHandler
    public void playerResourcePack(PlayerResourcePackStatusEvent e) {
        
        if (e.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD || e.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
            e.getPlayer().kick(Component.text("A resource pack is required to play on this server."), PlayerKickEvent.Cause.RESOURCE_PACK_REJECTION);
        }

    }

    @EventHandler
    private void onDisable(PluginDisableEvent e) {
        if (server != null) server.stop(0);
    }
}
