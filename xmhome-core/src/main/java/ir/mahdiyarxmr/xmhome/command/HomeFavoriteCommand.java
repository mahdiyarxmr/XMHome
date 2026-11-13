package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeFavoriteCommand implements CommandExecutor {
    private final XMHome plugin;

    public HomeFavoriteCommand(XMHome plugin) {
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

        if (args.length < 1) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "invalid-usage", "usage", "/homefavorite <name>"));
            return true;
        }
        Home home = plugin.getHomeManager().getHome(player.getUniqueId(), args[0]);
        if (home == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-not-found", "home", args[0]));
            return true;
        }

        home.setFavorite(!home.isFavorite());
        plugin.getDatabaseManager().saveHomeAsync(home);
        String key = home.isFavorite() ? "home-favorite-added" : "home-favorite-removed";
        player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, key, "home", home.getName()));
        return true;
    }
}
