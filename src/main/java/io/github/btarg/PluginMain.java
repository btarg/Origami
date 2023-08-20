package io.github.btarg;

import io.github.btarg.blockdata.BlockConfig;
import io.github.btarg.commands.DBListCommand;
import io.github.btarg.commands.GiveCustomBlockCommand;
import io.github.btarg.commands.ReloadConfigCommand;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.events.CustomBlocksEvents;
import io.github.btarg.events.PlayerEvents;
import io.github.btarg.loot.RainbowBlockLootTable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;

public final class PluginMain extends JavaPlugin {

    public static final String customBlockIDKey = "custom_block";
    public static BlockConfig blockConfig;

    @Override
    public void onEnable() {

        ConfigurationSerialization.registerClass(CustomBlockDefinition.class);
        blockConfig = new BlockConfig(this);

        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(new CustomBlocksEvents(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

        this.getCommand("givecustom").setExecutor(new GiveCustomBlockCommand());
        this.getCommand("dblist").setExecutor(new DBListCommand());
        this.getCommand("reloadcustom").setExecutor(new ReloadConfigCommand());

        // debug example object
        CustomBlockDefinition definition = new CustomBlockDefinition(new HashMap<>());
        definition.id = "rainbow_block";
        definition.blockModelData = 4;
        definition.modelDataWhenInFrame = 3;
        definition.interactable = false;
        definition.displayName = "§cR§6a§ei§an§9b§bo§5w §6B§el§ao§9c§bk";
        definition.lore = Collections.singletonList("It shimmers beautifully in the sunlight.");
        definition.breakLootTable = new RainbowBlockLootTable();
        definition.dropExperience = 0;
        // save to file
        blockConfig.saveBlockDefinitionToFile(definition);

        // load blocks from files
        blockConfig.loadAndRegisterBlocks();


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}