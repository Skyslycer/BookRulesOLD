package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.BookRules;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RuleBookCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final BookRules bookRules = BookRules.getInstance();

        if(!(sender instanceof Player)) {
            sender.sendMessage(bookRules.getData().prefix + "§4Only players can use this command!");
            return false;
        }

        bookRules.openBook((Player) sender);
        return true;
    }
}
