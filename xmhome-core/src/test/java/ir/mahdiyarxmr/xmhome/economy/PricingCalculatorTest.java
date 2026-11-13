package ir.mahdiyarxmr.xmhome.economy;

import ir.mahdiyarxmr.xmhome.config.ConfigManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingCalculatorTest {

    private PricingCalculator createCalculator(boolean firstFree, double basePrice, double multiplier, double maxPrice, RoundingMode rounding) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("economy.first-home-free", firstFree);
        yaml.set("economy.base-price", basePrice);
        yaml.set("economy.price-multiplier", multiplier);
        yaml.set("economy.max-price", maxPrice);
        yaml.set("economy.rounding", rounding.name());

        ConfigManager stubConfig = new ConfigManager(null) {
            @Override public boolean isFirstHomeFree() { return firstFree; }
            @Override public double getBasePrice() { return basePrice; }
            @Override public double getPriceMultiplier() { return multiplier; }
            @Override public double getMaxPrice() { return maxPrice; }
            @Override public RoundingMode getRoundingMode() { return rounding; }
            @Override public org.bukkit.configuration.file.FileConfiguration raw() { return yaml; }
        };
        return new PricingCalculator(stubConfig);
    }

    @Test
    void testFirstHomeFreeWith2xMultiplier() {
        PricingCalculator calc = createCalculator(true, 1000.0, 2.0, -1, RoundingMode.HALF_UP);
        assertEquals(0.0, calc.calculatePrice(0, null, null));
        assertEquals(1000.0, calc.calculatePrice(1, null, null));
        assertEquals(2000.0, calc.calculatePrice(2, null, null));
        assertEquals(4000.0, calc.calculatePrice(3, null, null));
        assertEquals(8000.0, calc.calculatePrice(4, null, null));
    }

    @Test
    void testFirstHomeFreeWith1_5xMultiplier() {
        PricingCalculator calc = createCalculator(true, 1000.0, 1.5, -1, RoundingMode.HALF_UP);
        assertEquals(0.0, calc.calculatePrice(0, null, null));
        assertEquals(1000.0, calc.calculatePrice(1, null, null));
        assertEquals(1500.0, calc.calculatePrice(2, null, null));
        assertEquals(2250.0, calc.calculatePrice(3, null, null));
        assertEquals(3375.0, calc.calculatePrice(4, null, null));
        assertEquals(5062.50, calc.calculatePrice(5, null, null));
    }

    @Test
    void testFirstHomePaidAndMaxPrice() {
        PricingCalculator calc = createCalculator(false, 1000.0, 2.0, 3000.0, RoundingMode.HALF_UP);
        assertEquals(1000.0, calc.calculatePrice(0, null, null));
        assertEquals(2000.0, calc.calculatePrice(1, null, null));
        assertEquals(3000.0, calc.calculatePrice(2, null, null));
        assertEquals(3000.0, calc.calculatePrice(3, null, null));
    }
}
