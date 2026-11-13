package ir.mahdiyarxmr.xmhome.model;

import java.util.UUID;

public class PlayerSettings {
    private final UUID playerUuid;
    private String language;
    private String selectedSet;
    private boolean teleportSounds;
    private boolean teleportParticles;
    private boolean teleportNotifications;
    private boolean guiAnimations;
    private boolean confirmDelete;
    private boolean makeHomeSafe;

    public PlayerSettings(UUID playerUuid, String language, String selectedSet,
                          boolean teleportSounds, boolean teleportParticles,
                          boolean teleportNotifications, boolean guiAnimations,
                          boolean confirmDelete, boolean makeHomeSafe) {
        this.playerUuid = playerUuid;
        this.language = language;
        this.selectedSet = selectedSet;
        this.teleportSounds = teleportSounds;
        this.teleportParticles = teleportParticles;
        this.teleportNotifications = teleportNotifications;
        this.guiAnimations = guiAnimations;
        this.confirmDelete = confirmDelete;
        this.makeHomeSafe = makeHomeSafe;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSelectedSet() { return selectedSet; }
    public void setSelectedSet(String selectedSet) { this.selectedSet = selectedSet; }
    public boolean isTeleportSounds() { return teleportSounds; }
    public void setTeleportSounds(boolean teleportSounds) { this.teleportSounds = teleportSounds; }
    public boolean isTeleportParticles() { return teleportParticles; }
    public void setTeleportParticles(boolean teleportParticles) { this.teleportParticles = teleportParticles; }
    public boolean isTeleportNotifications() { return teleportNotifications; }
    public void setTeleportNotifications(boolean teleportNotifications) { this.teleportNotifications = teleportNotifications; }
    public boolean isGuiAnimations() { return guiAnimations; }
    public void setGuiAnimations(boolean guiAnimations) { this.guiAnimations = guiAnimations; }
    public boolean isConfirmDelete() { return confirmDelete; }
    public void setConfirmDelete(boolean confirmDelete) { this.confirmDelete = confirmDelete; }
    public boolean isMakeHomeSafe() { return makeHomeSafe; }
    public void setMakeHomeSafe(boolean makeHomeSafe) { this.makeHomeSafe = makeHomeSafe; }
}
