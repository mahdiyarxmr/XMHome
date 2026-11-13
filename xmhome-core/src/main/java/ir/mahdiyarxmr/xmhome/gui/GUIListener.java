package ir.mahdiyarxmr.xmhome.gui;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import ir.mahdiyarxmr.xmhome.model.HomeSet;
import ir.mahdiyarxmr.xmhome.model.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIListener implements Listener {
    private final XMHome plugin;
    private final Map<UUID, Long> clickCooldown = new ConcurrentHashMap<>();

    public GUIListener(XMHome plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        long now = System.currentTimeMillis();
        if (now - clickCooldown.getOrDefault(player.getUniqueId(), 0L) < 150L) {
            return;
        }
        clickCooldown.put(player.getUniqueId(), now);

        MenuHolder holder = (MenuHolder) event.getInventory().getHolder();
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        switch (holder.getType()) {
            case MAIN:
                handleMainClick(player, holder, slot, event.isRightClick(), event.isShiftClick());
                break;
            case HOMESETS:
                handleHomeSetsClick(player, slot);
                break;
            case DETAILS:
                handleDetailsClick(player, holder, slot);
                break;
            case SETTINGS:
                handleSettingsClick(player, slot);
                break;
            case CONFIRM_DELETE:
                handleConfirmDeleteClick(player, holder, slot);
                break;
            case ADMIN:
                handleAdminClick(player, slot);
                break;
        }
    }

    private void handleMainClick(Player player, MenuHolder holder, int slot, boolean right, boolean shift) {
        if (slot >= 9 && slot < 36) {
            HomeSet set = plugin.getHomeManager().getSelectedSet(player.getUniqueId());
            List<Home> homes = set != null
                    ? plugin.getHomeManager().getHomesInSet(player.getUniqueId(), set.getId())
                    : plugin.getHomeManager().getHomes(player.getUniqueId());
            int index = holder.getPage() * 27 + (slot - 9);
            if (index >= homes.size()) return;
            Home home = homes.get(index);

            if (shift) {
                home.setFavorite(!home.isFavorite());
                plugin.getDatabaseManager().saveHomeAsync(home);
                plugin.getGuiManager().openMainMenu(player, holder.getPage(), holder.isFavoritesOnly());
            } else if (right) {
                plugin.getGuiManager().openHomeDetailsMenu(player, home);
            } else {
                player.closeInventory();
                plugin.getTeleportManager().teleport(player, home);
            }
            return;
        }

        if (slot == 36 && holder.getPage() > 0) {
            plugin.getGuiManager().openMainMenu(player, holder.getPage() - 1, holder.isFavoritesOnly());
        } else if (slot == 37) {
            plugin.getGuiManager().openHomeSetsMenu(player);
        } else if (slot == 41) {
            plugin.getGuiManager().openMainMenu(player, 0, !holder.isFavoritesOnly());
        } else if (slot == 42) {
            plugin.getGuiManager().openSettingsMenu(player);
        } else if (slot == 43) {
            plugin.getGuiManager().openMainMenu(player, holder.getPage() + 1, holder.isFavoritesOnly());
        } else if (slot == 44) {
            player.closeInventory();
        }
    }

    private void handleHomeSetsClick(Player player, int slot) {
        if (slot == 31) {
            plugin.getGuiManager().openMainMenu(player, 0, false);
            return;
        }
        List<HomeSet> sets = plugin.getHomeManager().getHomeSets(player.getUniqueId());
        if (slot >= 0 && slot < sets.size()) {
            HomeSet set = sets.get(slot);
            PlayerSettings s = plugin.getHomeManager().getSettings(player.getUniqueId());
            s.setSelectedSet(set.getName());
            plugin.getHomeManager().saveSettings(s);
            player.sendMessage(plugin.getLanguageManager().getPrefixed(s.getLanguage(), "homeset-selected", "set", set.getName()));
            plugin.getGuiManager().openMainMenu(player, 0, false);
        }
    }

    private void handleDetailsClick(Player player, MenuHolder holder, int slot) {
        Home home = plugin.getHomeManager().getHomes(player.getUniqueId()).stream()
                .filter(h -> h.getId().equals(holder.getTargetId()))
                .findFirst().orElse(null);
        if (home == null) {
            plugin.getGuiManager().openMainMenu(player, 0, false);
            return;
        }
        String lang = plugin.getHomeManager().getSettings(player.getUniqueId()).getLanguage();

        if (slot == 10) {
            player.closeInventory();
            plugin.getTeleportManager().teleport(player, home);
        } else if (slot == 11) {
            player.closeInventory();
            String coords = String.format("%s %.1f %.1f %.1f", home.getWorldName(), home.getX(), home.getY(), home.getZ());
            plugin.getCompat().sendCopyableCoords(player,
                    plugin.getLanguageManager().get(lang, "coords-xyz",
                            "x", String.format("%.1f", home.getX()),
                            "y", String.format("%.1f", home.getY()),
                            "z", String.format("%.1f", home.getZ())),
                    coords);
        } else if (slot == 12) {
            home.setFavorite(!home.isFavorite());
            plugin.getDatabaseManager().saveHomeAsync(home);
            plugin.getGuiManager().openHomeDetailsMenu(player, home);
        } else if (slot == 14) {
            home.setWorldName(player.getWorld().getName());
            home.setX(player.getLocation().getX());
            home.setY(player.getLocation().getY());
            home.setZ(player.getLocation().getZ());
            home.setYaw(player.getLocation().getYaw());
            home.setPitch(player.getLocation().getPitch());
            plugin.getDatabaseManager().saveHomeAsync(home);
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-location-updated", "home", home.getName()));
            player.closeInventory();
        } else if (slot == 16) {
            plugin.getGuiManager().openConfirmDeleteMenu(player, home);
        } else if (slot == 22) {
            plugin.getGuiManager().openMainMenu(player, 0, false);
        }
    }

    private void handleSettingsClick(Player player, int slot) {
        PlayerSettings s = plugin.getHomeManager().getSettings(player.getUniqueId());
        if (slot == 10) {
            s.setLanguage("en".equalsIgnoreCase(s.getLanguage()) ? "fa" : "en");
            plugin.getHomeManager().saveSettings(s);
            player.sendMessage(plugin.getLanguageManager().getPrefixed(s.getLanguage(), "gui.language-changed"));
            plugin.getGuiManager().openSettingsMenu(player);
        } else if (slot == 11) {
            s.setTeleportSounds(!s.isTeleportSounds());
            plugin.getHomeManager().saveSettings(s);
            plugin.getGuiManager().openSettingsMenu(player);
        } else if (slot == 12) {
            s.setTeleportParticles(!s.isTeleportParticles());
            plugin.getHomeManager().saveSettings(s);
            plugin.getGuiManager().openSettingsMenu(player);
        } else if (slot == 13) {
            s.setTeleportNotifications(!s.isTeleportNotifications());
            plugin.getHomeManager().saveSettings(s);
            plugin.getGuiManager().openSettingsMenu(player);
        } else if (slot == 14) {
            s.setGuiAnimations(!s.isGuiAnimations());
            plugin.getHomeManager().saveSettings(s);
            plugin.getGuiManager().openSettingsMenu(player);
        } else if (slot == 15 && plugin.getConfigManager().isMakeHomeSafeEnabled()) {
            s.setMakeHomeSafe(!s.isMakeHomeSafe());
            plugin.getHomeManager().saveSettings(s);
            String key = s.isMakeHomeSafe() ? "gui.make-home-safe-enabled" : "gui.make-home-safe-disabled";
            player.sendMessage(plugin.getLanguageManager().getPrefixed(s.getLanguage(), key));
            plugin.getGuiManager().openSettingsMenu(player);
        } else if (slot == 22) {
            plugin.getGuiManager().openMainMenu(player, 0, false);
        }
    }

    private void handleConfirmDeleteClick(Player player, MenuHolder holder, int slot) {
        Home home = plugin.getHomeManager().getHomes(player.getUniqueId()).stream()
                .filter(h -> h.getId().equals(holder.getTargetId()))
                .findFirst().orElse(null);
        String lang = plugin.getHomeManager().getSettings(player.getUniqueId()).getLanguage();

        if (slot == 11 && home != null) {
            plugin.getHomeManager().deleteHome(home);
            player.sendMessage(plugin.getLanguageManager().getPrefixed(lang, "home-deleted", "home", home.getName()));
            plugin.getGuiManager().openMainMenu(player, 0, false);
        } else if (slot == 15) {
            plugin.getGuiManager().openMainMenu(player, 0, false);
        }
    }

    private void handleAdminClick(Player player, int slot) {
        if (slot == 15 && player.hasPermission("xmhome.admin.reload")) {
            plugin.getConfigManager().reload();
            plugin.getLanguageManager().loadLanguages();
            player.sendMessage(ChatColor.GREEN + "XMHome reloaded!");
            player.closeInventory();
        } else if (slot == 22) {
            player.closeInventory();
        }
    }
}
