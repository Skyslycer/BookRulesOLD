package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.util.Data;
import de.skyslycer.bookrules.util.PlayerSaver;
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
    PlayerSaver playerSaver = new PlayerSaver();

    String kickText = data.kickText;
    String message;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(data.prefix + "§4Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        if(args.length == 0) {
            message = "§7[§cBookRules§7] §cBookRules §7by §cSkyslycer\n";
            if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.reload") || player.hasPermission("bookrules.acceptrules") || player.hasPermission("bookrules.declinerules")) {
                message = message + "§cCommands:\n";
                if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.reload")) {
                    message = message + "§c/bookrules reload §7- reload the config\n";
                }
                if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.acceptrules")) {
                    message = message + "§c/bookrules acceptrules [player] §7- accept the rules for a player\n";
                }
                if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.declinerules")) {
                    message = message + "§c/bookrules declinerules [player] §7- decline the rules for a player\n\n";
                }
            }
            message = message + "§7Links:\n" +
                    "§cDiscord:§7 https://discord.gg/MJkVb2NMPS\n" +
                    "§cDownload:§7 http://bit.ly/bookrules";
            player.sendMessage(message);
        }else if(args.length == 1) {
            if((player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.reload")) && args[0].equals("reload")) {
                data.instantiateFile();
                player.sendMessage("§7[§cBookRules§7] §cBookRules §7successfully §creloaded!");
            }else if((player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.acceptrules")) && args[0].equals("acceptrules")) {
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
            }else if((player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.declinerules")) && args[0].equals("declinerules")) {
                if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    kickText = PlaceholderAPI.setPlaceholders(player, kickText);
                }

                if(playerSaver.containsInFile(player.getUniqueId().toString(), "plugins//BookRules//players.txt")) {
                    BookRules.debug("Player " + player.getName() + " did accept the rules (is registered in players.txt), removing entry, caused by player.");
                    playerSaver.replaceInFile("plugins//BookRules//players.txt", player.getUniqueId().toString(), "");
                }else {
                    BookRules.debug("Player " + player.getName() + " didn't accept the rules (isn't registered in players.txt).");
                }
                BookRules.debug("Kicking player " + player.getName() + " for declining the rules.");
                player.kickPlayer(kickText);
            }else {
                message = "";
                if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.reload") || player.hasPermission("bookrules.declinerules")) {
                    message = message + "§7[§cBookRules§7] §cCommands:\n";
                    if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.reload")) {
                        message = message + "§c/bookrules reload §7- reload the config\n";
                    }
                    if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.acceptrules")) {
                        message = message + "§c/bookrules acceptrules [player] §7- accept the rules for a player\n";
                    }
                    if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.declinerules")) {
                        message = message + "§c/bookrules declinerules [player] §7- decline the rules for a player\n";
                    }
                }else message = data.prefix + data.noPermission;

                player.sendMessage(message);
            }
        }else if(args.length == 2) {
            Player target;
            OfflinePlayer offlinePlayer;
            if((player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.declinerules")) && args[0].equals("declinerules")) {
                offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if(playerSaver.containsInFile(offlinePlayer.getUniqueId().toString(), "plugins//BookRules//players.txt")) {
                    playerSaver.replaceInFile("plugins//BookRules//players.txt", offlinePlayer.getUniqueId().toString(), "");
                    player.sendMessage("§7[§cBookRules§7] Successfully removed the §caccepted §7status from the player §c" + offlinePlayer.getName() + "§c.");
                    target = Bukkit.getPlayer(offlinePlayer.getUniqueId());
                    if(target != null) {
                        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            kickText = PlaceholderAPI.setPlaceholders(target, kickText);
                        }
                        player.sendMessage("§7[§cBookRules§7] The player §c" + target.getName() + " §7is online, kicking.");
                        target.kickPlayer(kickText);
                    }
                }else {
                    player.sendMessage("§7[§cBookRules§7] The player §c" + offlinePlayer.getName() + " §7didn't accept the rules!");
                }
            }else if((player.hasPermission("bookrules.commands") || player.hasPermission("boookrules.acceptrules")) && args[0].equals("acceptrules")) {
                offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if(playerSaver.containsInFile(offlinePlayer.getUniqueId().toString(), "plugins//BookRules//players.txt")) {
                    player.sendMessage("§7[§cBookRules§7] §7The player §c" + offlinePlayer.getName() + " §7already accepted the rules.");
                    BookRules.debug("Player " + offlinePlayer.getName() + " did accept the rules (is registered in players.txt), passing, no action taken.");
                }else {
                    BookRules.debug("Player " + offlinePlayer.getName() + " didn't accept the rules (isn't registered in players.txt).");
                    BookRules.debug("Registering player " + offlinePlayer.getName() + " in players.txt (player accepted the rules). (Player " + player.getName() + " added accepted status)");
                    playerSaver.writeToFile(offlinePlayer.getUniqueId().toString(), "plugins//BookRules//players.txt");
                    player.sendMessage("§7[§cBookRules§7] You sucessfully set the status from the player §c" + offlinePlayer.getName() + " §7to §caccepted.");
                }
            }else {
                message = "";
                if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.reload") || player.hasPermission("bookrules.acceptrules") || player.hasPermission("bookrules.declinerules")) {
                    message = message + "§7[§cBookRules§7] §cCommands:\n";
                    if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.reload")) {
                        message = message + "§c/bookrules reload §7- reload the config\n";
                    }
                    if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.acceptrules")) {
                        message = message + "§c/bookrules acceptrules [player] §7- accept the rules for a player\n";
                    }
                    if(player.hasPermission("bookrules.commands") || player.hasPermission("bookrules.declinerules")) {
                        message = message + "§c/bookrules declinerules [player] §7- decline the rules for a player\n";
                    }
                }else message = data.prefix + data.noPermission;

                player.sendMessage(message);
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
        }else if(args.length == 2 && args[0].equals("declinerules") || args[0].equals("acceptrules")) {
            if(sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules") || sender.hasPermission("bookrules.declinerules")) {
                for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    tabComplete.add(offlinePlayer.getName());
                }
            }
        }
        return tabComplete;
    }
}
