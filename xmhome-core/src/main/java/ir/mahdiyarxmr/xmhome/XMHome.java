package ir.mahdiyarxmr.xmhome;

import ir.mahdiyarxmr.xmhome.command.*;
import ir.mahdiyarxmr.xmhome.compat.CompatManager;
import ir.mahdiyarxmr.xmhome.compat.CompatibilityAdapter;
import ir.mahdiyarxmr.xmhome.config.ConfigManager;
import ir.mahdiyarxmr.xmhome.config.LanguageManager;
import ir.mahdiyarxmr.xmhome.database.DatabaseManager;
import ir.mahdiyarxmr.xmhome.economy.EconomyManager;
import ir.mahdiyarxmr.xmhome.gui.GUIListener;
import ir.mahdiyarxmr.xmhome.gui.GUIManager;
import ir.mahdiyarxmr.xmhome.hooks.GriefPreventionHook;
import ir.mahdiyarxmr.xmhome.hooks.PlaceholderAPIHook;
import ir.mahdiyarxmr.xmhome.manager.HomeManager;
import ir.mahdiyarxmr.xmhome.manager.ProtectionManager;
import ir.mahdiyarxmr.xmhome.manager.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class XMHome extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private CompatibilityAdapter compat;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private GriefPreventionHook griefPreventionHook;
    private HomeManager homeManager;
    private ProtectionManager protectionManager;
    private TeleportManager teleportManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this, configManager);
        this.compat = new CompatManager().getAdapter();

        this.databaseManager = new DatabaseManager(this, configManager);
        try {
            this.databaseManager.init();
        } catch (SQLException e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.economyManager = new EconomyManager(this, configManager);
        this.griefPreventionHook = new GriefPreventionHook();
        this.homeManager = new HomeManager(this);
        this.protectionManager = new ProtectionManager(this);
        this.teleportManager = new TeleportManager(this);
        this.guiManager = new GUIManager(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(teleportManager, this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        registerCommands();

        for (Player online : Bukkit.getOnlinePlayers()) {
            homeManager.loadPlayer(online);
        }

        getLogger().info("XMHome v" + getDescription().getVersion() + " enabled (" + compat.getVersionName() + ")");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("XMHome disabled.");
    }

    private void registerCommands() {
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("homes").setExecutor(new HomesCommand(this));
        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("homeset").setExecutor(new HomeSetCommand(this));
        getCommand("homesets").setExecutor(new HomeSetsCommand(this));
        getCommand("homecoords").setExecutor(new HomeCoordsCommand(this));
        getCommand("homeshare").setExecutor(new HomeShareCommand(this));
        getCommand("homeunshare").setExecutor(new HomeUnshareCommand(this));
        getCommand("homeinfo").setExecutor(new HomeInfoCommand(this));
        getCommand("homefavorite").setExecutor(new HomeFavoriteCommand(this));
        getCommand("homeadmin").setExecutor(new HomeAdminCommand(this));
        getCommand("xmhome").setExecutor(new XMHomeCommand(this));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        homeManager.loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        homeManager.unloadPlayer(event.getPlayer().getUniqueId());
    }

    public ConfigManager getConfigManager() { return configManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public CompatibilityAdapter getCompat() { return compat; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public GriefPreventionHook getGriefPreventionHook() { return griefPreventionHook; }
    public HomeManager getHomeManager() { return homeManager; }
    public ProtectionManager getProtectionManager() { return protectionManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    public GUIManager getGuiManager() { return guiManager; }
}
