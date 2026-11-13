package ir.mahdiyarxmr.xmhome.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<String, FileConfiguration> languages = new HashMap<>();

    public LanguageManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        loadLanguages();
    }

    public void loadLanguages() {
        languages.clear();
        saveDefaultLanguage("en");
        saveDefaultLanguage("fa");

        File langDir = new File(plugin.getDataFolder(), "languages");
        File[] files = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String code = file.getName().replace(".yml", "").toLowerCase();
                languages.put(code, YamlConfiguration.loadConfiguration(file));
            }
        }
    }

    private void saveDefaultLanguage(String code) {
        File file = new File(plugin.getDataFolder(), "languages/" + code + ".yml");
        if (!file.exists()) {
            plugin.saveResource("languages/" + code + ".yml", false);
        }
    }

    public String get(String lang, String key, String... replacements) {
        String resolvedLang = (lang != null && languages.containsKey(lang.toLowerCase()))
                ? lang.toLowerCase()
                : configManager.getDefaultLanguage().toLowerCase();

        FileConfiguration cfg = languages.get(resolvedLang);
        String msg = cfg != null ? cfg.getString(key) : null;

        if (msg == null && !"en".equals(resolvedLang)) {
            FileConfiguration en = languages.get("en");
            if (en != null) {
                msg = en.getString(key);
            }
        }

        if (msg == null) {
            msg = "&cMissing translation: " + key;
        }

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String getPrefixed(String lang, String key, String... replacements) {
        return get(lang, "prefix") + get(lang, key, replacements);
    }
}
