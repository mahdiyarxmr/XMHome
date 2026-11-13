package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import ir.mahdiyarxmr.xmhome.model.HomeShare;
import ir.mahdiyarxmr.xmhome.model.SharePermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeShareCommand implements CommandExecutor {
    private final XMHome plugin;

    public HomeShareCommand(XMHome plugin) {
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

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Shared Homes:");
            for (Home h : plugin.getHomeManager().getHomes(player.getUniqueId())) {
                List<HomeShare> shares = plugin.getHomeManager().getShares(h.getId());
                if (!shares.isEmpty()) {
                    player.sendMessage(ChatColor.AQUA + h.getName() + ChatColor.GRAY + " -> " + shares.size() + " player(s)");
                }
            }
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "invalid-usage", "usage", "/homeshare <home> <player> [VISIT|VIEW|MANAGE]"));
            return true;
        }

        Home home = plugin.getHomeManager().getHome(player.getUniqueId(), args[0]);
        if (home == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-not-found", "home", args[0]));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "player-not-found", "player", args[1]));
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "share-cannot-self"));
            return true;
        }

        SharePermission perm = args.length >= 3 ? SharePermission.fromString(args[2]) : SharePermission.VISIT;
        plugin.getHomeManager().addShare(new HomeShare(home.getId(), target.getUniqueId(), target.getName(), perm, -1));
        player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "share-success",
                "home", home.getName(), "player", target.getName(), "level", perm.name()));
        return true;
    }
}
