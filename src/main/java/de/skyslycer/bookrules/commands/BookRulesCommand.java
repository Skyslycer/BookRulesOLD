package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.util.Data;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BookRulesCommand implements CommandExecutor, TabCompleter {
    Data data = BookRules.data;

    String kickText = data.kickText;
    String message;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            message = "§7[§cBookRules§7] §cBookRules §7by §cSkyslycer\n";
            if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload") || sender.hasPermission("bookrules.acceptrules") || sender.hasPermission("bookrules.declinerules") || sender.hasPermission("bookrules.status")) {
                message = message + "§cCommands:\n";
                if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) {
                    message = message + "§c/bookrules reload §7- reload the config\n";
                }
                if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.status")) {
                    message = message + "§c/bookrules status [player] §7- check the status of a sender\n\n";
                }
                if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) {
                    message = message + "§c/bookrules acceptrules [player] §7- accept the rules for a sender\n";
                }
                if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                    message = message + "§c/bookrules declinerules [player] §7- decline the rules for a sender\n\n";
                }
            }
            message = message + "§7Links:\n" +
                    "§cDiscord:§7 https://discord.gg/jTkfTDGr5c\n" +
                    "§cDownload:§7 http://bit.ly/bookrules";
            sender.sendMessage(message);
        }else if(args.length == 1) {
            if((sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) && args[0].equals("reload")) {
                data.instantiateFile();
                data.reloadPlayerData();
                sender.sendMessage("§7[§cBookRules§7] §cBookRules §7successfully §creloaded!");
            }else if((sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) && args[0].equals("acceptrules")) {
                if(!(sender instanceof Player)) {
                    sender.sendMessage(data.prefix + "§4Only players can use this command!");
                    return true;
                }
                Player player = (Player) sender;
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
            }else if((sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) && args[0].equals("declinerules")) {
                if(!(sender instanceof Player)) {
                    sender.sendMessage(data.prefix + "§4Only players can use this command!");
                    return true;
                }
                Player player = (Player) sender;
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
            }else {
                message = "";
                if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload") || sender.hasPermission("bookrules.declinerules")) {
                    message = message + "§7[§cBookRules§7] §cCommands:\n";
                    if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) {
                        message = message + "§c/bookrules reload §7- reload the config\n";
                    }
                    if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.status")) {
                        message = message + "§c/bookrules status [player] §7- check the status of a sender\n\n";
                    }
                    if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) {
                        message = message + "§c/bookrules acceptrules [player] §7- accept the rules for a sender\n";
                    }
                    if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                        message = message + "§c/bookrules declinerules [player] §7- decline the rules for a sender\n";
                    }
                }else message = data.prefix + data.noPermission;

                sender.sendMessage(message);
            }
        }else if(args.length == 2) {
            Player target;
            OfflinePlayer offlinePlayer;
            if((sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) && args[0].equals("declinerules")) {
                offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if(data.players.contains(offlinePlayer.getUniqueId().toString())) {
                    data.players.remove(offlinePlayer.getUniqueId().toString());
                    sender.sendMessage("§7[§cBookRules§7] Successfully removed the §caccepted §7status from the player §c" + offlinePlayer.getName() + "§c.");
                    target = Bukkit.getPlayer(offlinePlayer.getUniqueId());
                    if(target != null) {
                        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            kickText = PlaceholderAPI.setPlaceholders(target, kickText);
                        }
                        sender.sendMessage("§7[§cBookRules§7] The player §c" + target.getName() + " §7is online, kicking.");
                        target.kickPlayer(kickText);
                    }
                }else {
                    sender.sendMessage("§7[§cBookRules§7] The player §c" + offlinePlayer.getName() + " §7didn't accept the rules!");
                }
            }else if((sender.hasPermission("bookrules.commands") || sender.hasPermission("boookrules.acceptrules")) && args[0].equals("acceptrules")) {
                offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if(data.players.contains(offlinePlayer.getUniqueId().toString())) {
                    sender.sendMessage("§7[§cBookRules§7] §7The player §c" + offlinePlayer.getName() + " §7already accepted the rules.");
                    BookRules.debug("Player " + offlinePlayer.getName() + " did accept the rules (is registered in players.txt), passing, no action taken.");
                }else {
                    BookRules.debug("Player " + offlinePlayer.getName() + " didn't accept the rules (isn't registered in players.txt).");
                    BookRules.debug("Registering player " + offlinePlayer.getName() + " in players.txt (player accepted the rules). (Player " + sender.getName() + " added accepted status)");
                    data.players.add(offlinePlayer.getUniqueId().toString());
                    sender.sendMessage("§7[§cBookRules§7] You sucessfully set the status from the player §c" + offlinePlayer.getName() + " §7to §caccepted.");
                    target = Bukkit.getPlayer(offlinePlayer.getUniqueId());
                    if(target != null) {
                        target.getOpenInventory().close();
                    }
                }
            }else if((sender.hasPermission("bookrules.commands") || sender.hasPermission("boookrules.status")) && args[0].equals("status")) {
                offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if(data.players.contains(offlinePlayer.getUniqueId().toString())) {
                    sender.sendMessage("§7[§cBookRules§7] §7The player §c" + offlinePlayer.getName() + " §7accepted the rules.");
                    BookRules.debug("Player " + offlinePlayer.getName() + " did accept the rules (is registered in players.txt).");
                }else {
                    sender.sendMessage("§7[§cBookRules§7] §7The player §c" + offlinePlayer.getName() + " §7didn't accept the rules or declined them.");
                    BookRules.debug("Player " + offlinePlayer.getName() + " didn't accept the rules (isn't registered in players.txt).");
                }
            }else {
                message = "";
                if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload") || sender.hasPermission("bookrules.acceptrules") || sender.hasPermission("bookrules.declinerules")) {
                    message = message + "§7[§cBookRules§7] §cCommands:\n";
                    if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) {
                        message = message + "§c/bookrules reload §7- reload the config\n";
                    }
                    if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.status")) {
                        message = message + "§c/bookrules status [player] §7- check the status of a player\n";
                    }
                    if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) {
                        message = message + "§c/bookrules acceptrules [player] §7- accept the rules for a player\n";
                    }
                    if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                        message = message + "§c/bookrules declinerules [player] §7- decline the rules for a player\n";
                    }
                }else message = data.prefix + data.noPermission;
                sender.sendMessage(message);
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> tabComplete = new ArrayList<>();
        if(args.length == 1) {
            if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) {
                tabComplete.add("reload");
            }
            if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) {
                tabComplete.add("acceptrules");
            }
            if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                tabComplete.add("declinerules");
            }
            if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.status")) {
                tabComplete.add("status");
            }
        }else if(args.length == 2 && args[0].equals("declinerules") || args[0].equals("acceptrules") || args[0].equals("status")) {
            if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules") || sender.hasPermission("bookrules.declinerules") || sender.hasPermission("bookrules.status")) {
                for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    tabComplete.add(offlinePlayer.getName());
                }
            }
        }
        return tabComplete;
    }
}
