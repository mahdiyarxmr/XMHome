package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.HomeSet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    private final XMHome plugin;

    public SetHomeCommand(XMHome plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().get("en", "player-only"));
            return true;
        }
        Player player = (Player) sender;
        String lang = plugin.getHomeManager().getSettings(player.getUniqueId()).getLanguage();

        if (!player.hasPermission("xmhome.sethome")) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "no-permission"));
            return true;
        }

        if (plugin.getConfigManager().getDisabledWorlds().contains(player.getWorld().getName())) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-world-disabled"));
            return true;
        }

        HomeSet targetSet = plugin.getHomeManager().getSelectedSet(player.getUniqueId());
        String homeName;
        if (args.length >= 2) {
            targetSet = plugin.getHomeManager().getHomeSetByName(player.getUniqueId(), args[0]);
            if (targetSet == null) {
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-not-found", "set", args[0]));
                return true;
            }
            homeName = args[1];
        } else if (args.length == 1) {
            homeName = args[0];
        } else {
            homeName = plugin.getConfigManager().raw().getString("homes.default-home-prefix", "home")
                    + (plugin.getHomeManager().getHomes(player.getUniqueId()).size() + 1);
        }

        if (!plugin.getHomeManager().isValidName(homeName)) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-invalid-name",
                    "max", String.valueOf(plugin.getConfigManager().getMaxNameLength())));
            return true;
        }

        if (plugin.getHomeManager().getHome(player.getUniqueId(), homeName) != null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-already-exists", "home", homeName));
            return true;
        }

        int limit = plugin.getHomeManager().getEffectiveLimit(player);
        int currentCount = plugin.getHomeManager().getHomes(player.getUniqueId()).size();
        if (limit >= 0 && currentCount >= limit) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-limit-reached", "limit", String.valueOf(limit)));
            return true;
        }

        if (!plugin.getEconomyManager().tryLock(player.getUniqueId())) {
            return true;
        }

        double price = 0.0;
        boolean charged = false;
        try {
            if (plugin.getEconomyManager().isAvailable() && plugin.getConfigManager().isChargeOnCreate()
                    && !player.hasPermission("xmhome.economy.bypass")) {
                price = plugin.getEconomyManager().getCalculator().calculatePrice(
                        currentCount, targetSet.getName(), player.getWorld().getName());
                if (price > 0) {
                    if (!plugin.getEconomyManager().hasEnough(player, price)) {
                        player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "economy-insufficient",
                                "price", plugin.getEconomyManager().format(price),
                                "balance", plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player))));
                        return true;
                    }
                    if (!plugin.getEconomyManager().withdraw(player, price)) {
                        player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "economy-transaction-failed"));
                        return true;
                    }
                    charged = true;
                }
            }

            plugin.getHomeManager().createHome(player, targetSet, homeName, player.getLocation());
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-created", "home", homeName));
            if (charged) {
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "economy-charged",
                        "price", plugin.getEconomyManager().format(price),
                        "balance", plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player))));
            }
        } catch (Throwable t) {
            if (charged) {
                plugin.getEconomyManager().refund(player, price);
            }
            plugin.getLogger().severe("Error creating home: " + t.getMessage());
        } finally {
            plugin.getEconomyManager().unlock(player.getUniqueId());
        }
        return true;
    }
}
