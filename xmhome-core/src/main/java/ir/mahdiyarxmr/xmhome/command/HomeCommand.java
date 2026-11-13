package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeCommand implements CommandExecutor {
    private final XMHome plugin;

    public HomeCommand(XMHome plugin) {
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

        if (!player.hasPermission("xmhome.home")) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "no-permission"));
            return true;
        }

        if (args.length == 0) {
            List<Home> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
            if (homes.size() == 1) {
                plugin.getTeleportManager().teleport(player, homes.get(0));
            } else {
                plugin.getGuiManager().openMainMenu(player, 0, false);
            }
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("duplicate") && args.length >= 3) {
            Home src = plugin.getHomeManager().getHome(player.getUniqueId(), args[1]);
            if (src == null) {
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-not-found", "home", args[1]));
                return true;
            }
            if (!plugin.getHomeManager().isValidName(args[2])) {
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-invalid-name",
                        "max", String.valueOf(plugin.getConfigManager().getMaxNameLength())));
                return true;
            }
            if (plugin.getHomeManager().getHome(player.getUniqueId(), args[2]) != null) {
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-already-exists", "home", args[2]));
                return true;
            }
            plugin.getHomeManager().createHome(player, plugin.getHomeManager().getSelectedSet(player.getUniqueId()), args[2], src.toLocation());
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-duplicated", "source", args[1], "target", args[2]));
            return true;
        }

        Home home = plugin.getHomeManager().getHome(player.getUniqueId(), args[0]);
        if (home == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-not-found", "home", args[0]));
            return true;
        }
        plugin.getTeleportManager().teleport(player, home);
        return true;
    }
}
