package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeInfoCommand implements CommandExecutor {
    private final XMHome plugin;

    public HomeInfoCommand(XMHome plugin) {
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
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "invalid-usage", "usage", "/homeinfo <name>"));
            return true;
        }
        Home home = plugin.getHomeManager().getHome(player.getUniqueId(), args[0]);
        if (home == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-not-found", "home", args[0]));
            return true;
        }

        player.sendMessage(ChatColor.DARK_PURPLE + "=== " + ChatColor.AQUA + home.getName() + ChatColor.DARK_PURPLE + " ===");
        player.sendMessage(ChatColor.GRAY + "World: " + ChatColor.WHITE + home.getWorldName());
        player.sendMessage(ChatColor.GRAY + String.format("Location: %.1f, %.1f, %.1f", home.getX(), home.getY(), home.getZ()));
        player.sendMessage(ChatColor.GRAY + "Favorite: " + (home.isFavorite() ? ChatColor.GOLD + "Yes" : ChatColor.WHITE + "No"));
        return true;
    }
}
