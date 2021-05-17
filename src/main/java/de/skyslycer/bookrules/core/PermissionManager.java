package de.skyslycer.bookrules.core;

import de.skyslycer.bookrules.util.YamlFileWriter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionManager {
    public boolean extraPermissions;

    public void instantiatePermissions(YamlFileWriter configFile, MessageManager messageManager) {
        if(configFile.getString("use-permissions") == null) {
            configFile.setValue("use-permissions", false);
        }
        extraPermissions = configFile.getBoolean("use-permissions");
        messageManager.sendDebug("Extra permissions: " + extraPermissions);
    }

    public boolean hasExtraPermission(CommandSender sender, String permission) {
        if(sender.hasPermission(permission) || !extraPermissions) {
            return true;
        }else return false;
    }

    public boolean hasExtraPermission(Player player, String permission) {
        if(player.hasPermission(permission) || !extraPermissions) {
            return true;
        }else return false;
    }
}
