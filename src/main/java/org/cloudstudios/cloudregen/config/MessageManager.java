package org.cloudstudios.cloudregen.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.cloudstudios.cloudregen.utils.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class MessageManager {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String[] BUNDLED_LANGUAGES_FALLBACK = {
            "ar", "cs", "da", "de", "en", "es", "fr", "he", "hu", "id", "it", "ja", "ko", "lt",
            "nl", "pl", "pt", "ro", "ru", "sk", "sv", "th", "tr", "uk", "vi", "zh"
    };

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private YamlConfiguration messages;
    private YamlConfiguration fallback;
    private PlaceholderService placeholders;
    private boolean languageFallback;

    public MessageManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        reload();
    }

    public void setPlaceholderService(PlaceholderService placeholders) {
        this.placeholders = placeholders;
    }

    public void reload() {
        migrateLegacyMessagesFile();
        ensureLangDirectory();

        String requested = normalizeLanguage(configManager.language());
        languageFallback = false;

        File langFile = resolveLangFile(requested);
        if (!langFile.exists()) {
            if (!requested.equals(DEFAULT_LANGUAGE)) {
                plugin.getLogger().warning(
                        "Language file lang/" + requested + ".yml not found. Falling back to " + DEFAULT_LANGUAGE + ".");
                languageFallback = true;
            }
            requested = DEFAULT_LANGUAGE;
            langFile = resolveLangFile(DEFAULT_LANGUAGE);
            if (!langFile.exists()) {
                plugin.saveResource("lang/" + DEFAULT_LANGUAGE + ".yml", false);
            }
        }

        this.messages = YamlConfiguration.loadConfiguration(langFile);
        File enFile = resolveLangFile(DEFAULT_LANGUAGE);
        this.fallback = requested.equals(DEFAULT_LANGUAGE)
                ? this.messages
                : YamlConfiguration.loadConfiguration(enFile);
    }

    public boolean usedLanguageFallback() {
        return languageFallback;
    }

    public String get(String key) {
        return apply(Text.color(resolveString(key)));
    }

    public List<String> getList(String key) {
        List<String> lines = resolveList(key);
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>(lines.size());
        for (String line : Text.colorList(lines)) {
            out.add(apply(line));
        }
        return out;
    }

    public String format(String key, String... replacements) {
        String value = get(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            value = value.replace(replacements[i], replacements[i + 1]);
        }
        return apply(value);
    }

    public void sendPrefixed(org.bukkit.command.CommandSender sender, String key, String... replacements) {
        sender.sendMessage(format("prefix") + format(key, replacements));
    }

    private String resolveString(String key) {
        String value = messages.getString(key);
        if (value == null && fallback != messages) {
            value = fallback.getString(key);
        }
        return value != null ? value : key;
    }

    private List<String> resolveList(String key) {
        List<String> lines = messages.getStringList(key);
        if ((lines == null || lines.isEmpty()) && fallback != messages) {
            lines = fallback.getStringList(key);
        }
        return lines != null ? lines : Collections.emptyList();
    }

    private String apply(String input) {
        if (placeholders == null) {
            return input;
        }
        return placeholders.apply(input);
    }

    private void ensureLangDirectory() {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists() && !langDir.mkdirs()) {
            plugin.getLogger().log(Level.WARNING, "Could not create lang directory at {0}", langDir.getAbsolutePath());
        }
        for (String code : discoverBundledLanguages()) {
            File target = new File(langDir, code + ".yml");
            if (!target.exists()) {
                try {
                    plugin.saveResource("lang/" + code + ".yml", false);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().log(Level.WARNING, "Missing bundled language file: lang/{0}.yml", code);
                }
            }
        }
    }

    private void migrateLegacyMessagesFile() {
        File legacy = new File(plugin.getDataFolder(), "messages.yml");
        File enFile = resolveLangFile(DEFAULT_LANGUAGE);
        if (legacy.exists() && !enFile.exists()) {
            File langDir = enFile.getParentFile();
            if (!langDir.exists()) {
                langDir.mkdirs();
            }
            if (legacy.renameTo(enFile)) {
                plugin.getLogger().info("Migrated messages.yml to lang/en.yml");
            }
        }
    }

    private File resolveLangFile(String code) {
        return new File(plugin.getDataFolder(), "lang/" + code + ".yml");
    }

    private String[] discoverBundledLanguages() {
        TreeSet<String> codes = new TreeSet<>();
        try {
            Enumeration<URL> resources = plugin.getClass().getClassLoader().getResources("lang");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if ("jar".equals(url.getProtocol())) {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    try (JarFile jar = connection.getJarFile()) {
                        for (JarEntry entry : Collections.list(jar.entries())) {
                            addLangCode(codes, entry.getName());
                        }
                    }
                } else {
                    Path langDir = Path.of(url.toURI());
                    if (Files.isDirectory(langDir)) {
                        try (Stream<Path> paths = Files.list(langDir)) {
                            paths.filter(path -> path.toString().endsWith(".yml"))
                                    .map(path -> path.getFileName().toString())
                                    .map(name -> name.substring(0, name.length() - 4))
                                    .forEach(codes::add);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.FINE, "Could not scan bundled languages", ex);
        }
        if (codes.isEmpty()) {
            try (InputStream ignored = plugin.getResource("lang/en.yml")) {
                if (ignored != null) {
                    return BUNDLED_LANGUAGES_FALLBACK;
                }
            } catch (IOException ignored) {
                // fall through
            }
            return BUNDLED_LANGUAGES_FALLBACK;
        }
        return codes.toArray(String[]::new);
    }

    private static void addLangCode(TreeSet<String> codes, String entryName) {
        if (!entryName.startsWith("lang/") || !entryName.endsWith(".yml") || entryName.contains("/lang/lang/")) {
            return;
        }
        String fileName = entryName.substring(entryName.lastIndexOf('/') + 1);
        codes.add(fileName.substring(0, fileName.length() - 4));
    }

    static String normalizeLanguage(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_LANGUAGE;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        if (!normalized.matches("[a-z0-9_-]+")) {
            return DEFAULT_LANGUAGE;
        }
        return normalized;
    }
}
