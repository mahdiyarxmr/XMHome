package ir.mahdiyarxmr.xmhome.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Home {
    private final UUID id;
    private final UUID owner;
    private UUID setId;
    private String name;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private String icon;
    private String description;
    private String category;
    private boolean favorite;
    private boolean isPublic;
    private final long createdAt;
    private long lastVisitedAt;

    public Home(UUID id, UUID owner, UUID setId, String name, String worldName,
                double x, double y, double z, float yaw, float pitch,
                String icon, String description, String category,
                boolean favorite, boolean isPublic, long createdAt, long lastVisitedAt) {
        this.id = id;
        this.owner = owner;
        this.setId = setId;
        this.name = name;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.icon = icon != null ? icon : "ENDER_PEARL";
        this.description = description != null ? description : "";
        this.category = category != null ? category : "General";
        this.favorite = favorite;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.lastVisitedAt = lastVisitedAt;
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public UUID getSetId() { return setId; }
    public void setSetId(UUID setId) { this.setId = setId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public long getCreatedAt() { return createdAt; }
    public long getLastVisitedAt() { return lastVisitedAt; }
    public void setLastVisitedAt(long lastVisitedAt) { this.lastVisitedAt = lastVisitedAt; }
}
