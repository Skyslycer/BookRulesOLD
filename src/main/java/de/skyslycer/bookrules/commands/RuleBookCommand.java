package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.core.BookManager;
import de.skyslycer.bookrules.core.MessageManager;
import de.skyslycer.bookrules.core.PermissionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RuleBookCommand implements CommandExecutor {
    BookManager bookManager;
    MessageManager messageManager;
    PermissionManager permissionManager;

    public void injectData(MessageManager messageManager, BookManager bookManager, PermissionManager permissionManager) {
        this.messageManager = messageManager;
        this.bookManager = bookManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PLAYER, sender);
            return false;
        }

        if (permissionManager.hasExtraPermission(sender, "bookrules.openbook")) {
            bookManager.openBook((Player) sender, "bookrules.openbook", true);
        } else messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, sender);
        return true;
    }

}
