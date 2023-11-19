package io.github.btarg;

import io.github.btarg.commands.RootCommand;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.definitions.CustomRecipeDefinition;
import io.github.btarg.events.BlockDamageListener;
import io.github.btarg.events.CustomBlockListener;
import io.github.btarg.events.CustomItemListener;
import io.github.btarg.events.ResourcePackListener;
import io.github.btarg.rendering.BrokenBlocksService;
import io.github.btarg.resourcepack.ResourcePackGenerator;
import io.github.btarg.serialization.DefinitionSerializer;
import io.github.btarg.util.NamespacedKeyHelper;
import io.github.btarg.util.items.CooldownManager;
import io.github.btarg.util.loot.LootTableHelper;
import io.github.btarg.util.loot.versions.LootTableHelper_1_20_R2;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class OrigamiMain extends JavaPlugin implements Listener {

    public static FileConfiguration config;
    public static DefinitionSerializer definitionSerializer;
    public static BrokenBlocksService brokenBlocksService;
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
        NamespacedKeyHelper.init(this);

        if (!setupNMS()) {
            Bukkit.getLogger().severe("This version of Paper is unsupported! See the Origami Docs for a list of supported versions, or contact the developer.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        initConfig();
        brokenBlocksService = new BrokenBlocksService();

        this.getServer().getPluginManager().registerEvents(new ResourcePackListener(), this);
        this.getServer().getPluginManager().registerEvents(new CustomBlockListener(), this);
        this.getServer().getPluginManager().registerEvents(new BlockDamageListener(), this);
        this.getServer().getPluginManager().registerEvents(new CustomItemListener(), this);
        this.getServer().getPluginManager().registerEvents(new CooldownManager(), this);
        this.getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("origami")).setExecutor(new RootCommand());

        if (config.getBoolean("resource-packs.generate-resource-pack")) {
            // Generate resource pack and serve with http
            try {
                ResourcePackListener.serveResourcePack(ResourcePackGenerator.generateResourcePack());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private boolean setupNMS() {

        if (Bukkit.getServer().getMinecraftVersion().equals("1.20.2")) {
            lootTableHelper = new LootTableHelper_1_20_R2();
        } else {
            return false;
        }
        return true;
    }

    private void initConfig() {
        ConfigurationSerialization.registerClass(CustomBlockDefinition.class);
        ConfigurationSerialization.registerClass(CustomRecipeDefinition.class);

        config = getConfig();
        config.options().setHeader(Arrays.asList("If you choose to enable the internal HTTP server (enable-http-server: true),", "you can set the local path to the resource pack which will be hosted below.", "If you enable resource pack generation, the unzipped directory specified below will be zipped and combined with any existing resource pack, otherwise the zipped resource pack path is where you should place your pack.", "Remember to set your server's IP address in server.properties"));
        definitionSerializer = new DefinitionSerializer();


        ConfigurationSection resourcePackSection = config.createSection("resource-packs");
        resourcePackSection.addDefault("enable-http-server", true);
        resourcePackSection.addDefault("http-port", 8008);
        resourcePackSection.addDefault("generate-resource-pack", true);
        config.options().copyDefaults(true);
        saveConfig();

        // load blocks from files (requires config loaded)
        definitionSerializer.loadAndRegister(CustomBlockDefinition.class);
        // items
        definitionSerializer.loadAndRegister(CustomItemDefinition.class);
        // recipes
        definitionSerializer.loadAndRegister(CustomRecipeDefinition.class);

    }
}