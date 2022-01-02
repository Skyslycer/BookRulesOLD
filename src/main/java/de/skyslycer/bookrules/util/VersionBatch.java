package de.skyslycer.bookrules.util;

public class VersionBatch {

    private final int major;
    private final int minor;
    private final int patch;

    public VersionBatch(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public boolean isOlderThan(VersionBatch versionBatch) {
        if (versionBatch == null) {
            return true;
        }

        return versionBatch.major > major || versionBatch.minor > minor || versionBatch.patch > patch;
    }

    public static VersionBatch fromString(String version) {
        var parts = version.split("\\.");

        if (parts.length !=  3) {
            return null;
        }

        try {
            return new VersionBatch(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (Exception ignored) {
            return null;
        }
    }

}
