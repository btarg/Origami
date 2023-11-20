package io.github.btarg.commands;

import io.github.btarg.OrigamiMain;
import io.github.btarg.blockdata.CustomBlockPersistentData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillInterceptor implements Listener {

    private static String tempCommand = "";

    private static String extractSelector(String command) {
        String regex = "@[a-zA-Z_]+\\[[^\\]]*\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);

        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().contains("/kill") || event.getMessage().contains("/tp")) {

            String selector = extractSelector(event.getMessage());
            if (selector.isBlank()) return;
            List<Entity> entities = Bukkit.getServer().selectEntities(Bukkit.getServer().getConsoleSender(), selector);
            for (Entity entity : entities) {

                if (!Objects.equals(tempCommand, event.getMessage())) {
                    tempCommand = event.getMessage();
                    event.setCancelled(true);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            tempCommand = "";
                        }
                    }.runTaskLater(OrigamiMain.getInstance(), 60); // reset last command after a few seconds

                } else {
                    tempCommand = "";
                    return;
                }

                if (CustomBlockPersistentData.getBlockInformation(entity.getChunk()).getBlocksMap().containsValue(entity.getUniqueId().toString())) {
                    Component component = MiniMessage.miniMessage().deserialize(
                            "<dark_red><b>WARNING:</b></dark_red> <red>Killing or teleporting Item Displays will break Origami custom blocks placed in your world. Type the command again only if you understand the implications of this!</red>"
                    );
                    event.getPlayer().sendMessage(component);
                }
            }
        }
    }
}
