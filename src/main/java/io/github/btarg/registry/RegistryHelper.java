package io.github.btarg.registry;

import io.github.btarg.OrigamiMain;
import io.github.btarg.definitions.CustomDefinition;
import io.github.btarg.definitions.CustomItemDefinition;
import io.github.btarg.resourcepack.ResourcePackGenerator;
import io.github.btarg.util.NamespacedKeyHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class RegistryHelper {
    
    public static ItemStack createCustomItemStack(CustomDefinition definition, int count) {
        if (definition == null) {
            return null;
        }

        Material baseMaterial = (definition instanceof CustomItemDefinition)
                ? definition.baseMaterial
                : Material.ITEM_FRAME;

        ItemStack itemStack = new ItemStack(baseMaterial, count);
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            Component name = definition.getDisplayName();
            meta.displayName(name);
            meta.lore(definition.getLore());
            meta.setCustomModelData(ResourcePackGenerator.getOverrideByModelName(definition.model));

            if (definition instanceof CustomItemDefinition customItem) {
                for (Map.Entry<Enchantment, Integer> entry : customItem.enchantments.entrySet()) {
                    Enchantment key = entry.getKey();
                    Integer value = entry.getValue();
                    if (key != null) {
                        meta.addEnchant(key, value, true);
                    }
                }
                meta.addItemFlags(customItem.flags.toArray(new ItemFlag[0]));
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

    public static ItemStack getAnyItemStack(String itemId, int count) {
        itemId = itemId.substring(OrigamiMain.PREFIX.length());
        ItemStack stack = createCustomItemStack(CustomBlockRegistry.GetRegisteredBlock(itemId), count);
        if (stack == null) {
            stack = createCustomItemStack(CustomItemRegistry.GetRegisteredItem(itemId), count);
        }
        return stack;
    }

}
