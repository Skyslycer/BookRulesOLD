package de.skyslycer.bookrules.commands;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.api.RulesAPI;
import de.skyslycer.bookrules.core.MessageManager;
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
    RulesAPI rulesAPI = new RulesAPI();
    String kickText;
    MessageManager messageManager;
    BookRules bookRules;

    public void injectData(MessageManager messageManager, BookRules bookRules) {
        this.messageManager = messageManager;
        this.bookRules = bookRules;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            //sending permission-based instructions to the player
            String message = "§7[§cBookRules§7] §cBookRules §7by §cSkyslycer\n";
            if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload") || sender.hasPermission("bookrules.acceptrules") || sender.hasPermission("bookrules.declinerules") || sender.hasPermission("bookrules.status")) {
                message = message + "§cCommands:\n";
                if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) {
                    message = message + "§c/bookrules reload §7- reload the config\n";
                }
                if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.status")) {
                    message = message + "§c/bookrules status [player] §7- check the status of a player\n";
                }
                if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) {
                    message = message + "§c/bookrules acceptrules [player] §7- accept the rules for a player\n";
                }
                if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                    message = message + "§c/bookrules declinerules [player] §7- decline the rules for a player\n\n";
                }
            }

            message = message + "§7Links:\n" +
                    "§7Discord:§c " + BookRules.DISCORD_URL + "\n" +
                    "§7Download:§c " + BookRules.DOWNLOAD;

            sender.sendMessage(message);
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reload" -> {
                    if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) {
                        //sender has permission to reload the plugin via the /bookrules command
                        if (bookRules.instantiateConfig()) {
                            rulesAPI.reloadPlayerData();
                            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_RELOAD_SUCCESSFUL, sender);
                        } else messageManager.sendMessage(MessageManager.MessageType.MESSAGE_RELOAD_FAILED, sender);
                    } else
                        messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, sender);
                }
                case "acceptrules" -> {
                    if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) {
                        //player has permission to accept rules via /bookrules command, DOES NOT specify a player
                        if (!(sender instanceof Player)) {
                            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PLAYER, sender);
                            return true;
                        }

                        Player player = (Player) sender;

                        rulesAPI.playerHasAcceptedRules(player.getUniqueId().toString()).thenAccept((hasAccepted) -> {
                            if (hasAccepted) {
                                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_ALREADY_ACCEPTED, player);
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTED, player.getName());
                            } else {
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINED, player.getName());
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTING, player.getName());
                                rulesAPI.acceptRules(player.getUniqueId().toString());
                                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_ACCEPT_RULES, player);
                                player.getOpenInventory().close();
                            }
                        });
                    } else
                        messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, sender);
                }
                case "declinerules" -> {
                    if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                        //player has permission to decline rules via the /bookrules command, DOES NOT specify a player
                        if (!(sender instanceof Player)) {
                            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PLAYER, sender);
                            return true;
                        }

                        Player player = (Player) sender;

                        this.kickText = messageManager.kickText;
                        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            kickText = PlaceholderAPI.setPlaceholders(player, kickText);
                        }

                        rulesAPI.playerHasAcceptedRules(player.getUniqueId().toString()).thenAccept((hasAccepted) -> {
                            if (hasAccepted) {
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTED, player.getName());
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINING, player.getName());
                                rulesAPI.declineRules(player.getUniqueId().toString());
                            } else {
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINED, player.getName());
                            }
                        });

                        messageManager.sendDebug(MessageManager.DebugType.DEBUG_KICK, player.getName());
                        player.kickPlayer(kickText);
                    } else
                        messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, sender);
                }
                case "status" -> {
                    if (sender.hasPermission("bookrules.commands") || sender.hasPermission("boookrules.status")) {
                        //sender has permission to see the status of a player
                        if (!(sender instanceof Player)) {
                            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PLAYER, sender);
                            return true;
                        }

                        Player player = (Player) sender;

                        rulesAPI.playerHasAcceptedRules(player.getUniqueId().toString()).thenAccept((hasAccepted) -> {
                            if (hasAccepted) {
                                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_STATUS_ACCEPTED, player);
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTED, player.getName());
                            } else {
                                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_STATUS_DECLINED, player);
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINED, player.getName());
                            }
                        });
                    } else
                        messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, sender);
                }
                default -> {
                    String message = "";
                    if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload") || sender.hasPermission("bookrules.declinerules")) {
                        message = message + "§7[§cBookRules§7] §cCommands:\n";
                        if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) {
                            message = message + "§c/bookrules reload §7- reload the config\n";
                        }

                        if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.status")) {
                            message = message + "§c/bookrules status [player] §7- check the status of a player\n";
                        }

                        if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) {
                            message = message + "§c/bookrules acceptrules [player] §7- accept the rules for a player\n";
                        }

                        if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                            message = message + "§c/bookrules declinerules [player] §7- decline the rules for a player\n";
                        }
                    } else message = messageManager.prefix + messageManager.noPermission;
                    messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_NO_PREFIX, message, sender);
                }
            }
        } else if (args.length == 2) {
            OfflinePlayer offlinePlayer;

            switch (args[0].toLowerCase()) {
                case "declinerules" -> {
                    if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                        //sender has permission to decline the rules via the /bookrules command, DOES specify a player
                        offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

                        Player target = Bukkit.getPlayer(offlinePlayer.getUniqueId());

                        rulesAPI.playerHasAcceptedRules(offlinePlayer.getUniqueId().toString()).thenAccept((hasAccepted) -> {
                            if (hasAccepted) {
                                rulesAPI.declineRules(offlinePlayer.getUniqueId().toString());
                                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_REMOVE_ACCEPTED_STATUS, offlinePlayer.getName(), sender);
                                if (target != null) {
                                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                                        kickText = PlaceholderAPI.setPlaceholders(target, kickText);
                                    }
                                    messageManager.sendMessage(MessageManager.MessageType.MESSAGE_REMOVE_ACCEPTED_KICK, target.getName(), sender);
                                    target.kickPlayer(kickText);
                                }
                            } else {
                                sender.sendMessage("§7[§cBookRules§7] The player §c" + offlinePlayer.getName() + " §7didn't accept the rules!");
                            }
                        });
                    } else
                        messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, sender);
                }
                case "acceptrules" -> {
                    if (sender.hasPermission("bookrules.commands") || sender.hasPermission("boookrules.acceptrules")) {
                        //sender has permission to accept the rules via the /bookrules command, DOES specify a player
                        offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

                        Player target = Bukkit.getPlayer(offlinePlayer.getUniqueId());

                        rulesAPI.playerHasAcceptedRules(offlinePlayer.getUniqueId().toString()).thenAccept((hasAccepted) -> {
                            if (hasAccepted) {
                                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_PLAYER_ALREADY_ACCEPTED, offlinePlayer.getName(), sender);
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTED, offlinePlayer.getName());
                            } else {
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINED, offlinePlayer.getName());
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTING, offlinePlayer.getName());
                                rulesAPI.acceptRules(offlinePlayer.getUniqueId().toString());
                                sender.sendMessage("§7[§cBookRules§7] You sucessfully set the status from the player §c" + offlinePlayer.getName() + " §7to §caccepted.");

                                if (target != null) {
                                    target.getOpenInventory().close();
                                }
                            }
                        });
                    } else
                        messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, sender);
                }
                case "status" -> {
                    if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.status")) {
                        //sender has permission to see the status of a player
                        offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

                        rulesAPI.playerHasAcceptedRules(offlinePlayer.getUniqueId().toString()).thenAccept((hasAccepted) -> {
                            if (hasAccepted) {
                                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_PLAYER_STATUS_ACCEPTED, offlinePlayer.getName(), sender);
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTED, offlinePlayer.getName());
                            } else {
                                messageManager.sendMessage(MessageManager.MessageType.MESSAGE_PLAYER_STATUS_DECLINED, offlinePlayer.getName(), sender);
                                messageManager.sendDebug(MessageManager.DebugType.DEBUG_DECLINED, offlinePlayer.getName());
                            }
                        });
                    } else
                        messageManager.sendMessage(MessageManager.MessageType.MESSAGE_NO_PERMISSION, sender);
                }
                default -> {
                    String message = "";

                    if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload") || sender.hasPermission("bookrules.acceptrules") || sender.hasPermission("bookrules.declinerules")) {
                        message = message + "§7[§cBookRules§7] §cCommands:\n";
                        if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) {
                            message = message + "§c/bookrules reload §7- reload the config\n";
                        }
                        if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.status")) {
                            message = message + "§c/bookrules status [player] §7- check the status of a player\n";
                        }
                        if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) {
                            message = message + "§c/bookrules acceptrules [player] §7- accept the rules for a player\n";
                        }
                        if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                            message = message + "§c/bookrules declinerules [player] §7- decline the rules for a player\n";
                        }
                    } else message = messageManager.prefix + messageManager.noPermission;
                    messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_NO_PREFIX, message, sender);
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> tabComplete = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.reload")) {
                tabComplete.add("reload");
            }
            if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules")) {
                tabComplete.add("acceptrules");
            }
            if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.declinerules")) {
                tabComplete.add("declinerules");
            }
            if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.status")) {
                tabComplete.add("status");
            }
        } else if (args.length == 2 && args[0].equals("declinerules") || args[0].equals("acceptrules") || args[0].equals("status")) {
            if (sender.hasPermission("bookrules.commands") || sender.hasPermission("bookrules.acceptrules") || sender.hasPermission("bookrules.declinerules") || sender.hasPermission("bookrules.status")) {
                for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    tabComplete.add(offlinePlayer.getName());
                }
            }
        }
        return tabComplete;
    }

}
