package io.github.btarg.commands;

import io.github.btarg.PluginMain;
import io.github.btarg.registry.CustomBlockRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ReloadConfigCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {
            if (Objects.equals(args[0], "blocks")) {
                sender.sendMessage("Reloading custom blocks...");
                CustomBlockRegistry.ClearBlockRegistry();
                PluginMain.blockConfig.loadAndRegisterBlocks(sender);
            } else if (Objects.equals(args[0], "items")) {
                sender.sendMessage("coming soon");


            } else {
                sender.sendMessage(ChatColor.RED + "Please specify which registry you want to reload.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Command usage:\n" + command.getUsage());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

        if (args.length == 1) {
            return Arrays.asList("blocks", "items");
        }

        return null;
    }
}