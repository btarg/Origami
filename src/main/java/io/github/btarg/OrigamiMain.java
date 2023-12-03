package io.github.btarg;

import io.github.btarg.commands.KillInterceptor;
import io.github.btarg.commands.RootCommand;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.definitions.CustomRecipeDefinition;
import io.github.btarg.listeners.ResourcePackListener;
import io.github.btarg.listeners.blocks.BlockDamageListener;
import io.github.btarg.listeners.blocks.CustomBlockListener;
import io.github.btarg.listeners.items.CustomItemListener;
import io.github.btarg.listeners.items.DurabilityListener;
import io.github.btarg.rendering.BrokenBlocksService;
import io.github.btarg.resourcepack.ResourcePackGenerator;
import io.github.btarg.serialization.DefinitionSerializer;
import io.github.btarg.util.NamespacedKeyHelper;
import io.github.btarg.util.items.CooldownManager;
import io.github.btarg.util.loot.LootTableHelper;
import io.github.btarg.util.loot.versions.LootTableHelper_1_20_R2;
import io.github.btarg.web.JavalinServer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class OrigamiMain extends JavaPlugin implements Listener {

    public static final String PREFIX = "origami:";
    public static FileConfiguration config;
    public static BrokenBlocksService brokenBlocksService;
    @Getter
    private static DefinitionSerializer definitionSerializer;
    @Getter
    private static OrigamiMain Instance;
    @Getter
    private static LootTableHelper lootTableHelper;

    @Override
    public void onEnable() {

        Instance = this;
        NamespacedKeyHelper.init(this);

        if (!setupNMS()) {
            Bukkit.getLogger().severe("This version of Paper is unsupported! See the Origami Docs for a list of supported versions, or contact the developer.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            initConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        brokenBlocksService = new BrokenBlocksService();

        PluginManager pluginManager = this.getServer().getPluginManager();

        pluginManager.registerEvents(new ResourcePackListener(), this);
        pluginManager.registerEvents(new CustomBlockListener(), this);
        pluginManager.registerEvents(new BlockDamageListener(), this);
        pluginManager.registerEvents(new CustomItemListener(), this);
        pluginManager.registerEvents(new DurabilityListener(), this);

        pluginManager.registerEvents(new CooldownManager(), this);
        pluginManager.registerEvents(new KillInterceptor(), this);
        pluginManager.registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("origami")).setExecutor(new RootCommand());

        // Generate resource pack and serve with http
        try {
            JavalinServer.initAndServePack(ResourcePackGenerator.generateResourcePack());
        } catch (IOException e) {
            e.printStackTrace();
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

    private void initConfig() throws IOException {
        ConfigurationSerialization.registerClass(CustomBlockDefinition.class);
        ConfigurationSerialization.registerClass(CustomRecipeDefinition.class);

        config = getConfig();
        config.options().setHeader(List.of("Remember to set your server's IP address properly in server.properties!"));
        definitionSerializer = new DefinitionSerializer();

        ConfigurationSection resourcePackSection = config.createSection("resource-pack");
        resourcePackSection.addDefault("http-port", 8008);
        resourcePackSection.addDefault("pack-description", "Powered by <color:#f51d5e>Origami</color>");
        resourcePackSection.addDefault("save-generated-pack", false);
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