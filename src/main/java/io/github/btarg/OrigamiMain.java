package io.github.btarg;

import io.github.btarg.blockdata.CustomBlockDatabase;
import io.github.btarg.commands.RootCommand;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.CustomRecipeDefinition;
import io.github.btarg.events.CustomBlockListener;
import io.github.btarg.rendering.BrokenBlocksService;
import io.github.btarg.resourcepack.FileUtils;
import io.github.btarg.resourcepack.ResourcePackGenerator;
import io.github.btarg.resourcepack.ResourcePackServer;
import io.github.btarg.serialization.BlockConfig;
import io.github.btarg.serialization.RecipeConfig;
import io.github.btarg.util.loot.LootTableHelper;
import io.github.btarg.util.loot.versions.LootTableHelper_1_20_R1;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("deprecation")
public final class OrigamiMain extends JavaPlugin implements Listener {

    public static FileConfiguration config;
    public static BlockConfig blockConfig;
    public static RecipeConfig recipeConfig;
    public static BrokenBlocksService brokenBlocksService;
    public static NamespacedKey customItemTag = null;
    public static String sversion;
    @Getter
    private static OrigamiMain Instance;
    @Getter
    private static LootTableHelper lootTableHelper;

    @Getter
    @Setter
    private static boolean isHostingPack;

    @Override
    public void onEnable() {

        Instance = this;
        if (!setupNMS()) {
            Bukkit.getLogger().severe("This version of Paper is unsupported! See the Origami Docs for a list of supported versions, or contact the developer.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        initConfig();
        customItemTag = new NamespacedKey(this, "custom-item");
        brokenBlocksService = new BrokenBlocksService();

        this.getServer().getPluginManager().registerEvents(new CustomBlockListener(), this);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("origami").setExecutor(new RootCommand());

        // Generate resource pack
        ResourcePackGenerator.CreateZipFile(this::serveResourcePack);


    }

    private boolean setupNMS() {
        try {
            sversion = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
        if (sversion.equals("v1_20_R1")) {
            lootTableHelper = new LootTableHelper_1_20_R1();
        }
        return lootTableHelper != null;
    }

    private void initConfig() {
        ConfigurationSerialization.registerClass(CustomBlockDefinition.class);
        ConfigurationSerialization.registerClass(CustomRecipeDefinition.class);
        blockConfig = new BlockConfig();
        recipeConfig = new RecipeConfig();

        config = getConfig();
        config.options().setHeader(Arrays.asList("If you choose to enable the internal HTTP server (enable-http-server: true),", "you can set the local path to the resource pack which will be hosted below.", "If you enable resource pack generation, the unzipped directory specified below will be zipped and combined with any existing resource pack, otherwise the zipped resource pack path is where you should place your pack.", "Remember to set your server's IP address in server.properties"));

        ConfigurationSection resourcePackSection = config.createSection("resource-packs");
        resourcePackSection.addDefault("enable-http-server", true);
        resourcePackSection.addDefault("http-port", 8008);
        resourcePackSection.addDefault("generate-resource-pack", true);
        resourcePackSection.addDefault("zipped-resource-pack-path", this.getDataFolder() + File.separator + "pack.zip");
        resourcePackSection.addDefault("unzipped-resource-pack-path", this.getDataFolder() + File.separator + "resources");

        ConfigurationSection itemSection = config.createSection("custom-items");
        itemSection.addDefault("prefix", "origami");
        ConfigurationSection blockSection = config.createSection("custom-blocks");
        blockSection.addDefault("save-cooldown-seconds", 3);

        config.options().copyDefaults(true);
        saveConfig();

        // load blocks from files (requires config loaded)
        blockConfig.loadAndRegisterBlocks();
        recipeConfig.loadAndRegisterRecipes();
    }

    private void serveResourcePack() {
        // Start resource pack host server
        if (config.getBoolean("resource-packs.enable-http-server")) {
            Integer port = (Integer) config.get("resource-packs.http-port");
            if (port == null) return;

            Bukkit.getLogger().info("Hosting resource pack on port " + port);
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

    @EventHandler(priority = EventPriority.HIGHEST)
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

    @EventHandler
    public void playerResourcePack(PlayerResourcePackStatusEvent e) {

        if (!config.getBoolean("resource-packs.generate-resource-pack")) return;

        if (!(ResourcePackGenerator.isReady() && isHostingPack)) {
            e.getPlayer().kick(Component.text("The server hasn't finished loading yet!\nTry again in a few seconds."), PlayerKickEvent.Cause.PLUGIN);
            return;
        }
        if (e.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD || e.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
            e.getPlayer().kick(Component.text("A resource pack is required to play on this server."), PlayerKickEvent.Cause.RESOURCE_PACK_REJECTION);
        }

    }

    @Override
    public void onDisable() {
        CustomBlockDatabase.saveAllNow();
    }
}