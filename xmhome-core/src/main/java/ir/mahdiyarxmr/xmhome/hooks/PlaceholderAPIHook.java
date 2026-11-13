package ir.mahdiyarxmr.xmhome.hooks;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final XMHome plugin;

    public PlaceholderAPIHook(XMHome plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "xmhome";
    }

    @Override
    public String getAuthor() {
        return "MahdiyarXMR";
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
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";
        switch (params.toLowerCase()) {
            case "home_count":
                return String.valueOf(plugin.getHomeManager().getHomes(player.getUniqueId()).size());
            case "home_limit":
                int limit = plugin.getHomeManager().getEffectiveLimit(player);
                return limit < 0 ? "Unlimited" : String.valueOf(limit);
            case "current_set":
                return plugin.getHomeManager().getSettings(player.getUniqueId()).getSelectedSet();
            case "favorite_home":
                List<Home> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
                return homes.stream().filter(Home::isFavorite).map(Home::getName).findFirst().orElse("None");
            case "next_home_price":
                int count = plugin.getHomeManager().getHomes(player.getUniqueId()).size();
                String set = plugin.getHomeManager().getSettings(player.getUniqueId()).getSelectedSet();
                double price = plugin.getEconomyManager().getCalculator().calculatePrice(count, set, player.getWorld().getName());
                return plugin.getEconomyManager().format(price);
            case "language":
                return plugin.getHomeManager().getSettings(player.getUniqueId()).getLanguage();
            default:
                return null;
        }
    }
}
