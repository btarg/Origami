package io.github.btarg.commands;

import io.github.btarg.PluginMain;
import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class RootCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (Objects.equals(args[0], "give")) {
            int count = 1;
            if (args.length == 4) {
                count = Integer.parseInt(args[3]);
            }

            if (args.length >= 3) {
                Player target = Bukkit.getPlayer(args[1]);
                String blockId = args[2];
                CustomBlockDefinition definition = CustomBlockRegistry.GetRegisteredBlock(blockId);

                if (definition != null && target != null) {
                    target.getInventory().addItem(CustomBlockRegistry.CreateCustomBlockItemStack(definition, count));

                    TranslatableComponent giveMessage = new TranslatableComponent("commands.give.success.single");
                    giveMessage.addWith(String.valueOf(count));


                    giveMessage.addWith(definition.displayName);

                    TextComponent username = new TextComponent(target.getDisplayName());
                    giveMessage.addWith(username);

                    sender.spigot().sendMessage(giveMessage);
                }

            } else {
                sender.sendMessage(ChatColor.RED + "Incorrect arguments. Usage:\n" + command.getUsage());
            }
        } else if (Objects.equals(args[0], "reload")) {

            if (Objects.equals(args[1], "blocks")) {
                sender.sendMessage("Reloading custom blocks...");
                CustomBlockRegistry.ClearBlockRegistry();
                PluginMain.blockConfig.loadAndRegisterBlocks(sender);
            } else if (Objects.equals(args[1], "items")) {
                sender.sendMessage("coming soon");

            } else {
                sender.sendMessage(ChatColor.RED + "Please specify which registry you want to reload.");
            }
        } else if (Objects.equals(args[0], "listblocks")) {

            World world = null;

            if (sender instanceof Player && args.length == 2) {
                world = ((Player) sender).getWorld();
            } else if (args.length == 3) {
                world = sender.getServer().getWorld(args[1]);
            }
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Could not get world. Usage:\n" + command.getUsage());
                return true;
            }

            World.Environment environment = world.getEnvironment();

            StringBuilder finalString = new StringBuilder(" blocks in " + environment.name() + ":\n");
            HashMap<org.bukkit.util.Vector, String> currentDB = CustomBlockDatabase.getBlocksInDatabase(world);

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

        } else {
            sender.sendMessage(ChatColor.RED + "Incorrect arguments. Usage:\n" + command.getUsage());
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tabComplete = new ArrayList<>();
        if (args.length == 1) return Arrays.asList("give", "reload", "listblocks");

        if (Objects.equals(args[0], "give")) {

            if (args.length == 2) {

                Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
                Bukkit.getServer().getOnlinePlayers().toArray(players);
                for (Player player : players) {
                    tabComplete.add(player.getName());
                }

            } else if (args.length == 3) {
                tabComplete = CustomBlockRegistry.GetBlockIDs();
            } else if (args.length == 4) {
                tabComplete = Collections.singletonList("64");
            }

        } else if (Objects.equals(args[0], "reload")) {
            tabComplete = Arrays.asList("blocks", "items");
        } else if (Objects.equals(args[0], "listblocks")) {
            List<String> worldNames = new ArrayList<>();
            Bukkit.getWorlds().forEach(world -> {
                worldNames.add(world.getName());
            });
            tabComplete = worldNames;
        }

        return tabComplete;

    }
}