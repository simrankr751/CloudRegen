package org.cloudstudios.cloudregen.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.cloudstudios.cloudregen.CloudRegenPlugin;

public final class CloudRegenPlaceholderExpansion extends PlaceholderExpansion {
    private final CloudRegenPlugin plugin;

    public CloudRegenPlaceholderExpansion(CloudRegenPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "cr";
    }

    @Override
    public String getAuthor() {
        return String.join(",", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params == null || params.isBlank()) {
            return "";
        }
        if (params.toLowerCase().startsWith("time_")) {
            String region = params.substring("time_".length()).trim();
            if (region.startsWith("<")) {
                region = region.substring(1);
            }
            if (region.endsWith(">")) {
                region = region.substring(0, region.length() - 1);
            }
            long seconds = plugin.secondsUntilRegen(region);
            return seconds < 0L ? "N/A" : String.valueOf(seconds);
        }
        return null;
    }
}
