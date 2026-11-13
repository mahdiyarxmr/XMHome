package ir.mahdiyarxmr.xmhome.compat.modern;

import ir.mahdiyarxmr.xmhome.compat.CompatibilityAdapter;
import ir.mahdiyarxmr.xmhome.compat.MaterialCompat;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ModernAdapter implements CompatibilityAdapter {

    @Override
    public String getVersionName() {
        return "Modern (1.13-1.21+)";
    }

    @Override
    public boolean isModern() {
        return true;
    }

    @Override
    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message))
        );
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(
                ChatColor.translateAlternateColorCodes('&', title),
                ChatColor.translateAlternateColorCodes('&', subtitle),
                fadeIn, stay, fadeOut
        );
    }

    @Override
    public void playSound(Player player, String soundKey, float volume, float pitch) {
        String mapped;
        switch (soundKey.toUpperCase()) {
            case "TELEPORT": mapped = "ENTITY_ENDERMAN_TELEPORT"; break;
            case "CLICK": mapped = "UI_BUTTON_CLICK"; break;
            case "SUCCESS": mapped = "ENTITY_EXPERIENCE_ORB_PICKUP"; break;
            case "ERROR": mapped = "ENTITY_VILLAGER_NO"; break;
            case "LEVEL_UP": mapped = "ENTITY_PLAYER_LEVELUP"; break;
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
            Particle particle = "HEART".equalsIgnoreCase(particleKey) ? Particle.HEART : Particle.PORTAL;
            player.spawnParticle(particle, location, Math.min(count, 30), 0.35, 0.5, 0.35, 0.05);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void sendCopyableCoords(Player player, String displayText, String copyText) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(
                ChatColor.translateAlternateColorCodes('&', displayText)
        ));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText));
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatColor.GREEN + "Click to copy: " + ChatColor.WHITE + copyText).create()
        ));
        player.spigot().sendMessage(component);
    }

    @Override
    public ItemStack createStainedGlass(short legacyData, String modernMaterialName) {
        Material mat = MaterialCompat.match(modernMaterialName, "PURPLE_STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE");
        return new ItemStack(mat, 1);
    }

    @Override
    public Material resolveMaterial(String... names) {
        return MaterialCompat.match(names);
    }
}
