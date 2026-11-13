package ir.mahdiyarxmr.xmhome.model;

import java.util.UUID;

public class HomeSet {
    private final UUID id;
    private final UUID owner;
    private String name;
    private String icon;
    private String description;
    private boolean isDefault;
    private boolean favorite;
    private boolean isPublic;
    private final long createdAt;

    public HomeSet(UUID id, UUID owner, String name, String icon, String description,
                   boolean isDefault, boolean favorite, boolean isPublic, long createdAt) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.icon = icon != null ? icon : "CHEST";
        this.description = description != null ? description : "";
        this.isDefault = isDefault;
        this.favorite = favorite;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public long getCreatedAt() { return createdAt; }
}
