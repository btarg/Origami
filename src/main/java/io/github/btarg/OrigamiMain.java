package io.github.btarg;

import io.github.btarg.blockdata.BlockConfig;
import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.commands.RootCommand;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.events.CustomBlockListener;
import io.github.btarg.rendering.BrokenBlocksService;
import io.github.btarg.util.http.ResourcePackServer;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public final class OrigamiMain extends JavaPlugin implements Listener {

    public static final String customBlockIDKey = "custom_block";
    public static BlockConfig blockConfig;
    public static BrokenBlocksService brokenBlocksService;
    public static NamespacedKey customItemTag = null;
    public static FileConfiguration config;

    @Override
    public void onEnable() {

        customItemTag = new NamespacedKey(this, "custom-item");

        ConfigurationSerialization.registerClass(CustomBlockDefinition.class);
        blockConfig = new BlockConfig(this);

        brokenBlocksService = new BrokenBlocksService();

        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(new CustomBlockListener(), this);
        this.getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("origami").setExecutor(new RootCommand());

        // load blocks from files
        blockConfig.loadAndRegisterBlocks();


        // resource pack config
        config = getConfig();
        config.options().setHeader(Arrays.asList("If you choose to enable the internal HTTP server (enable-http-server: true)", "You can set the local path to the resource pack which will be hosted below.", "Set the appropriate SHA-1 and URL (http://localhost:[PORT]) in server.properties"));
        config.addDefault("enable-http-server", false);
        config.addDefault("http-port", 8008);
        config.addDefault("local-resource-pack-path", this.getDataFolder() + File.separator + "pack.zip");

        config.options().copyDefaults(true);
        saveConfig();


        // Start resource pack host server
        if (config.getBoolean("enable-http-server")) {
            Integer port = (Integer) config.get("http-port");
            if (port == null) return;

            try {
                ResourcePackServer.startServer(port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @EventHandler
    public void onWorldLoaded(WorldLoadEvent event) {
        CustomBlockDatabase.initWorld(event.getWorld());
    }

}