package ir.mahdiyarxmr.xmhome.manager;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import ir.mahdiyarxmr.xmhome.model.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager implements Listener {
    private final XMHome plugin;
    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public TeleportManager(XMHome plugin) {
        this.plugin = plugin;
    }

    public void teleport(Player player, Home home) {
        UUID uuid = player.getUniqueId();
        PlayerSettings settings = plugin.getHomeManager().getSettings(uuid);
        String lang = settings.getLanguage();

        Location dest = home.toLocation();
        if (dest == null || dest.getWorld() == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "teleport-world-missing", "world", home.getWorldName()));
            return;
        }

        if (plugin.getConfigManager().getDisabledWorlds().contains(dest.getWorld().getName())) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-world-disabled"));
            return;
        }

        if (!player.getWorld().equals(dest.getWorld())) {
            if (!plugin.getConfigManager().isCrossWorldEnabled()) {
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "teleport-cross-world-disabled"));
                return;
            }
            if (!plugin.getConfigManager().isCrossDimensionEnabled()
                    && player.getWorld().getEnvironment() != dest.getWorld().getEnvironment()) {
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "teleport-cross-world-disabled"));
                return;
            }
        }

        if (plugin.getConfigManager().isSafeLocationEnabled() && !plugin.getProtectionManager().isSafeLocation(dest)) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "teleport-unsafe"));
            return;
        }

        if (!player.hasPermission("xmhome.bypass.cooldown")) {
            long now = System.currentTimeMillis();
            long until = cooldowns.getOrDefault(uuid, 0L);
            if (until > now) {
                long left = (until - now) / 1000 + 1;
                player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "teleport-cooldown", "seconds", String.valueOf(left)));
                return;
            }
        }

        cancelPending(uuid);

        int delay = player.hasPermission("xmhome.bypass.delay") ? 0 : plugin.getConfigManager().getTeleportDelay();
        if (delay <= 0) {
            executeTeleport(player, home, dest, settings);
            return;
        }

        player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "teleport-starting",
                "home", home.getName(), "delay", String.valueOf(delay)));

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(uuid);
            if (player.isOnline()) {
                executeTeleport(player, home, dest, settings);
            }
        }, delay * 20L);

        pendingTeleports.put(uuid, task);
    }

    private void executeTeleport(Player player, Home home, Location dest, PlayerSettings settings) {
        player.teleport(dest);
        home.setLastVisitedAt(System.currentTimeMillis());
        plugin.getDatabaseManager().saveHomeAsync(home);

        if (!player.hasPermission("xmhome.bypass.cooldown")) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (plugin.getConfigManager().getTeleportCooldown() * 1000L));
        }

        if (settings.isTeleportNotifications()) {
            player.sendMessage(plugin.getLanguageManager().getPrefixed(settings.getLanguage(), "teleport-success", "home", home.getName()));
            plugin.getCompat().sendActionBar(player, plugin.getLanguageManager().get(settings.getLanguage(), "teleport-success", "home", home.getName()));
        }
        if (settings.isTeleportSounds() && plugin.getConfigManager().isGuiSounds()) {
            plugin.getCompat().playSound(player, "TELEPORT", 1.0f, 1.0f);
        }
        if (settings.isTeleportParticles() && plugin.getConfigManager().isGuiParticles()) {
            plugin.getCompat().spawnParticle(player, dest.clone().add(0, 1, 0), "PORTAL", 20);
        }
    }

    public void cancelPending(UUID uuid) {
        BukkitTask task = pendingTeleports.remove(uuid);
        if (task != null) task.cancel();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isCancelOnMove()) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        UUID uuid = event.getPlayer().getUniqueId();
        if (pendingTeleports.containsKey(uuid)) {
            cancelPending(uuid);
            String lang = plugin.getHomeManager().getSettings(uuid).getLanguage();
            event.getPlayer().sendMessage(plugin.getLanguageManager().getPrefixed(lang, "teleport-cancelled-move"));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!plugin.getConfigManager().isCancelOnDamage()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();
        if (pendingTeleports.containsKey(uuid)) {
            cancelPending(uuid);
            String lang = plugin.getHomeManager().getSettings(uuid).getLanguage();
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "teleport-cancelled-damage"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelPending(event.getPlayer().getUniqueId());
    }
}
