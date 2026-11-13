package ir.mahdiyarxmr.xmhome.economy;

import ir.mahdiyarxmr.xmhome.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PricingCalculator {
    private final ConfigManager config;

    public PricingCalculator(ConfigManager config) {
        this.config = config;
    }

    public double calculatePrice(int currentHomeCount, String setName, String worldName) {
        int resultingCount = currentHomeCount + 1;
        boolean firstFree = config.isFirstHomeFree();
        double basePrice = config.getBasePrice();
        double multiplier = config.getPriceMultiplier();
        double maxPrice = config.getMaxPrice();

        // Layer 1: Home-set pricing
        if (setName != null && config.raw().getBoolean("economy.per-set-pricing.enabled", false)) {
            ConfigurationSection setSec = config.raw().getConfigurationSection("economy.per-set-pricing.sets." + setName);
            if (setSec != null) {
                firstFree = setSec.getBoolean("first-home-free", firstFree);
                basePrice = Math.max(0.0, setSec.getDouble("base-price", basePrice));
                double m = setSec.getDouble("price-multiplier", multiplier);
                if (m > 0) multiplier = m;
                maxPrice = setSec.getDouble("max-price", maxPrice);
            }
        } else if (worldName != null && config.raw().getBoolean("economy.per-world-pricing.enabled", false)) {
            // Layer 2: World-specific pricing
            ConfigurationSection worldSec = config.raw().getConfigurationSection("economy.per-world-pricing.worlds." + worldName);
            if (worldSec != null) {
                firstFree = worldSec.getBoolean("first-home-free", firstFree);
                basePrice = Math.max(0.0, worldSec.getDouble("base-price", basePrice));
                double m = worldSec.getDouble("price-multiplier", multiplier);
                if (m > 0) multiplier = m;
                maxPrice = worldSec.getDouble("max-price", maxPrice);
            }
        }

        if (firstFree && resultingCount <= 1) {
            return 0.0;
        }

        int exponent = firstFree ? (resultingCount - 2) : (resultingCount - 1);
        if (exponent < 0) exponent = 0;

        double rawPrice = basePrice * Math.pow(multiplier, exponent);
        if (maxPrice >= 0 && rawPrice > maxPrice) {
            rawPrice = maxPrice;
        }
        if (rawPrice < 0) rawPrice = 0.0;

        RoundingMode rounding = config.getRoundingMode();
        return BigDecimal.valueOf(rawPrice).setScale(2, rounding).doubleValue();
    }
}
