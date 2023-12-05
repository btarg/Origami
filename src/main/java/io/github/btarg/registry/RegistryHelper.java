package io.github.btarg.registry;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomBlockDefinition;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.definitions.base.BaseCustomDefinition;
import io.github.btarg.resourcepack.ResourcePackGenerator;
import io.github.btarg.util.ContentPackHelper;
import io.github.btarg.util.NamespacedKeyHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegistryHelper {

    public static ItemStack createCustomItemStack(BaseCustomDefinition definition, int count) {
        if (definition == null) {
            return null;
        }

        Material baseMaterial = (definition instanceof CustomItemDefinition) ? definition.baseMaterial : Material.ITEM_FRAME;

        ItemStack itemStack = new ItemStack(baseMaterial, count);
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            Component name = definition.getDisplayName();
            meta.displayName(name);
            meta.lore(definition.getLore());

            if (definition instanceof CustomItemDefinition customItem) {
                meta.setCustomModelData(ResourcePackGenerator.getItemOverride(customItem));

                for (Map.Entry<Enchantment, Integer> entry : customItem.enchantments.entrySet()) {
                    Enchantment key = entry.getKey();
                    Integer value = entry.getValue();
                    if (key != null) {
                        meta.addEnchant(key, value, true);
                    }
                }
                meta.addItemFlags(customItem.flags.toArray(new ItemFlag[0]));
                //TODO: add attributes here

            } else if (definition instanceof CustomBlockDefinition customBlock) {
                meta.setCustomModelData(ResourcePackGenerator.getBlockOverride(customBlock));
            }

            meta.getPersistentDataContainer().set(
                    (definition instanceof CustomItemDefinition)
                            ? NamespacedKeyHelper.customItemTag
                            : NamespacedKeyHelper.customBlockItemTag,
                    PersistentDataType.STRING,
                    definition.id
            );

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    public static List<BaseCustomDefinition> getBlocksAndItemsWithEvents() {
        return Arrays.stream(ContentPackHelper.getAllContentPacks())
                .flatMap(pack -> Stream.concat(
                        CustomBlockRegistry.getBlockDefinitions(pack.getName()).values().stream(),
                        CustomItemRegistry.getItemDefinitions(pack.getName()).values().stream()
                ))
                // only if it has events
                .filter(BaseCustomDefinition::hasEvents)
                .collect(Collectors.toList());
    }


    public static ItemStack getAnyItemStack(String itemId, int count) {
        if (itemId.startsWith(OrigamiMain.PREFIX) && !itemId.contains("/")) {
            itemId = itemId.replace(OrigamiMain.PREFIX, "");
        }

        ItemStack stack = createCustomItemStack(CustomBlockRegistry.getRegisteredBlock(itemId), count);
        return (stack != null) ? stack : createCustomItemStack(CustomItemRegistry.getRegisteredItem(itemId), count);
    }


}
