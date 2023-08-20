package io.github.btarg.definitions;

import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.loot.LootTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBlockDefinition implements ConfigurationSerializable {

    public String id = null;
    public Integer blockModelData = 0;
    public Integer modelDataWhenInFrame = 0;
    public String displayName = null;
    public LootTable breakLootTable = null;
    public Boolean interactable = false;
    public List<String> lore = new ArrayList<>();
    public Integer dropExperience = 0;
    public Integer toolLevelRequired = 0;

    public CustomBlockDefinition(Map<String, Object> map) {
        this.id = null;
        this.blockModelData = (Integer) map.get("blockModelData");
        this.modelDataWhenInFrame = (Integer) map.get("modelDataWhenInFrame");
        this.displayName = (String) map.get("displayName");
        this.breakLootTable = null;
        this.interactable = (Boolean) map.get("interactable");
        this.lore = (List<String>) map.get("lore");
        this.dropExperience = (Integer) map.get("dropExperience");
        this.toolLevelRequired = (Integer) map.get("toolLevelRequired");
    }

    public static CustomBlockDefinition deserialize(Map<String, Object> map) {
        return new CustomBlockDefinition(map);
    }

    public TranslatableComponent getDisplayName() {
        return new TranslatableComponent(displayName);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put("blockModelData", this.blockModelData);
        map.put("modelDataWhenInFrame", this.modelDataWhenInFrame);

        map.put("displayName", this.displayName);
        map.put("interactable", this.interactable);
        map.put("lore", this.lore);
        map.put("dropExperience", this.dropExperience);
        map.put("toolLevelRequired", this.toolLevelRequired);

        return map;
    }

}
