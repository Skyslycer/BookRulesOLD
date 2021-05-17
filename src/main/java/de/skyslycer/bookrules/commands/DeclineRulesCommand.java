package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.core.MessageManager;
import de.skyslycer.bookrules.core.PermissionManager;
import de.skyslycer.bookrules.api.RulesAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeclineRulesCommand implements CommandExecutor {

    MessageManager messageManager;
    PermissionManager permissionManager;
    String kickText;
    RulesAPI rulesAPI = new RulesAPI();

    public void injectData(MessageManager messageManager, PermissionManager permissionManager) {
        this.messageManager = messageManager;
        this.permissionManager = permissionManager;
        this.kickText = messageManager.kickText;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PLAYER, sender);
            return true;
        }

        Player player = (Player) sender;
        messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINING, player.getName());

        if (!permissionManager.hasExtraPermission(player, "bookrules.decline")) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, player);
            messageManager.sendDebug(MessageManager.DebugType.DEBUG_NO_PERMISSION, player.getName(), "bookrules.decline");
            return true;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            kickText = PlaceholderAPI.setPlaceholders(player, kickText);
        }

        if (rulesAPI.playerHasAcceptedRules(player.getUniqueId().toString())) {
            messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTED, player.getName());
            rulesAPI.declineRules(player.getUniqueId().toString());
        } else {
            messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINED, player.getName());
        }
        messageManager.sendDebug(MessageManager.DebugType.DEBUG_KICK, player.getName());
        player.kickPlayer(kickText);
        return false;
    }
    
}
