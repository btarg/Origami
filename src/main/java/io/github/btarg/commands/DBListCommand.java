package io.github.btarg.commands;

import io.github.btarg.blockdata.CustomBlockDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBListCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        World world = null;

        if (sender instanceof Player && args.length == 0) {
            world = ((Player) sender).getWorld();
        } else if (args.length == 1) {
            world = sender.getServer().getWorld(args[0]);
        }
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "Could not get world. Usage:\n" + command.getUsage());
            return true;
        }

        World.Environment environment = world.getEnvironment();

        StringBuilder finalString = new StringBuilder(" blocks in " + environment.name() + ":\n");
        HashMap<Vector, String> currentDB = CustomBlockDatabase.getBlocksInDatabase(world);

        if (!currentDB.isEmpty()) {
            int count = 0;
            for (Map.Entry<Vector, String> blockEntry : currentDB.entrySet()) {
                finalString.append(String.format("§r  * %s %s[%s]\n", blockEntry.getValue(), ChatColor.GREEN, blockEntry.getKey().toString()));
                count++;
            }
            sender.sendMessage(String.valueOf(count) + finalString);

        } else {
            sender.sendMessage("No blocks in database!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> worldNames = new ArrayList<>();

        if (args.length == 1) {
            Bukkit.getWorlds().forEach(world -> {
                worldNames.add(world.getName());
            });

        }
        return worldNames;
    }
}