package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCoordsCommand implements CommandExecutor {
    private final XMHome plugin;

    public HomeCoordsCommand(XMHome plugin) {
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
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "invalid-usage", "usage", "/homecoords <name>"));
            return true;
        }
        Home home = plugin.getHomeManager().getHome(player.getUniqueId(), args[0]);
        if (home == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-not-found", "home", args[0]));
            return true;
        }

        player.sendMessage(plugin.getLanguageManager().get(lang, "coords-header", "home", home.getName()));
        player.sendMessage(plugin.getLanguageManager().get(lang, "coords-world", "world", home.getWorldName()));
        String xyz = plugin.getLanguageManager().get(lang, "coords-xyz",
                "x", String.format("%.1f", home.getX()),
                "y", String.format("%.1f", home.getY()),
                "z", String.format("%.1f", home.getZ()));
        String copy = String.format("%s %.1f %.1f %.1f", home.getWorldName(), home.getX(), home.getY(), home.getZ());
        plugin.getCompat().sendCopyableCoords(player, xyz, copy);
        return true;
    }
}
