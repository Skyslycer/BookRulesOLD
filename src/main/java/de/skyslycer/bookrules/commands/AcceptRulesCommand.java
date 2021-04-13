package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.util.Data;
import de.skyslycer.bookrules.util.PlayerSaver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptRulesCommand implements CommandExecutor {
    Data data = BookRules.data;
    PlayerSaver playerSaver = new PlayerSaver();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(data.prefix + "§4Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        if(data.usePermissions) {
            if(!player.hasPermission("bookrules.rules")) {
                player.sendMessage(data.prefix + data.noPermission);
                BookRules.debug("Player " + player.getName() + " doesn't have permission (bookrules.rules), passing, no action taken.");
                return true;
            }
        }
        if(playerSaver.containsInFile(player.getUniqueId().toString(), "plugins//BookRules//players.txt")) {
            player.sendMessage(data.prefix + data.alreadyAccepted);
            BookRules.debug("Player " + player.getName() + " did accept the rules (is registered in players.txt), passing, no action taken.");
        }else {
            BookRules.debug("Player " + player.getName() + " didn't accept the rules (isn't registered in players.txt).");
            BookRules.debug("Registering player " + player.getName() + " in players.txt (player accepted the rules).");
            playerSaver.writeToFile(player.getUniqueId().toString(), "plugins//BookRules//players.txt");
            player.sendMessage(data.prefix + data.acceptRules);
            player.getOpenInventory().close();
        }
        return false;
    }
}
