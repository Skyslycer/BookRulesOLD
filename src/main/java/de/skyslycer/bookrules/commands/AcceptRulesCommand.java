package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.util.Data;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptRulesCommand implements CommandExecutor {
    Data data = BookRules.data;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(data.prefix + "§4Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        BookRules.debug("Player " + player.getName() + " clicked the decline button or ran the command '/acceptrules'");
        if(data.usePermissions) {
            if(!player.hasPermission("bookrules.rules")) {
                player.sendMessage(data.prefix + data.noPermission);
                BookRules.debug("Player " + player.getName() + " doesn't have permission (bookrules.accept), passing, no action taken.");
                return true;
            }
        }
        if(data.players.contains(player.getUniqueId().toString())) {
            player.sendMessage(data.prefix + data.alreadyAccepted);
            BookRules.debug("Player " + player.getName() + " did accept the rules (is registered in players.txt), passing, no action taken.");
        }else {
            BookRules.debug("Player " + player.getName() + " didn't accept the rules (isn't registered in players.txt).");
            BookRules.debug("Registering player " + player.getName() + " in players.txt (player accepted the rules).");
            data.players.add(player.getUniqueId().toString());
            player.sendMessage(data.prefix + data.acceptRules);
            player.getOpenInventory().close();
        }
        return false;
    }
}
