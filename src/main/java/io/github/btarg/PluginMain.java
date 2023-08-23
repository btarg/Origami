package io.github.btarg;

import io.github.btarg.blockdata.BlockConfig;
import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.commands.RootCommand;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.events.CustomBlockListener;
import io.github.btarg.rendering.BrokenBlocksService;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginMain extends JavaPlugin implements Listener {

    public static final String customBlockIDKey = "custom_block";
    public static BlockConfig blockConfig;
    public static BrokenBlocksService brokenBlocksService;
    public static NamespacedKey customItemTag = null;

    @Override
    public void onEnable() {

        customItemTag = new NamespacedKey(this, "custom-item");

        ConfigurationSerialization.registerClass(CustomBlockDefinition.class);
        blockConfig = new BlockConfig(this);

        brokenBlocksService = new BrokenBlocksService();

        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(new CustomBlockListener(), this);
        this.getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("customcontent").setExecutor(new RootCommand());

        // load blocks from files
        blockConfig.loadAndRegisterBlocks();

    }

    @EventHandler
    public void onWorldLoaded(WorldLoadEvent event) {
        CustomBlockDatabase.initWorld(event.getWorld());
    }

}