package io.github.btarg.definitions;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DefaultDefinitions {

    public static CustomBlockDefinition getDefaultBlockDefinition() {
        // save example object if there are no files
        CustomBlockDefinition definition = new CustomBlockDefinition(new HashMap<>());
        definition.id = "rainbow_block";
        definition.baseBlock = Material.GLASS;
        definition.glowing = false;
        definition.blockItemModelData = 4;
        definition.blockModelData = 3;
        definition.displayName = "&cR&6a&ei&an&9b&bo&5w &6B&el&ao&9c&bk";
        definition.rightClickCommands = Collections.singletonList("tellraw @s {\"text\":\"The block reverberates majestically.\",\"italic\":true,\"color\":\"gray\"}");
        definition.lore = Collections.singletonList("<rainbow>It shimmers beautifully in the sunlight.</rainbow>");
        definition.drops = List.of(new ItemStack(Material.DIAMOND, 1));
        definition.isAffectedByFortune = true;
        definition.dropExperience = 0;
        definition.toolLevelRequired = 2;
        definition.canBeMinedWith = Collections.singletonList("pickaxes");
        definition.timeToBreak = 40;
        definition.breakSound = Sound.BLOCK_AMETHYST_BLOCK_BREAK.toString();
        definition.placeSound = Sound.BLOCK_AMETHYST_BLOCK_PLACE.toString();
        return definition;
    }

}
