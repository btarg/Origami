package io.github.btarg.util.items;

import org.bukkit.Bukkit;

import java.util.List;

public class CommandRunner {
    public static void runCommands(List<String> commands, String playerSelector) {
        for (String command : commands) {
            if (command.startsWith("/")) {
                command = command.substring(1);
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute as " + playerSelector + " run " + command);
        }
    }
}
