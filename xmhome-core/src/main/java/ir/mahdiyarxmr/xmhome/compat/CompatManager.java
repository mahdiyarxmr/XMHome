package ir.mahdiyarxmr.xmhome.compat;

import org.bukkit.Bukkit;

public class CompatManager {
    private final CompatibilityAdapter adapter;

    public CompatManager() {
        this.adapter = detectAdapter();
    }

    private CompatibilityAdapter detectAdapter() {
        String version = Bukkit.getBukkitVersion();
        boolean isLegacy = version.startsWith("1.8") || version.startsWith("1.9")
                || version.startsWith("1.10") || version.startsWith("1.11") || version.startsWith("1.12");

        String className = isLegacy
                ? "ir.mahdiyarxmr.xmhome.compat.legacy.LegacyAdapter"
                : "ir.mahdiyarxmr.xmhome.compat.modern.ModernAdapter";

        try {
            Class<?> clazz = Class.forName(className);
            return (CompatibilityAdapter) clazz.getDeclaredConstructor().newInstance();
        } catch (Throwable ignored) {
            // Fallback to the other adapter if only one module is present
            String fallback = isLegacy
                    ? "ir.mahdiyarxmr.xmhome.compat.modern.ModernAdapter"
                    : "ir.mahdiyarxmr.xmhome.compat.legacy.LegacyAdapter";
            try {
                Class<?> clazz = Class.forName(fallback);
                return (CompatibilityAdapter) clazz.getDeclaredConstructor().newInstance();
            } catch (Throwable e) {
                throw new IllegalStateException("No CompatibilityAdapter found on classpath!", e);
            }
        }
    }

    public CompatibilityAdapter getAdapter() {
        return adapter;
    }
}
