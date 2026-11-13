package ir.mahdiyarxmr.xmhome.manager;

import ir.mahdiyarxmr.xmhome.XMHome;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ProtectionManager {
    private final XMHome plugin;

    public ProtectionManager(XMHome plugin) {
        this.plugin = plugin;
    }

    public boolean isSafeLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        Block feet = loc.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);

        if (isDangerous(feet.getType()) || isDangerous(head.getType()) || isDangerous(ground.getType())) {
            return false;
        }
        if (feet.getType().isSolid() || head.getType().isSolid()) {
            return false;
        }
        return ground.getType().isSolid();
    }

    private boolean isDangerous(Material mat) {
        String name = mat.name();
        return name.contains("LAVA") || name.contains("FIRE") || name.equals("CACTUS") || name.equals("MAGMA_BLOCK");
    }

    public boolean canAccessHomeArea(Player player, Location loc) {
        if (!plugin.getConfigManager().isGriefPreventionEnabled()) return true;
        if (!plugin.getGriefPreventionHook().isAvailable()) return true;
        return plugin.getGriefPreventionHook().hasAccess(player, loc);
    }
}
