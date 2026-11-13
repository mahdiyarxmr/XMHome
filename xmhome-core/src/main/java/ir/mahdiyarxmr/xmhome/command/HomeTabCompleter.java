package ir.mahdiyarxmr.xmhome.command;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import ir.mahdiyarxmr.xmhome.model.HomeSet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HomeTabCompleter implements TabCompleter {
    private final XMHome plugin;

    public HomeTabCompleter(XMHome plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();
        String prefix = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (cmd.equals("homeset")) {
            if (args.length == 1) {
                return filter(Arrays.asList("create", "delete", "rename", "select", "list"), prefix);
            }
            if (args.length == 2 && !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("list")) {
                return filter(plugin.getHomeManager().getHomeSets(player.getUniqueId())
                        .stream().map(HomeSet::getName).collect(Collectors.toList()), prefix);
            }
            return Collections.emptyList();
        }

        if (cmd.equals("xmhome")) {
            if (args.length == 1) {
                return filter(Arrays.asList("reload", "version", "debug"), prefix);
            }
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> names = plugin.getHomeManager().getHomes(player.getUniqueId())
                    .stream().map(Home::getName).collect(Collectors.toList());
            if (cmd.equals("home")) {
                names.add("duplicate");
            }
            return filter(names, prefix);
        }

        if (cmd.equals("homeshare") && args.length == 3) {
            return filter(Arrays.asList("VISIT", "VIEW", "MANAGE"), prefix);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        for (String opt : options) {
            if (opt.toLowerCase().startsWith(prefix)) {
                result.add(opt);
            }
        }
        return result;
    }
}
