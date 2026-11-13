package ir.mahdiyarxmr.xmhome.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.RoundingMode;
import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration raw() {
        return config;
    }

    public String getDefaultLanguage() {
        return config.getString("general.language", "en");
    }

    public String getStorageType() {
        return config.getString("general.storage", "SQLITE").toUpperCase();
    }

    public boolean isDebug() {
        return config.getBoolean("general.debug", false);
    }

    public int getDefaultLimit() {
        return Math.max(1, config.getInt("homes.default-limit", 3));
    }

    public boolean isAllowCustomSets() {
        return config.getBoolean("homes.allow-custom-sets", true);
    }

    public int getMaxHomeSets() {
        return Math.max(1, config.getInt("homes.max-home-sets", 5));
    }

    public int getMaxNameLength() {
        return Math.max(3, config.getInt("homes.max-name-length", 24));
    }

    public int getMinNameLength() {
        return Math.max(1, config.getInt("homes.min-name-length", 1));
    }

    public String getDefaultSetName() {
        return config.getString("homes.default-set-name", "Main");
    }

    public int getMaxSharedUsers() {
        return Math.max(1, config.getInt("homes.max-shared-users", 10));
    }

    public List<String> getDisabledWorlds() {
        return config.getStringList("homes.disabled-worlds");
    }

    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled", true);
    }

    public String getCurrencySymbol() {
        return config.getString("economy.currency-symbol", "$");
    }

    public boolean isFirstHomeFree() {
        return config.getBoolean("economy.first-home-free", true);
    }

    public double getBasePrice() {
        return Math.max(0.0, config.getDouble("economy.base-price", 1000.0));
    }

    public double getPriceMultiplier() {
        double m = config.getDouble("economy.price-multiplier", 1.5);
        return m > 0 ? m : 1.0;
    }

    public double getMaxPrice() {
        return config.getDouble("economy.max-price", -1);
    }

    public RoundingMode getRoundingMode() {
        String mode = config.getString("economy.rounding", "HALF_UP").toUpperCase();
        try {
            return RoundingMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            return RoundingMode.HALF_UP;
        }
    }

    public boolean isChargeOnCreate() {
        return config.getBoolean("economy.charge-on-create", true);
    }

    public boolean isChargeOnDuplicate() {
        return config.getBoolean("economy.charge-on-duplicate", true);
    }

    public int getTeleportDelay() {
        return Math.max(0, config.getInt("teleport.delay", 5));
    }

    public int getTeleportCooldown() {
        return Math.max(0, config.getInt("teleport.cooldown", 30));
    }

    public boolean isCancelOnMove() {
        return config.getBoolean("teleport.cancel-on-move", true);
    }

    public boolean isCancelOnDamage() {
        return config.getBoolean("teleport.cancel-on-damage", true);
    }

    public boolean isSafeLocationEnabled() {
        return config.getBoolean("teleport.safe-location", true);
    }

    public boolean isCrossWorldEnabled() {
        return config.getBoolean("teleport.cross-world", true);
    }

    public boolean isCrossDimensionEnabled() {
        return config.getBoolean("teleport.cross-dimension", true);
    }

    public boolean isMakeHomeSafeEnabled() {
        return config.getBoolean("protection.make-home-safe", true);
    }

    public boolean isGriefPreventionEnabled() {
        return config.getBoolean("protection.griefprevention.enabled", true);
    }

    public boolean isGuiAnimations() {
        return config.getBoolean("gui.animations", true);
    }

    public boolean isGuiSounds() {
        return config.getBoolean("gui.sounds", true);
    }

    public boolean isGuiParticles() {
        return config.getBoolean("gui.particles", true);
    }
}
