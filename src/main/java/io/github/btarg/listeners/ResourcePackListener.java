package io.github.btarg.listeners;

import io.github.btarg.web.JavalinServer;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.PluginDisableEvent;

public class ResourcePackListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        JavalinServer.sendResourcePack(event.getPlayer());
    }

    @EventHandler
    public void playerResourcePack(PlayerResourcePackStatusEvent e) {
        if (e.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD || e.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
            e.getPlayer().kick(Component.text("A resource pack is required to play on this server."), PlayerKickEvent.Cause.RESOURCE_PACK_REJECTION);
        }
    }

    @EventHandler
    private void onDisable(PluginDisableEvent e) {
        if (JavalinServer.javalin != null) {
            JavalinServer.javalin.stop();
        }
    }
}