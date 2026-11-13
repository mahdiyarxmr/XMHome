package ir.mahdiyarxmr.xmhome.compat;

import org.bukkit.Material;

public final class MaterialCompat {
    private MaterialCompat() {}

    public static Material match(String... names) {
        for (String name : names) {
            if (name == null || name.isEmpty()) continue;
            Material mat = Material.matchMaterial(name.toUpperCase());
            if (mat != null) return mat;
        }
        return Material.STONE;
    }
}
