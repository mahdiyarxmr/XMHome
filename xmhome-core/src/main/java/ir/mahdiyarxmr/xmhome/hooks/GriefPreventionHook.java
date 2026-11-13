package ir.mahdiyarxmr.xmhome.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class GriefPreventionHook {
    private boolean available = false;
    private Object dataStore;
    private Method getClaimAtMethod;

    public GriefPreventionHook() {
        try {
            if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) {
                Class<?> gpClass = Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
                Object instance = gpClass.getField("instance").get(null);
                this.dataStore = gpClass.getField("dataStore").get(instance);
                Class<?> claimClass = Class.forName("me.ryanhamshire.GriefPrevention.Claim");
                this.getClaimAtMethod = dataStore.getClass().getMethod("getClaimAt", Location.class, boolean.class, claimClass);
                this.available = true;
            }
        } catch (Throwable ignored) {
            this.available = false;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isInsideClaim(Location location) {
        if (!available || location == null) return false;
        try {
            Object claim = getClaimAtMethod.invoke(dataStore, location, false, null);
            return claim != null;
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean hasAccess(Player player, Location location) {
        if (!available || location == null) return true;
        try {
            Object claim = getClaimAtMethod.invoke(dataStore, location, false, null);
            if (claim == null) return true;
            Method allowAccess = claim.getClass().getMethod("allowAccess", Player.class);
            Object denialReason = allowAccess.invoke(claim, player);
            return denialReason == null;
        } catch (Throwable e) {
            return true;
        }
    }
}
