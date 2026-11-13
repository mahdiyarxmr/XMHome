package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class XMHomeCommand implements CommandExecutor {
    private final XMHome plugin;

    public XMHomeCommand(XMHome plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xmhome.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixed("en", "no-permission"));
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "XMHome v" + plugin.getDescription().getVersion()
                    + ChatColor.GRAY + " (" + plugin.getCompat().getVersionName() + ")");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            plugin.getLanguageManager().loadLanguages();
            sender.sendMessage(plugin.getLanguageManager().getPrefixed("en", "reloaded"));
            return true;
        }
        if (args[0].equalsIgnoreCase("debug")) {
            sender.sendMessage(ChatColor.GOLD + "XMHome Diagnostics:");
            sender.sendMessage(ChatColor.GRAY + "Compat: " + ChatColor.WHITE + plugin.getCompat().getVersionName());
            sender.sendMessage(ChatColor.GRAY + "Storage: " + ChatColor.WHITE + plugin.getConfigManager().getStorageType());
            sender.sendMessage(ChatColor.GRAY + "Vault: " + ChatColor.WHITE + plugin.getEconomyManager().isAvailable());
            sender.sendMessage(ChatColor.GRAY + "GriefPrevention: " + ChatColor.WHITE + plugin.getGriefPreventionHook().isAvailable());
            return true;
        }
        return true;
    }
}
