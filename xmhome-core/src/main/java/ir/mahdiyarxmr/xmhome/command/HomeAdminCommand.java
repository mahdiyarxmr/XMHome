package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeAdminCommand implements CommandExecutor {
    private final XMHome plugin;

    public HomeAdminCommand(XMHome plugin) {
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
        if (!player.hasPermission("xmhome.admin.gui")) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "no-permission"));
            return true;
        }
        plugin.getGuiManager().openAdminMenu(player);
        return true;
    }
}
