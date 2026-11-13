package ir.mahdiyarxmr.xmhome.manager;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HomeManager {
    private final XMHome plugin;
    private final Map<UUID, List<Home>> homesCache = new ConcurrentHashMap<>();
    private final Map<UUID, List<HomeSet>> setsCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerSettings> settingsCache = new ConcurrentHashMap<>();
    private final Map<UUID, List<HomeShare>> sharesByHome = new ConcurrentHashMap<>();

    public HomeManager(XMHome plugin) {
        this.plugin = plugin;
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        plugin.getDatabaseManager().loadSettingsAsync(uuid, plugin.getConfigManager().getDefaultLanguage(), plugin.getConfigManager().getDefaultSetName())
                .thenAccept(s -> settingsCache.put(uuid, s));

        plugin.getDatabaseManager().loadHomeSetsAsync(uuid).thenAccept(sets -> {
            if (sets.isEmpty()) {
                HomeSet def = new HomeSet(UUID.randomUUID(), uuid, plugin.getConfigManager().getDefaultSetName(),
                        "CHEST", "Default Home Set", true, false, false, System.currentTimeMillis());
                sets.add(def);
                plugin.getDatabaseManager().saveHomeSetAsync(def);
            }
            setsCache.put(uuid, new ArrayList<>(sets));
        });

        plugin.getDatabaseManager().loadHomesAsync(uuid).thenAccept(homes ->
                homesCache.put(uuid, new ArrayList<>(homes))
        );
    }

    public void unloadPlayer(UUID uuid) {
        homesCache.remove(uuid);
        setsCache.remove(uuid);
        settingsCache.remove(uuid);
    }

    public PlayerSettings getSettings(UUID uuid) {
        return settingsCache.computeIfAbsent(uuid, id -> new PlayerSettings(
                id, plugin.getConfigManager().getDefaultLanguage(),
                plugin.getConfigManager().getDefaultSetName(),
                true, true, true, true, true, false
        ));
    }

    public void saveSettings(PlayerSettings settings) {
        settingsCache.put(settings.getPlayerUuid(), settings);
        plugin.getDatabaseManager().saveSettingsAsync(settings);
    }

    public List<Home> getHomes(UUID uuid) {
        return homesCache.getOrDefault(uuid, Collections.emptyList());
    }

    public List<Home> getHomesInSet(UUID uuid, UUID setId) {
        return getHomes(uuid).stream()
                .filter(h -> h.getSetId().equals(setId))
                .collect(Collectors.toList());
    }

    public List<HomeSet> getHomeSets(UUID uuid) {
        return setsCache.getOrDefault(uuid, Collections.emptyList());
    }

    public HomeSet getSelectedSet(UUID uuid) {
        String selectedName = getSettings(uuid).getSelectedSet();
        List<HomeSet> sets = getHomeSets(uuid);
        return sets.stream()
                .filter(s -> s.getName().equalsIgnoreCase(selectedName))
                .findFirst()
                .orElse(sets.isEmpty() ? null : sets.get(0));
    }

    public HomeSet getHomeSetByName(UUID uuid, String name) {
        return getHomeSets(uuid).stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Home getHome(UUID uuid, String name) {
        HomeSet selected = getSelectedSet(uuid);
        if (selected != null) {
            Optional<Home> inSet = getHomesInSet(uuid, selected.getId()).stream()
                    .filter(h -> h.getName().equalsIgnoreCase(name))
                    .findFirst();
            if (inSet.isPresent()) return inSet.get();
        }
        return getHomes(uuid).stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public int getEffectiveLimit(Player player) {
        if (player.hasPermission("xmhome.limit.unlimited")) {
            return -1;
        }
        int max = plugin.getConfigManager().getDefaultLimit();
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) continue;
            String perm = info.getPermission().toLowerCase();
            if (perm.startsWith("xmhome.limit.")) {
                String suffix = perm.substring("xmhome.limit.".length());
                try {
                    int val = Integer.parseInt(suffix);
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return max;
    }

    public boolean isValidName(String name) {
        if (name == null) return false;
        int min = plugin.getConfigManager().getMinNameLength();
        int max = plugin.getConfigManager().getMaxNameLength();
        return name.length() >= min && name.length() <= max && name.matches("^[a-zA-Z0-9_-]+$");
    }

    public Home createHome(Player player, HomeSet set, String name, Location loc) {
        Home home = new Home(
                UUID.randomUUID(),
                player.getUniqueId(),
                set.getId(),
                name,
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(),
                "ENDER_PEARL", "", "General",
                false, false,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );
        homesCache.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(home);
        plugin.getDatabaseManager().saveHomeAsync(home);
        return home;
    }

    public void deleteHome(Home home) {
        List<Home> list = homesCache.get(home.getOwner());
        if (list != null) {
            list.removeIf(h -> h.getId().equals(home.getId()));
        }
        sharesByHome.remove(home.getId());
        plugin.getDatabaseManager().deleteHomeAsync(home.getId());
    }

    public HomeSet createHomeSet(Player player, String name) {
        HomeSet set = new HomeSet(
                UUID.randomUUID(),
                player.getUniqueId(),
                name,
                "CHEST",
                "",
                false,
                false,
                false,
                System.currentTimeMillis()
        );
        setsCache.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(set);
        plugin.getDatabaseManager().saveHomeSetAsync(set);
        return set;
    }

    public void deleteHomeSet(HomeSet set) {
        List<HomeSet> list = setsCache.get(set.getOwner());
        if (list != null) {
            list.removeIf(s -> s.getId().equals(set.getId()));
        }
        plugin.getDatabaseManager().deleteHomeSetAsync(set.getId());
    }

    public List<HomeShare> getShares(UUID homeId) {
        return sharesByHome.getOrDefault(homeId, Collections.emptyList());
    }

    public void addShare(HomeShare share) {
        sharesByHome.computeIfAbsent(share.getHomeId(), k -> new ArrayList<>()).add(share);
        plugin.getDatabaseManager().saveShareAsync(share);
    }

    public void removeShare(UUID homeId, UUID targetUuid) {
        List<HomeShare> list = sharesByHome.get(homeId);
        if (list != null) {
            list.removeIf(s -> s.getSharedWith().equals(targetUuid));
        }
        plugin.getDatabaseManager().deleteShareAsync(homeId, targetUuid);
    }
}
