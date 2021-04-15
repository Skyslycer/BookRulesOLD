package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.util.Data;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeclineRulesCommand implements CommandExecutor {
    Data data = BookRules.data;
    String kickText = data.kickText;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(data.prefix + "§4Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        BookRules.debug("Player " + player.getName() + " clicked the decline button or ran the command '/declinerules'");

        if(data.usePermissions) {
            if(!player.hasPermission("bookrules.decline")) {
                player.sendMessage(data.prefix + data.noPermission);
                BookRules.debug("Player " + player.getName() + " doesn't have permission (bookrules.rules), passing, no action taken.");
                return true;
            }
        }

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            kickText = PlaceholderAPI.setPlaceholders(player, kickText);
        }

        if(data.players.contains(player.getUniqueId().toString())) {
            BookRules.debug("Player " + player.getName() + " did accept the rules (is registered in players.txt), removing entry, caused by player.");
            data.players.remove(player.getUniqueId().toString());
        }else {
            BookRules.debug("Player " + player.getName() + " didn't accept the rules (isn't registered in players.txt).");
        }
        BookRules.debug("Kicking player " + player.getName() + " for declining the rules.");
        player.kickPlayer(kickText);
        return false;
    }
}
