package ir.mahdiyarxmr.xmhome.economy;

import ir.mahdiyarxmr.xmhome.config.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final PricingCalculator calculator;
    private Economy economy;
    private final Set<UUID> activeTransactions = ConcurrentHashMap.newKeySet();

    public EconomyManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.calculator = new PricingCalculator(config);
        setupVault();
    }

    public void setupVault() {
        if (!config.isEconomyEnabled()) {
            this.economy = null;
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            this.economy = null;
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            this.economy = rsp.getProvider();
            plugin.getLogger().info("Vault Economy hooked: " + economy.getName());
        } else {
            this.economy = null;
        }
    }

    public boolean isAvailable() {
        return config.isEconomyEnabled() && economy != null;
    }

    public PricingCalculator getCalculator() {
        return calculator;
    }

    public String format(double amount) {
        if (economy != null) {
            return economy.format(amount);
        }
        return config.getCurrencySymbol() + String.format("%.2f", amount);
    }

    public double getBalance(Player player) {
        return economy != null ? economy.getBalance(player) : 0.0;
    }

    public boolean hasEnough(Player player, double amount) {
        if (!isAvailable() || amount <= 0 || player.hasPermission("xmhome.economy.bypass")) {
            return true;
        }
        return economy.has(player, amount);
    }

    public boolean tryLock(UUID playerUuid) {
        return activeTransactions.add(playerUuid);
    }

    public void unlock(UUID playerUuid) {
        activeTransactions.remove(playerUuid);
    }

    public boolean withdraw(Player player, double amount) {
        if (!isAvailable() || amount <= 0 || player.hasPermission("xmhome.economy.bypass")) {
            return true;
        }
        EconomyResponse resp = economy.withdrawPlayer(player, amount);
        return resp.transactionSuccess();
    }

    public void refund(Player player, double amount) {
        if (!isAvailable() || amount <= 0) return;
        economy.depositPlayer(player, amount);
    }
}
