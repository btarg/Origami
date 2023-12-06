package io.github.btarg.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@SuppressWarnings("deprecation")
public class ComponentHelper {

    public static Component deserializeGenericComponent(String input) {

        Component textComponent = Component.empty();

        try {
            // allow for display name to be set in resource pack
            TranslatableComponent translated = new TranslatableComponent(input);
            if (!translated.toPlainText().equals(input)) {
                input = translated.toPlainText();
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

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

    public static Component getChatPrefix() {
        return MiniMessage.miniMessage().deserialize("<dark_gray>[</dark_gray><gradient:#ca01ba:#ff1c3c>Origami</gradient><dark_gray>]</dark_gray> ");
    }

    public static void sendDecoratedChatMessage(String message, CommandSender sender) {
        if (sender != null) {
            sender.sendMessage(getChatPrefix().append(MiniMessage.miniMessage().deserialize(message)));
        }
    }

    public static void sendDecoratedChatMessage(ComponentLike message, CommandSender sender) {
        if (sender != null) {
            sender.sendMessage(getChatPrefix().append(message));
        }
    }

    public static Component removeItalicsIfAbsent(Component input) {
        return input.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

}
