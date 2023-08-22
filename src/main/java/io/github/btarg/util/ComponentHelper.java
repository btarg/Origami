package io.github.btarg.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ComponentHelper {

    public static Component deserializeGenericComponent(String input) {

        Component textComponent = Component.empty();
        // if there are any colour codes in the string they will use the section char
        input = ChatColor.translateAlternateColorCodes('&', input);

        if (input.contains(String.valueOf(LegacyComponentSerializer.SECTION_CHAR))) {
            // input is a legacy string, convert it
            textComponent = LegacyComponentSerializer.legacySection().deserialize(input);
        } else {
            try {
                textComponent = MiniMessage.miniMessage().deserialize(input);
            } catch (Exception e) {
                Bukkit.getLogger().warning(e.getMessage());
            }
        }
        return textComponent;
    }

    public static Component removeItalicsIfAbsent(Component input) {
        return input.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

}
