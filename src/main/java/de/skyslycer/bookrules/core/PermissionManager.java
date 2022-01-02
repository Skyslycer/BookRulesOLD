package de.skyslycer.bookrules.core;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Path;

public class PermissionManager {
    public boolean extraPermissions;

    public void instantiatePermissions(FileConfiguration configFile, MessageManager messageManager) {
        if (configFile.getString("use-permissions") == null) {
            configFile.set("use-permissions", false);
        }
        extraPermissions = configFile.getBoolean("use-permissions");
        messageManager.sendDebug("Extra permissions: " + extraPermissions);
    }

    public boolean hasExtraPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || !extraPermissions;
    }
}
