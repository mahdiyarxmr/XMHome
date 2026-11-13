package ir.mahdiyarxmr.xmhome.compat;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface CompatibilityAdapter {
    String getVersionName();
    boolean isModern();

    void sendActionBar(Player player, String message);
    void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);
    void playSound(Player player, String soundKey, float volume, float pitch);
    void spawnParticle(Player player, Location location, String particleKey, int count);
    void sendCopyableCoords(Player player, String displayText, String copyText);

    ItemStack createStainedGlass(short legacyData, String modernMaterialName);
    Material resolveMaterial(String... names);
}
