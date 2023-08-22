package io.github.btarg.events;


import io.github.btarg.blockdata.CustomBlockDatabase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvents implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CustomBlockDatabase.initWorld(event.getPlayer().getWorld());
    }

}
