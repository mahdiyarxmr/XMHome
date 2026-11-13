package ir.mahdiyarxmr.xmhome.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ir.mahdiyarxmr.xmhome.config.ConfigManager;
import ir.mahdiyarxmr.xmhome.model.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private HikariDataSource dataSource;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public DatabaseManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void init() throws SQLException {
        HikariConfig hikari = new HikariConfig();
        if ("MYSQL".equals(config.getStorageType())) {
            String host = config.raw().getString("database.mysql.host", "localhost");
            int port = config.raw().getInt("database.mysql.port", 3306);
            String db = config.raw().getString("database.mysql.database", "xmhome");
            String user = config.raw().getString("database.mysql.username", "root");
            String pass = config.raw().getString("database.mysql.password", "");
            int pool = config.raw().getInt("database.mysql.pool-size", 10);
            boolean ssl = config.raw().getBoolean("database.mysql.use-ssl", false);

            hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=" + ssl + "&autoReconnect=true");
            hikari.setUsername(user);
            hikari.setPassword(pass);
            hikari.setMaximumPoolSize(pool);
        } else {
            File dataDir = new File(plugin.getDataFolder(), "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dbFile = new File(dataDir, "xmhome.db");
            hikari.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikari.setMaximumPoolSize(1);
        }
        hikari.setPoolName("XMHome-DB");
        this.dataSource = new HikariDataSource(hikari);
        createTables();
    }

    private void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS xmhome_sets (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "owner VARCHAR(36) NOT NULL," +
                    "name VARCHAR(64) NOT NULL," +
                    "icon VARCHAR(64) NOT NULL," +
                    "description VARCHAR(255)," +
                    "is_default BOOLEAN NOT NULL," +
                    "favorite BOOLEAN NOT NULL," +
                    "is_public BOOLEAN NOT NULL," +
                    "created_at BIGINT NOT NULL" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS xmhome_homes (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "owner VARCHAR(36) NOT NULL," +
                    "set_id VARCHAR(36) NOT NULL," +
                    "name VARCHAR(64) NOT NULL," +
                    "world VARCHAR(64) NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "yaw FLOAT NOT NULL," +
                    "pitch FLOAT NOT NULL," +
                    "icon VARCHAR(64) NOT NULL," +
                    "description VARCHAR(255)," +
                    "category VARCHAR(64)," +
                    "favorite BOOLEAN NOT NULL," +
                    "is_public BOOLEAN NOT NULL," +
                    "created_at BIGINT NOT NULL," +
                    "last_visited_at BIGINT NOT NULL" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS xmhome_shares (" +
                    "home_id VARCHAR(36) NOT NULL," +
                    "shared_with VARCHAR(36) NOT NULL," +
                    "shared_with_name VARCHAR(32) NOT NULL," +
                    "permission VARCHAR(16) NOT NULL," +
                    "expires_at BIGINT NOT NULL," +
                    "PRIMARY KEY (home_id, shared_with)" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS xmhome_settings (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY," +
                    "language VARCHAR(8) NOT NULL," +
                    "selected_set VARCHAR(64) NOT NULL," +
                    "teleport_sounds BOOLEAN NOT NULL," +
                    "teleport_particles BOOLEAN NOT NULL," +
                    "teleport_notifications BOOLEAN NOT NULL," +
                    "gui_animations BOOLEAN NOT NULL," +
                    "confirm_delete BOOLEAN NOT NULL," +
                    "make_home_safe BOOLEAN NOT NULL" +
                    ")");
        }
    }

    public void close() {
        executor.shutdown();
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public CompletableFuture<Void> saveHomeAsync(Home home) {
        return CompletableFuture.runAsync(() -> {
            String sql = "REPLACE INTO xmhome_homes (id, owner, set_id, name, world, x, y, z, yaw, pitch, icon, description, category, favorite, is_public, created_at, last_visited_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, home.getId().toString());
                ps.setString(2, home.getOwner().toString());
                ps.setString(3, home.getSetId().toString());
                ps.setString(4, home.getName());
                ps.setString(5, home.getWorldName());
                ps.setDouble(6, home.getX());
                ps.setDouble(7, home.getY());
                ps.setDouble(8, home.getZ());
                ps.setFloat(9, home.getYaw());
                ps.setFloat(10, home.getPitch());
                ps.setString(11, home.getIcon());
                ps.setString(12, home.getDescription());
                ps.setString(13, home.getCategory());
                ps.setBoolean(14, home.isFavorite());
                ps.setBoolean(15, home.isPublic());
                ps.setLong(16, home.getCreatedAt());
                ps.setLong(17, home.getLastVisitedAt());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save home: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Void> deleteHomeAsync(UUID homeId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM xmhome_shares WHERE home_id = ?")) {
                    ps.setString(1, homeId.toString());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM xmhome_homes WHERE id = ?")) {
                    ps.setString(1, homeId.toString());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete home: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Void> saveHomeSetAsync(HomeSet set) {
        return CompletableFuture.runAsync(() -> {
            String sql = "REPLACE INTO xmhome_sets (id, owner, name, icon, description, is_default, favorite, is_public, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, set.getId().toString());
                ps.setString(2, set.getOwner().toString());
                ps.setString(3, set.getName());
                ps.setString(4, set.getIcon());
                ps.setString(5, set.getDescription());
                ps.setBoolean(6, set.isDefault());
                ps.setBoolean(7, set.isFavorite());
                ps.setBoolean(8, set.isPublic());
                ps.setLong(9, set.getCreatedAt());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save home set: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Void> deleteHomeSetAsync(UUID setId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM xmhome_sets WHERE id = ?")) {
                ps.setString(1, setId.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete home set: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Void> saveSettingsAsync(PlayerSettings s) {
        return CompletableFuture.runAsync(() -> {
            String sql = "REPLACE INTO xmhome_settings (player_uuid, language, selected_set, teleport_sounds, teleport_particles, teleport_notifications, gui_animations, confirm_delete, make_home_safe) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, s.getPlayerUuid().toString());
                ps.setString(2, s.getLanguage());
                ps.setString(3, s.getSelectedSet());
                ps.setBoolean(4, s.isTeleportSounds());
                ps.setBoolean(5, s.isTeleportParticles());
                ps.setBoolean(6, s.isTeleportNotifications());
                ps.setBoolean(7, s.isGuiAnimations());
                ps.setBoolean(8, s.isConfirmDelete());
                ps.setBoolean(9, s.isMakeHomeSafe());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save player settings: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<List<Home>> loadHomesAsync(UUID owner) {
        return CompletableFuture.supplyAsync(() -> {
            List<Home> list = new ArrayList<>();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM xmhome_homes WHERE owner = ?")) {
                ps.setString(1, owner.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Home(
                                UUID.fromString(rs.getString("id")),
                                UUID.fromString(rs.getString("owner")),
                                UUID.fromString(rs.getString("set_id")),
                                rs.getString("name"),
                                rs.getString("world"),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("yaw"),
                                rs.getFloat("pitch"),
                                rs.getString("icon"),
                                rs.getString("description"),
                                rs.getString("category"),
                                rs.getBoolean("favorite"),
                                rs.getBoolean("is_public"),
                                rs.getLong("created_at"),
                                rs.getLong("last_visited_at")
                        ));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load homes: " + e.getMessage());
            }
            return list;
        }, executor);
    }

    public CompletableFuture<List<HomeSet>> loadHomeSetsAsync(UUID owner) {
        return CompletableFuture.supplyAsync(() -> {
            List<HomeSet> list = new ArrayList<>();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM xmhome_sets WHERE owner = ?")) {
                ps.setString(1, owner.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new HomeSet(
                                UUID.fromString(rs.getString("id")),
                                UUID.fromString(rs.getString("owner")),
                                rs.getString("name"),
                                rs.getString("icon"),
                                rs.getString("description"),
                                rs.getBoolean("is_default"),
                                rs.getBoolean("favorite"),
                                rs.getBoolean("is_public"),
                                rs.getLong("created_at")
                        ));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load home sets: " + e.getMessage());
            }
            return list;
        }, executor);
    }

    public CompletableFuture<PlayerSettings> loadSettingsAsync(UUID owner, String defaultLang, String defaultSet) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM xmhome_settings WHERE player_uuid = ?")) {
                ps.setString(1, owner.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new PlayerSettings(
                                owner,
                                rs.getString("language"),
                                rs.getString("selected_set"),
                                rs.getBoolean("teleport_sounds"),
                                rs.getBoolean("teleport_particles"),
                                rs.getBoolean("teleport_notifications"),
                                rs.getBoolean("gui_animations"),
                                rs.getBoolean("confirm_delete"),
                                rs.getBoolean("make_home_safe")
                        );
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load player settings: " + e.getMessage());
            }
            return new PlayerSettings(owner, defaultLang, defaultSet, true, true, true, true, true, false);
        }, executor);
    }

    public CompletableFuture<Void> saveShareAsync(HomeShare share) {
        return CompletableFuture.runAsync(() -> {
            String sql = "REPLACE INTO xmhome_shares (home_id, shared_with, shared_with_name, permission, expires_at) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, share.getHomeId().toString());
                ps.setString(2, share.getSharedWith().toString());
                ps.setString(3, share.getSharedWithName());
                ps.setString(4, share.getPermission().name());
                ps.setLong(5, share.getExpiresAt());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save share: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Void> deleteShareAsync(UUID homeId, UUID sharedWith) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM xmhome_shares WHERE home_id = ? AND shared_with = ?")) {
                ps.setString(1, homeId.toString());
                ps.setString(2, sharedWith.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete share: " + e.getMessage());
            }
        }, executor);
    }
}
