package de.skyslycer.bookrules.util;

import org.bukkit.Bukkit;

public enum MCVersion {

    vUNKNOWN(-1),
    v1_8_R1(8),
    v1_8_R2(8),
    v1_8_R3(8),
    v1_9_R1(9),
    v1_9_R2(9),
    v1_10_R1(10),
    v1_11_R1(11),
    v1_12_R1(12),
    v1_13_R1(13),
    v1_13_R2(13),
    v1_14_R1(14),
    v1_15_R1(15),
    v1_16_R1(16),
    v1_16_R2(16),
    v1_16_R3(16),
    v1_17_R1(17),
    v1_17_R2(17),
    v1_18_R1(18),
    v1_18_R2(18);

    private static MCVersion version;
    private final int majorId;

    MCVersion(int majorId) {
        this.majorId = majorId;
    }

    public static MCVersion getVersion() {
        if (version != null) return version;
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String versionPackage = name.substring(name.lastIndexOf('.') + 1) + ".";
        for (MCVersion version : values()) {
            if (versionPackage.contains(version.toString())) {
                MCVersion.version = version;
                return version;
            }
        }
        version = vUNKNOWN;
        return MCVersion.vUNKNOWN;
    }

    public boolean isMajorNewerThan(int majorVersion) {
        return majorId > majorVersion;
    }

    public boolean isMajorOlderThan(int majorVersion) {
        return majorId < majorVersion;
    }

    public boolean isSameMajor(MCVersion version) {
        return majorId == version.majorId;
    }

    public boolean isSameMajor(int majorVersion) {
        return majorId == majorVersion;
    }

    public int getMajorVersion() {
        return majorId;
    }

    public boolean isBetween(MCVersion version1, MCVersion version2) {
        return version1.majorId <= majorId && majorId <= version2.majorId;
    }

    public boolean isMajorBetween(int major1, int major2) {
        return major1 <= majorId && majorId <= major2;
    }
}