package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.api.RulesAPI;
import de.skyslycer.bookrules.core.MessageManager;
import de.skyslycer.bookrules.core.PermissionManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class AcceptRulesCommand implements CommandExecutor {

    RulesAPI rulesAPI = new RulesAPI();
    MessageManager messageManager;
    PermissionManager permissionManager;
    Map<Player, Location> playerCache;

    public void injectData(MessageManager messageManager, Map<Player, Location> playerCache, PermissionManager permissionManager) {
        this.messageManager = messageManager;
        this.permissionManager = permissionManager;
        this.playerCache = playerCache;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PLAYER, sender);
            return true;
        }
        Player player = (Player) sender;
        messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINING, player.getName());
        if (!permissionManager.hasExtraPermission(player, "bookrules.accept")) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, player);
            messageManager.sendDebug(MessageManager.DebugType.DEBUG_NO_PERMISSION, player.getName(), "bookrules.accept");
            return true;
        }

        rulesAPI.playerHasAcceptedRules(player.getUniqueId().toString()).thenAccept((hasAccepted) -> {
            if (hasAccepted) {
                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_ALREADY_ACCEPTED, player);
                messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTED, player.getName());
            } else  {
                messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINED, player.getName());
                messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTING, player.getName());
                rulesAPI.acceptRules(player.getUniqueId().toString());
                playerCache.remove(player);
                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_ACCEPT_RULES, player);
                player.getOpenInventory().close();
            }
        });
        return false;
    }

}
