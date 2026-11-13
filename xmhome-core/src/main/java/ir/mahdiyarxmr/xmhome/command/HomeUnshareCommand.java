package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeUnshareCommand implements CommandExecutor {
    private final XMHome plugin;

    public HomeUnshareCommand(XMHome plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().get("en", "player-only"));
            return true;
        }
        Player player = (Player) sender;
        String lang = plugin.getHomeManager().getSettings(player.getUniqueId()).getLanguage();

        if (args.length < 2) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "invalid-usage", "usage", "/homeunshare <home> <player>"));
            return true;
        }

        Home home = plugin.getHomeManager().getHome(player.getUniqueId(), args[0]);
        if (home == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-not-found", "home", args[0]));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        plugin.getHomeManager().removeShare(home.getId(), target.getUniqueId());
        player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "share-revoked",
                "home", home.getName(), "player", args[1]));
        return true;
    }
}
