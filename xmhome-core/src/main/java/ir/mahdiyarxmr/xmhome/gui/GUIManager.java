package ir.mahdiyarxmr.xmhome.gui;

import ir.mahdiyarxmr.xmhome.XMHome;
import ir.mahdiyarxmr.xmhome.model.Home;
import ir.mahdiyarxmr.xmhome.model.HomeSet;
import ir.mahdiyarxmr.xmhome.model.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GUIManager {
    private final XMHome plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public GUIManager(XMHome plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player, int page, boolean favoritesOnly) {
        PlayerSettings settings = plugin.getHomeManager().getSettings(player.getUniqueId());
        String lang = settings.getLanguage();
        HomeSet selectedSet = plugin.getHomeManager().getSelectedSet(player.getUniqueId());
        String setName = selectedSet != null ? selectedSet.getName() : plugin.getConfigManager().getDefaultSetName();

        String title = plugin.getLanguageManager().get(lang, "gui.main-title", "set", setName);
        MenuHolder holder = new MenuHolder(MenuHolder.MenuType.MAIN, page, null, favoritesOnly);
        Inventory inv = Bukkit.createInventory(holder, 54, trimTitle(title));
        holder.setInventory(inv);

        // Decorative header (0-8) & footer (45-53)
        ItemStack purpleGlass = plugin.getCompat().createStainedGlass((short) 10, "PURPLE_STAINED_GLASS_PANE");
        ItemStack blueGlass = plugin.getCompat().createStainedGlass((short) 11, "BLUE_STAINED_GLASS_PANE");
        setName(purpleGlass, " ");
        setName(blueGlass, " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, (i % 2 == 0) ? purpleGlass : blueGlass);
        for (int i = 45; i < 54; i++) inv.setItem(i, (i % 2 == 0) ? purpleGlass : blueGlass);

        List<Home> homes = selectedSet != null
                ? plugin.getHomeManager().getHomesInSet(player.getUniqueId(), selectedSet.getId())
                : plugin.getHomeManager().getHomes(player.getUniqueId());

        if (favoritesOnly) {
            homes = homes.stream().filter(Home::isFavorite).collect(Collectors.toList());
        }

        int perPage = 27;
        int start = page * perPage;
        for (int i = 0; i < perPage && (start + i) < homes.size(); i++) {
            Home home = homes.get(start + i);
            inv.setItem(9 + i, buildHomeItem(home));
        }

        if (page > 0) {
            inv.setItem(36, makeItem(plugin.getCompat().resolveMaterial("ARROW"),
                    plugin.getLanguageManager().get(lang, "gui.previous-page")));
        }
        inv.setItem(37, makeItem(plugin.getCompat().resolveMaterial("CHEST"),
                plugin.getLanguageManager().get(lang, "gui.homesets"),
                ChatColor.GRAY + "Current: " + ChatColor.AQUA + setName));
        inv.setItem(40, makeItem(plugin.getCompat().resolveMaterial("NETHER_STAR"),
                plugin.getLanguageManager().get(lang, "gui.create-home"),
                ChatColor.GRAY + "Use /sethome <name>"));
        inv.setItem(41, makeItem(plugin.getCompat().resolveMaterial("GOLD_INGOT"),
                plugin.getLanguageManager().get(lang, "gui.favorites"),
                favoritesOnly ? ChatColor.GREEN + "Filter: ON" : ChatColor.GRAY + "Filter: OFF"));
        inv.setItem(42, makeItem(plugin.getCompat().resolveMaterial("REDSTONE_COMPARATOR", "COMPARATOR"),
                plugin.getLanguageManager().get(lang, "gui.settings")));

        if (start + perPage < homes.size()) {
            inv.setItem(43, makeItem(plugin.getCompat().resolveMaterial("ARROW"),
                    plugin.getLanguageManager().get(lang, "gui.next-page")));
        }
        inv.setItem(44, makeItem(plugin.getCompat().resolveMaterial("BARRIER"),
                plugin.getLanguageManager().get(lang, "gui.close")));

        player.openInventory(inv);
        if (settings.isTeleportSounds() && plugin.getConfigManager().isGuiSounds()) {
            plugin.getCompat().playSound(player, "CLICK", 0.7f, 1.2f);
        }
    }

    public void openHomeSetsMenu(Player player) {
        PlayerSettings settings = plugin.getHomeManager().getSettings(player.getUniqueId());
        String lang = settings.getLanguage();
        String title = plugin.getLanguageManager().get(lang, "gui.homesets-title");
        MenuHolder holder = new MenuHolder(MenuHolder.MenuType.HOMESETS, 0, null, false);
        Inventory inv = Bukkit.createInventory(holder, 36, trimTitle(title));
        holder.setInventory(inv);

        List<HomeSet> sets = plugin.getHomeManager().getHomeSets(player.getUniqueId());
        for (int i = 0; i < Math.min(sets.size(), 27); i++) {
            HomeSet set = sets.get(i);
            int count = plugin.getHomeManager().getHomesInSet(player.getUniqueId(), set.getId()).size();
            boolean selected = set.getName().equalsIgnoreCase(settings.getSelectedSet());
            inv.setItem(i, makeItem(
                    plugin.getCompat().resolveMaterial(set.getIcon(), "CHEST"),
                    (selected ? ChatColor.GREEN + "✔ " : ChatColor.AQUA) + set.getName(),
                    ChatColor.GRAY + "Homes: " + ChatColor.WHITE + count,
                    selected ? ChatColor.GREEN + "Currently Selected" : ChatColor.YELLOW + "Click to select"
            ));
        }
        inv.setItem(31, makeItem(plugin.getCompat().resolveMaterial("ARROW"),
                plugin.getLanguageManager().get(lang, "gui.back")));
        player.openInventory(inv);
    }

    public void openHomeDetailsMenu(Player player, Home home) {
        String lang = plugin.getHomeManager().getSettings(player.getUniqueId()).getLanguage();
        String title = plugin.getLanguageManager().get(lang, "gui.details-title", "home", home.getName());
        MenuHolder holder = new MenuHolder(MenuHolder.MenuType.DETAILS, 0, home.getId(), false);
        Inventory inv = Bukkit.createInventory(holder, 27, trimTitle(title));
        holder.setInventory(inv);

        inv.setItem(10, makeItem(plugin.getCompat().resolveMaterial("ENDER_PEARL"), ChatColor.GREEN + "Teleport"));
        inv.setItem(11, makeItem(plugin.getCompat().resolveMaterial("COMPASS"), ChatColor.AQUA + "Copy Coordinates"));
        inv.setItem(12, makeItem(plugin.getCompat().resolveMaterial("GOLD_INGOT"),
                home.isFavorite() ? ChatColor.GOLD + "★ Unfavorite" : ChatColor.GRAY + "☆ Favorite"));
        inv.setItem(13, makeItem(plugin.getCompat().resolveMaterial("PAPER"), ChatColor.YELLOW + "Duplicate (/home duplicate)"));
        inv.setItem(14, makeItem(plugin.getCompat().resolveMaterial("BED", "RED_BED"), ChatColor.LIGHT_PURPLE + "Update Location"));
        inv.setItem(16, makeItem(plugin.getCompat().resolveMaterial("BARRIER"), ChatColor.RED + "Delete Home"));
        inv.setItem(22, makeItem(plugin.getCompat().resolveMaterial("ARROW"), plugin.getLanguageManager().get(lang, "gui.back")));

        player.openInventory(inv);
    }

    public void openSettingsMenu(Player player) {
        PlayerSettings s = plugin.getHomeManager().getSettings(player.getUniqueId());
        String lang = s.getLanguage();
        String title = plugin.getLanguageManager().get(lang, "gui.settings-title");
        MenuHolder holder = new MenuHolder(MenuHolder.MenuType.SETTINGS, 0, null, false);
        Inventory inv = Bukkit.createInventory(holder, 27, trimTitle(title));
        holder.setInventory(inv);

        inv.setItem(10, makeItem(plugin.getCompat().resolveMaterial("BOOK"),
                ChatColor.AQUA + "Language: " + ChatColor.WHITE + s.getLanguage().toUpperCase(),
                ChatColor.GRAY + "Click to switch EN / FA (Finglish)"));
        inv.setItem(11, makeItem(plugin.getCompat().resolveMaterial("NOTE_BLOCK"),
                ChatColor.AQUA + "Teleport Sounds: " + status(s.isTeleportSounds())));
        inv.setItem(12, makeItem(plugin.getCompat().resolveMaterial("BLAZE_POWDER"),
                ChatColor.AQUA + "Teleport Particles: " + status(s.isTeleportParticles())));
        inv.setItem(13, makeItem(plugin.getCompat().resolveMaterial("PAPER"),
                ChatColor.AQUA + "Notifications: " + status(s.isTeleportNotifications())));
        inv.setItem(14, makeItem(plugin.getCompat().resolveMaterial("NETHER_STAR"),
                ChatColor.AQUA + "GUI Animations: " + status(s.isGuiAnimations())));

        if (plugin.getConfigManager().isMakeHomeSafeEnabled()) {
            inv.setItem(15, makeItem(plugin.getCompat().resolveMaterial("SHIELD", "IRON_CHESTPLATE"),
                    ChatColor.AQUA + "Make Home Safe: " + status(s.isMakeHomeSafe()),
                    ChatColor.GRAY + "GriefPrevention: " + (plugin.getGriefPreventionHook().isAvailable() ? ChatColor.GREEN + "Detected" : ChatColor.RED + "Not Installed")));
        }

        inv.setItem(22, makeItem(plugin.getCompat().resolveMaterial("ARROW"), plugin.getLanguageManager().get(lang, "gui.back")));
        player.openInventory(inv);
    }

    public void openConfirmDeleteMenu(Player player, Home home) {
        String lang = plugin.getHomeManager().getSettings(player.getUniqueId()).getLanguage();
        String title = plugin.getLanguageManager().get(lang, "gui.confirm-title");
        MenuHolder holder = new MenuHolder(MenuHolder.MenuType.CONFIRM_DELETE, 0, home.getId(), false);
        Inventory inv = Bukkit.createInventory(holder, 27, trimTitle(title));
        holder.setInventory(inv);

        inv.setItem(11, makeItem(plugin.getCompat().resolveMaterial("EMERALD_BLOCK"),
                plugin.getLanguageManager().get(lang, "gui.confirm"),
                ChatColor.RED + "Delete: " + ChatColor.WHITE + home.getName()));
        inv.setItem(15, makeItem(plugin.getCompat().resolveMaterial("REDSTONE_BLOCK"),
                plugin.getLanguageManager().get(lang, "gui.cancel")));
        player.openInventory(inv);
    }

    public void openAdminMenu(Player player) {
        String lang = plugin.getHomeManager().getSettings(player.getUniqueId()).getLanguage();
        String title = plugin.getLanguageManager().get(lang, "gui.admin-title");
        MenuHolder holder = new MenuHolder(MenuHolder.MenuType.ADMIN, 0, null, false);
        Inventory inv = Bukkit.createInventory(holder, 27, trimTitle(title));
        holder.setInventory(inv);

        inv.setItem(11, makeItem(plugin.getCompat().resolveMaterial("COMMAND", "COMMAND_BLOCK"),
                ChatColor.GOLD + "Server Info",
                ChatColor.GRAY + "Compat: " + ChatColor.WHITE + plugin.getCompat().getVersionName(),
                ChatColor.GRAY + "Storage: " + ChatColor.WHITE + plugin.getConfigManager().getStorageType(),
                ChatColor.GRAY + "Vault: " + status(plugin.getEconomyManager().isAvailable()),
                ChatColor.GRAY + "GriefPrevention: " + status(plugin.getGriefPreventionHook().isAvailable())));
        inv.setItem(15, makeItem(plugin.getCompat().resolveMaterial("REDSTONE"),
                ChatColor.GREEN + "Reload Configuration",
                ChatColor.GRAY + "Click to reload config & languages"));
        inv.setItem(22, makeItem(plugin.getCompat().resolveMaterial("BARRIER"),
                plugin.getLanguageManager().get(lang, "gui.close")));
        player.openInventory(inv);
    }

    private ItemStack buildHomeItem(Home home) {
        Material mat = plugin.getCompat().resolveMaterial(home.getIcon(), "ENDER_PEARL");
        String display = (home.isFavorite() ? ChatColor.GOLD + "★ " : ChatColor.AQUA + "") + home.getName();
        return makeItem(mat, display,
                ChatColor.GRAY + "World: " + ChatColor.WHITE + home.getWorldName(),
                ChatColor.GRAY + String.format("Coords: %.1f, %.1f, %.1f", home.getX(), home.getY(), home.getZ()),
                ChatColor.GRAY + "Created: " + ChatColor.WHITE + dateFormat.format(new Date(home.getCreatedAt())),
                "",
                ChatColor.GREEN + "Left-Click: Teleport",
                ChatColor.YELLOW + "Right-Click: Manage",
                ChatColor.LIGHT_PURPLE + "Shift-Click: Toggle Favorite");
    }

    private ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void setName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
    }

    private String status(boolean val) {
        return val ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
    }

    private String trimTitle(String title) {
        return title.length() > 32 ? title.substring(0, 32) : title;
    }
}
