package io.github.btarg.definitions;

import java.util.Collections;
import java.util.HashMap;

public class DefaultDefinitions {

    public static CustomBlockDefinition getDefaultBlockDefinition() {
        // save example object if there are no files
        CustomBlockDefinition definition = new CustomBlockDefinition(new HashMap<>());
        definition.id = "rainbow_block";
        definition.blockModelData = 4;
        definition.blockItemModelData = 3;
        definition.hasRightClickFunction = false;
        definition.displayName = "§cR§6a§ei§an§9b§bo§5w §6B§el§ao§9c§bk";
        definition.lore = Collections.singletonList("It shimmers beautifully in the sunlight.");
        definition.dropExperience = 0;
        definition.toolLevelRequired = 3;
        definition.canBeMinedWith = Collections.singletonList("pickaxes");
        definition.timeToBreak = 20d;
        return definition;
    }

}
