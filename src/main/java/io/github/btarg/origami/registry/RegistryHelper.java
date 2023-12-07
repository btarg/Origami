package io.github.btarg.origami.registry;

import io.github.btarg.origami.OrigamiMain;
import io.github.btarg.origami.definitions.CustomBlockDefinition;
import io.github.btarg.origami.definitions.CustomItemDefinition;
import io.github.btarg.origami.definitions.base.BaseCustomDefinition;
import io.github.btarg.origami.resourcepack.ResourcePackGenerator;
import io.github.btarg.origami.util.NamespacedKeyHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

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

    public static CustomItemDefinition getDefinitionFromItemstack(ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta.isUnbreakable()) return null;
        String customItemString = meta.getPersistentDataContainer().get(NamespacedKeyHelper.customItemTag, PersistentDataType.STRING);
        if (customItemString == null || customItemString.isEmpty()) return null;
        return CustomItemRegistry.getRegisteredItem(customItemString);
    }

    public static ItemStack getAnyItemStack(String itemId, int count) {
        if (itemId.startsWith(OrigamiMain.PREFIX) && !itemId.contains("/")) {
            itemId = itemId.replace(OrigamiMain.PREFIX, "");
        }

        ItemStack stack = createCustomItemStack(CustomBlockRegistry.getRegisteredBlock(itemId), count);
        return (stack != null) ? stack : createCustomItemStack(CustomItemRegistry.getRegisteredItem(itemId), count);
    }
}