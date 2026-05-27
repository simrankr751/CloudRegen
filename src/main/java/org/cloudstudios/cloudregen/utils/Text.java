package org.cloudstudios.cloudregen.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private Text() {
    }

    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }

    public static Component component(String input) {
        return LEGACY.deserialize(input == null ? "" : input);
    }

    public static List<String> colorList(List<String> input) {
        List<String> out = new ArrayList<>(input.size());
        for (String line : input) {
            out.add(color(line));
        }
        return out;
    }

    public static void sendActionbar(Player player, String message) {
        player.sendActionBar(component(message));
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        player.showTitle(net.kyori.adventure.title.Title.title(component(title), component(subtitle)));
    }
}
