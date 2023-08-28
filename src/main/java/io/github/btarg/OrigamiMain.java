package io.github.btarg;

import io.github.btarg.blockdata.BlockConfig;
import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.commands.RootCommand;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.events.CustomBlockListener;
import io.github.btarg.rendering.BrokenBlocksService;
import io.github.btarg.resourcepack.FileUtils;
import io.github.btarg.resourcepack.ResourcePackServer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("deprecation")
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

        // resource pack config
        config = getConfig();

        config.options().setHeader(Arrays.asList("If you choose to enable the internal HTTP server (enable-http-server: true),", "you can set the local path to the resource pack which will be hosted below.", "Remember to set your server's IP address in server.properties"));
        config.addDefault("enable-http-server", false);
        config.addDefault("http-port", 8008);
        config.addDefault("local-resource-pack-path", this.getDataFolder() + File.separator + "pack.zip");

        ConfigurationSection blockSection = config.createSection("custom-blocks");
        blockSection.addDefault("save-cooldown-seconds", 3);

        config.options().copyDefaults(true);
        saveConfig();


        // load blocks from files (requires config loaded)
        blockConfig.loadAndRegisterBlocks();

        // Start resource pack host server
        if (config.getBoolean("enable-http-server")) {
            Integer port = (Integer) config.get("http-port");
            if (port == null) return;

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

                        try {
                            ResourcePackServer.startServer(port);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        // init world here to make sure we have fully loaded plugin
        CustomBlockDatabase.initWorld(event.getPlayer().getWorld());

        String ipAddress = StringUtils.defaultIfEmpty(getServer().getIp(), "localhost");

        Integer port = Objects.requireNonNullElse((Integer) config.get("http-port"), 8008);

        try {
            String sha1 = FileUtils.currentSHA1();
            if (sha1.isEmpty()) {
                getLogger().warning("No hash generated!");
                event.getPlayer().setResourcePack("http://" + ipAddress + "/pack.zip");
            } else {
                getLogger().info("Resource pack hash: " + sha1);
                event.getPlayer().setResourcePack("http://" + ipAddress + ":" + port + "/pack.zip", sha1);
            }
        } catch (Exception e) {
            getLogger().severe(e.getMessage());
        }

    }

    @Override
    public void onDisable() {
        CustomBlockDatabase.onDisable();
    }
}