package io.github.btarg.definitions;

import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.loot.LootTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class CustomBlockDefinition implements ConfigurationSerializable {

    public String id;
    public Integer blockModelData;
    public Integer blockItemModelData;
    public String displayName;
    public Boolean dropBlock;
    public LootTable breakLootTable;
    public Boolean hasRightClickFunction;
    public List<String> lore;
    public Integer dropExperience;
    public Integer toolLevelRequired;
    public List<String> canBeMinedWith;
    public Double timeToBreak;

    public CustomBlockDefinition(Map<String, Object> map) {
        this.id = null;
        this.blockModelData = (Integer) map.get("blockModelData");
        this.blockItemModelData = (Integer) map.get("blockItemModelData");
        this.displayName = (String) map.get("displayName");
        this.dropBlock = (Boolean) map.get("dropBlock");
        this.breakLootTable = null;
        this.hasRightClickFunction = (Boolean) map.get("hasRightClickFunction");
        this.lore = (List<String>) map.get("lore");
        this.dropExperience = (Integer) map.get("dropExperience");
        this.toolLevelRequired = (Integer) map.get("toolLevelRequired");
        this.canBeMinedWith = (List<String>) map.get("canBeMinedWith");
        this.timeToBreak = (Double) Double.valueOf(map.get("timeToBreak").toString()); //wtf is this
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
        map.put("blockItemModelData", this.blockItemModelData);
        map.put("displayName", this.displayName);
        map.put("dropBlock", this.dropBlock);
        map.put("hasRightClickFunction", this.hasRightClickFunction);
        map.put("lore", this.lore);
        map.put("dropExperience", this.dropExperience);
        map.put("toolLevelRequired", this.toolLevelRequired);
        map.put("canBeMinedWith", this.canBeMinedWith);
        map.put("timeToBreak", this.timeToBreak);

        return map;
    }

}
