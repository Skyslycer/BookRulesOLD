package de.skyslycer.bookrules.util;

import org.bukkit.Bukkit;

public enum MCVersion {

    vUNKNOWN(-1, -1),
    v1_8_R1(81, 8),
    v1_8_R2(82, 8),
    v1_8_R3(83, 8),
    v1_9_R1(91, 9),
    v1_9_R2(92, 9),
    v1_10_R1(100, 10),
    v1_11_R1(110, 11),
    v1_12_R1(120, 12),
    v1_13_R1(131, 13),
    v1_13_R2(132, 13),
    v1_14_R1(140, 14),
    v1_15_R1(151, 15),
    v1_16_R1(161, 16),
    v1_16_R2(162, 16),
    v1_16_R3(163, 16);

    private static MCVersion version;
    private int id;
    private int majorId;

    MCVersion(int id, int majorId) {
        this.id = id;
        this.majorId = majorId;
    }

    public boolean isNewerThan(MCVersion version) {
        return this.id > version.id;
    }

    public boolean isOlderThan(MCVersion version) {
        return this.id < version.id;
    }

    public boolean isMajorNewerThan(int majorVersion) {
        return majorId > majorVersion;
    }

    public boolean isMajorOlderThan(int majorVersion) {
        return majorId < majorVersion;
    }

    public boolean isEqual(MCVersion version) {
        return id == version.id;
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
        return version1.id <= id && id <= version2.id;
    }

    public boolean isMajorBetween(int major1, int major2) {
        return major1 <= majorId && majorId <= major2;
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
}