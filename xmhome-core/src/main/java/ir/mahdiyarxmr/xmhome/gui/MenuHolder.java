package ir.mahdiyarxmr.xmhome.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class MenuHolder implements InventoryHolder {
    public enum MenuType {
        MAIN,
        HOMESETS,
        DETAILS,
        SETTINGS,
        CONFIRM_DELETE,
        ADMIN
    }

    private final MenuType type;
    private final int page;
    private final UUID targetId;
    private final boolean favoritesOnly;
    private Inventory inventory;

    public MenuHolder(MenuType type, int page, UUID targetId, boolean favoritesOnly) {
        this.type = type;
        this.page = page;
        this.targetId = targetId;
        this.favoritesOnly = favoritesOnly;
    }

    public MenuType getType() { return type; }
    public int getPage() { return page; }
    public UUID getTargetId() { return targetId; }
    public boolean isFavoritesOnly() { return favoritesOnly; }

    @Override
    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
}
