package io.github.btarg.commands;

import io.github.btarg.OrigamiMain;
import io.github.btarg.blockdata.CustomBlockPersistentData;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.definitions.CustomRecipeDefinition;
import io.github.btarg.events.ResourcePackListener;
import io.github.btarg.registry.CustomBlockRegistry;
import io.github.btarg.registry.CustomItemRegistry;
import io.github.btarg.registry.CustomRecipeRegistry;
import io.github.btarg.registry.RegistryHelper;
import io.github.btarg.resourcepack.ResourcePackGenerator;
import io.github.btarg.util.blocks.BlockPos;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings("deprecation")
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
                String itemId = args[2];
                if (target != null) {
                    ItemStack stack = RegistryHelper.getAnyItemStack(itemId, count);
                    if (stack == null) return false;
                    target.getInventory().addItem(stack);
                    Component giveMessage = Component.translatable("commands.give.success.single", Component.text(count), stack.displayName(), target.displayName());
                    sender.sendMessage(giveMessage);
                }

            } else {
                sender.sendMessage(ChatColor.RED + "Incorrect arguments. Usage:\n" + command.getUsage());
            }
        } else if (Objects.equals(args[0], "reload")) {

            if (StringUtils.equalsAny(args[1], "blocks", "all")) {
                sender.sendMessage("Reloading custom blocks...");
                CustomBlockRegistry.ClearBlockRegistry();
                OrigamiMain.definitionSerializer.loadAndRegister(sender, CustomBlockDefinition.class);
            }
            if (StringUtils.equalsAny(args[1], "items", "all")) {
                sender.sendMessage("Reloading custom items...");
                CustomItemRegistry.ClearItemRegistry();
                OrigamiMain.definitionSerializer.loadAndRegister(sender, CustomItemDefinition.class);
            }
            if (StringUtils.equalsAny(args[1], "recipes", "all")) {
                sender.sendMessage("Reloading custom recipes...");
                CustomRecipeRegistry.ClearRecipeRegistry();
                OrigamiMain.definitionSerializer.loadAndRegister(sender, CustomRecipeDefinition.class);
            }
            if (StringUtils.equalsAny(args[1], "resources", "all")) {
                sender.sendMessage("Reloading resource pack...");
                // Generate resource pack and serve with http
                ResourcePackListener.serveResourcePack(ResourcePackGenerator.generateResourcePack());
                Bukkit.getServer().getOnlinePlayers().forEach(ResourcePackListener::sendResourcePack);
            }
        } else if (Objects.equals(args[0], "listblocks")) {

            if (sender instanceof Player) {
                Chunk chunk = ((Player) sender).getLocation().getChunk();

                StringBuilder finalString = new StringBuilder(" blocks in " + chunk + ":\n");
                Map<BlockPos, String> currentDB = CustomBlockPersistentData.getBlocksInStorage(chunk);

                if (currentDB != null && !currentDB.isEmpty()) {
                    int count = 0;
                    for (Map.Entry<BlockPos, String> blockEntry : currentDB.entrySet()) {
                        finalString.append(String.format("  * %s %s[%s]\n", blockEntry.getValue(), ChatColor.GREEN, blockEntry.getKey().toString()));
                        count++;
                    }
                    sender.sendMessage(String.valueOf(count) + finalString);

                } else {
                    sender.sendMessage("No blocks in this chunk!");
                }
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

            } else if (args.length == 3 && !args[1].isEmpty()) {
                tabComplete = CustomBlockRegistry.GetBlockIDs();
                tabComplete.addAll(CustomItemRegistry.GetItemIds());
            } else if (args.length == 4 && !args[2].isEmpty()) {
                tabComplete = Collections.singletonList("64");
            }

        } else if (Objects.equals(args[0], "reload")) {
            tabComplete = Arrays.asList("all", "blocks", "items", "recipes", "resources");
        }

        return tabComplete;

    }
}