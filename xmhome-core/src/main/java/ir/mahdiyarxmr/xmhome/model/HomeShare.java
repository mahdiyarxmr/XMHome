package ir.mahdiyarxmr.xmhome.model;

import java.util.UUID;

public class HomeShare {
    private final UUID homeId;
    private final UUID sharedWith;
    private final String sharedWithName;
    private SharePermission permission;
    private final long expiresAt;

    public HomeShare(UUID homeId, UUID sharedWith, String sharedWithName, SharePermission permission, long expiresAt) {
        this.homeId = homeId;
        this.sharedWith = sharedWith;
        this.sharedWithName = sharedWithName;
        this.permission = permission;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }

    public UUID getHomeId() { return homeId; }
    public UUID getSharedWith() { return sharedWith; }
    public String getSharedWithName() { return sharedWithName; }
    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }
    public long getExpiresAt() { return expiresAt; }
}
