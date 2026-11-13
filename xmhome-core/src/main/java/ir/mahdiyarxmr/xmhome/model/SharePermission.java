package ir.mahdiyarxmr.xmhome.model;

public enum SharePermission {
    VISIT,
    VIEW,
    MANAGE;

    public static SharePermission fromString(String value) {
        if (value == null) return VISIT;
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return VISIT;
        }
    }
}
