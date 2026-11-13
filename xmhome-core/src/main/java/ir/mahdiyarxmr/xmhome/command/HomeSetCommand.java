package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.HomeSet;
import ir.mahdiyarxmr.xmhome.model.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeSetCommand implements CommandExecutor {
    private final XMHome plugin;

    public HomeSetCommand(XMHome plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().get("en", "player-only"));
            return true;
        }
        Player player = (Player) sender;
        PlayerSettings settings = plugin.getHomeManager().getSettings(player.getUniqueId());
        String lang = settings.getLanguage();

        if (args.length == 0) {
            plugin.getGuiManager().openHomeSetsMenu(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "invalid-usage", "usage", "/homeset create <name>"));
                    return true;
                }
                if (!player.hasPermission("xmhome.homeset.create")) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "no-permission"));
                    return true;
                }
                List<HomeSet> sets = plugin.getHomeManager().getHomeSets(player.getUniqueId());
                if (sets.size() >= plugin.getConfigManager().getMaxHomeSets()) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-limit-reached",
                            "limit", String.valueOf(plugin.getConfigManager().getMaxHomeSets())));
                    return true;
                }
                if (plugin.getHomeManager().getHomeSetByName(player.getUniqueId(), args[1]) != null) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-already-exists", "set", args[1]));
                    return true;
                }
                plugin.getHomeManager().createHomeSet(player, args[1]);
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-created", "set", args[1]));
                break;

            case "delete":
                if (args.length < 2) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "invalid-usage", "usage", "/homeset delete <name>"));
                    return true;
                }
                HomeSet toDelete = plugin.getHomeManager().getHomeSetByName(player.getUniqueId(), args[1]);
                if (toDelete == null) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-not-found", "set", args[1]));
                    return true;
                }
                if (toDelete.isDefault()) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-cannot-delete-default"));
                    return true;
                }
                plugin.getHomeManager().deleteHomeSet(toDelete);
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-deleted", "set", toDelete.getName()));
                break;

            case "rename":
                if (args.length < 3) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "invalid-usage", "usage", "/homeset rename <old> <new>"));
                    return true;
                }
                HomeSet toRename = plugin.getHomeManager().getHomeSetByName(player.getUniqueId(), args[1]);
                if (toRename == null) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-not-found", "set", args[1]));
                    return true;
                }
                toRename.setName(args[2]);
                plugin.getDatabaseManager().saveHomeSetAsync(toRename);
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-renamed", "old", args[1], "new", args[2]));
                break;

            case "select":
                if (args.length < 2) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "invalid-usage", "usage", "/homeset select <name>"));
                    return true;
                }
                HomeSet toSelect = plugin.getHomeManager().getHomeSetByName(player.getUniqueId(), args[1]);
                if (toSelect == null) {
                    player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-not-found", "set", args[1]));
                    return true;
                }
                settings.setSelectedSet(toSelect.getName());
                plugin.getHomeManager().saveSettings(settings);
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "homeset-selected", "set", toSelect.getName()));
                break;

            case "list":
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Home Sets:");
                for (HomeSet s : plugin.getHomeManager().getHomeSets(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GRAY + " - " + ChatColor.AQUA + s.getName()
                            + (s.getName().equalsIgnoreCase(settings.getSelectedSet()) ? ChatColor.GREEN + " (Selected)" : ""));
                }
                break;

            default:
                plugin.getGuiManager().openHomeSetsMenu(player);
                break;
        }
        return true;
    }
}
