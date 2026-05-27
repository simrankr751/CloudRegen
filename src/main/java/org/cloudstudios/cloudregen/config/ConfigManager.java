package org.cloudstudios.cloudregen.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {
    public static final String DEFAULT_LANGUAGE = "en";

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String language() {
        return MessageManager.normalizeLanguage(config.getString("language", DEFAULT_LANGUAGE));
    }

    public Material wandMaterial() { return Material.matchMaterial(config.getString("wand.material", "BLAZE_ROD")); }
    public String wandName() { return config.getString("wand.name", "&bCloudRegen Wand"); }
    public int blocksPerCycle() { return Math.max(64, config.getInt("regen.blocks-per-cycle", 4096)); }
    public int maxChunksPerCycle() { return Math.max(1, config.getInt("regen.max-chunks-per-cycle", 16)); }
    public int cyclesPerSecond() { return Math.max(1, config.getInt("regen.cycles-per-second", 20)); }
    public boolean adaptiveEnabled() { return config.getBoolean("regen.adaptive.enabled", true); }
    public int adaptiveMin() { return Math.max(64, config.getInt("regen.adaptive.min-blocks-per-cycle", 512)); }
    public int adaptiveMax() { return Math.max(adaptiveMin(), config.getInt("regen.adaptive.max-blocks-per-cycle", 16384)); }
    public int compressionLevel() { return Math.clamp(config.getInt("storage.compression-level", 3), 0, 9); }
    public int writeBufferSize() { return Math.max(8192, config.getInt("storage.write-buffer-size", 131072)); }
    public int autosaveSeconds() { return Math.max(10, config.getInt("storage.autosave-seconds", 60)); }
    public boolean debug() { return config.getBoolean("debug.enabled", false); }
}
