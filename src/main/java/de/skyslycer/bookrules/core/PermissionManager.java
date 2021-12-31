package de.skyslycer.bookrules.core;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Path;

public class PermissionManager {
    public boolean extraPermissions;

    public void instantiatePermissions(FileConfiguration configFile, Path configPath, MessageManager messageManager) {
        if (configFile.getString("use-permissions") == null) {
            configFile.set("use-permissions", false);
        }
        extraPermissions = configFile.getBoolean("use-permissions");
        messageManager.sendDebug("Extra permissions: " + extraPermissions);

        try {
            configFile.save(configPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasExtraPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || !extraPermissions;
    }

    public boolean hasExtraPermission(Player player, String permission) {
        return player.hasPermission(permission) || !extraPermissions;
    }
}
