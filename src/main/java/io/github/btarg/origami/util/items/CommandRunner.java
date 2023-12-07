package io.github.btarg.origami.util.items;

import org.bukkit.Bukkit;

import java.util.List;

public class CommandRunner {
    public static void runCommands(List<String> commands, String playerSelector) {
        String prefix = playerSelector != null ? "execute as " + playerSelector + " run " : "";

        commands.forEach(command -> {
            if (command.startsWith("/")) {
                command = command.substring(1);
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), prefix + command);
        });
    }
}
