package ir.mahdiyarxmr.xmhome.compat.legacy;

import ir.mahdiyarxmr.xmhome.compat.CompatibilityAdapter;
import ir.mahdiyarxmr.xmhome.compat.MaterialCompat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class LegacyAdapter implements CompatibilityAdapter {

    @Override
    public String getVersionName() {
        return "Legacy (1.8.8-1.12.2)";
    }

    @Override
    public boolean isModern() {
        return false;
    }

    @Override
    public void sendActionBar(Player player, String message) {
        try {
            String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object connection = handle.getClass().getField("playerConnection").get(handle);

            Class<?> icbcClass = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
            Class<?> chatSerializer = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent$ChatSerializer");
            Method aMethod = chatSerializer.getMethod("a", String.class);
            Object chatComponent = aMethod.invoke(null, "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', message).replace("\"", "\\\"") + "\"}");

            Class<?> packetClass = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
            Constructor<?> constructor = packetClass.getConstructor(icbcClass, byte.class);
            Object packet = constructor.newInstance(chatComponent, (byte) 2);

            Class<?> packetBase = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
            connection.getClass().getMethod("sendPacket", packetBase).invoke(connection, packet);
        } catch (Throwable ignored) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(
                ChatColor.translateAlternateColorCodes('&', title),
                ChatColor.translateAlternateColorCodes('&', subtitle)
        );
    }

    @Override
    public void playSound(Player player, String soundKey, float volume, float pitch) {
        String mapped;
        switch (soundKey.toUpperCase()) {
            case "TELEPORT": mapped = "ENDERMAN_TELEPORT"; break;
            case "CLICK": mapped = "CLICK"; break;
            case "SUCCESS": mapped = "ORB_PICKUP"; break;
            case "ERROR": mapped = "VILLAGER_NO"; break;
            case "LEVEL_UP": mapped = "LEVEL_UP"; break;
            default: mapped = soundKey; break;
        }
        try {
            Sound sound = Sound.valueOf(mapped);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public void spawnParticle(Player player, Location location, String particleKey, int count) {
        if (location == null || location.getWorld() == null) return;
        try {
            Effect effect = "HEART".equalsIgnoreCase(particleKey) ? Effect.HEART : Effect.PORTAL;
            for (int i = 0; i < Math.min(count, 10); i++) {
                location.getWorld().playEffect(location, effect, 0);
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void sendCopyableCoords(Player player, String displayText, String copyText) {
        // 1.8.8 does not support COPY_TO_CLIPBOARD; readable chat output fallback
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', displayText));
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack createStainedGlass(short legacyData, String modernMaterialName) {
        Material mat = Material.matchMaterial("STAINED_GLASS_PANE");
        if (mat == null) mat = Material.GLASS;
        return new ItemStack(mat, 1, legacyData);
    }

    @Override
    public Material resolveMaterial(String... names) {
        return MaterialCompat.match(names);
    }
}
