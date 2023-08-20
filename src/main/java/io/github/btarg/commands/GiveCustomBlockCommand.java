package io.github.btarg.commands;

import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.registry.CustomBlockRegistry;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GiveCustomBlockCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        int count = 1;
        if (args.length == 3) {
            count = Integer.parseInt(args[2]);
        }

        if (args.length >= 2) {
            Player target = Bukkit.getPlayer(args[0]);
            String blockId = args[1];
            CustomBlockDefinition definition = CustomBlockRegistry.GetRegisteredBlock(blockId);

            if (definition != null && target != null) {
                target.getInventory().addItem(CustomBlockRegistry.CreateCustomBlockItemStack(definition, count));

                TranslatableComponent giveMessage = new TranslatableComponent("commands.give.success.single");
                giveMessage.addWith(String.valueOf(count));


                giveMessage.addWith(definition.displayName);

                TextComponent username = new TextComponent(target.getDisplayName());
                giveMessage.addWith(username);

                sender.spigot().sendMessage(giveMessage);

                return true;
            }


        } else {
            sender.sendMessage(ChatColor.RED + "Incorrect arguments. Usage:\n" + command.getUsage());
        }


        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
            Bukkit.getServer().getOnlinePlayers().toArray(players);
            for (int i = 0; i < players.length; i++) {
                playerNames.add(players[i].getName());
            }

            return playerNames;
        } else if (args.length == 2) {
            return CustomBlockRegistry.GetBlockIDs();
        } else if (args.length == 3) {
            return Collections.singletonList("64");
        } else {
            return Collections.emptyList();
        }

    }
}