package de.skyslycer.bookrules.core;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Path;

public class MessageManager {
    public String standardPrefix = "§7[§cBookRules§7] ";
    public String prefix = "&7[&cBookRules&7] ";
    public String kickText = "&7In order to &aplay &7on the server, you need to &aagree &7to the rules!";
    public String noPermission = "&4You don't have permission to run this command!";
    public String acceptRules = "&7You successfully &aaccepted &7the &arules.";
    public String alreadyAccepted = "&7You &calready accepted &7the &crules!";
    public boolean debugMode = false;

    public void instantiateMessages(FileConfiguration configFile) {
        if (configFile.getString("prefix") == null) {
            configFile.set("prefix", prefix);
        }
        prefix = configFile.getString("prefix") + " ";
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        if (configFile.getString("kick-text") == null) {
            System.out.println(configFile.getString("kick-text"));
            configFile.set("kick-text", kickText);
        }
        kickText = configFile.getString("kick-text");
        kickText = ChatColor.translateAlternateColorCodes('&', kickText);

        if (configFile.getString("accept-message") == null) {
            configFile.set("accept-message", acceptRules);
        }
        acceptRules = configFile.getString("accept-message");
        acceptRules = ChatColor.translateAlternateColorCodes('&', acceptRules);

        if (configFile.getString("already-accepted-message") == null) {
            configFile.set("already-accepted-message", alreadyAccepted);
        }
        alreadyAccepted = configFile.getString("already-accepted-message");
        alreadyAccepted = ChatColor.translateAlternateColorCodes('&', alreadyAccepted);

        if (configFile.getString("no-permission") == null) {
            configFile.set("no-permission", noPermission);
        }
        noPermission = configFile.getString("no-permission");
        noPermission = ChatColor.translateAlternateColorCodes('&', noPermission);

        if (configFile.getString("debug-mode") == null) {
            configFile.set("debug-mode", false);
        }
        debugMode = configFile.getBoolean("debug-mode");

        sendDebug("Prefix: " + prefix);
        sendDebug("Kick text: " + kickText);
        sendDebug("Message sent on accept: " + acceptRules);
        sendDebug("Message sent if the rules are already accepted: " + alreadyAccepted);
        sendDebug("No permission message: " + noPermission);
    }

    public void sendMessage(MessageType messageType, String message, CommandSender sender) {
        if (messageType == MessageType.MESSAGE_CUSTOM) {
            sender.sendMessage(prefix + message);
        }
        if (messageType == MessageType.MESSAGE_CUSTOM_PREFIX) {
            sender.sendMessage(standardPrefix + message);
        }
        if (messageType == MessageType.MESSAGE_CUSTOM_NO_PREFIX) {
            sender.sendMessage(message);
        }
        if (messageType == MessageType.MESSAGE_REMOVE_ACCEPTED_STATUS) {
            sender.sendMessage(standardPrefix + "Successfully removed the §caccepted §7status from the player §c" + message + "§7.");
        }
        if (messageType == MessageType.MESSAGE_REMOVE_ACCEPTED_KICK) {
            sender.sendMessage(standardPrefix + "The player §c " + message + " §7is online, kicking.");
        }
        if (messageType == MessageType.MESSAGE_SET_ACCEPTED_STATUS) {
            sender.sendMessage(standardPrefix + "You successfully set the status from the player §c" + message + " §7to §caccepted.");
        }
        if (messageType == MessageType.MESSAGE_PLAYER_ALREADY_ACCEPTED) {
            sender.sendMessage(standardPrefix + "§7The player §c" + message + " §7already accepted the rules.");
        }
        if (messageType == MessageType.MESSAGE_PLAYER_STATUS_ACCEPTED) {
            sender.sendMessage(standardPrefix + "§7The player §c" + message + " §7accepted the rules.");
        }
        if (messageType == MessageType.MESSAGE_PLAYER_STATUS_DECLINED) {
            sender.sendMessage(standardPrefix + "§7The player §c" + message + " §7didn't accept the rules or declined them.");
        }
    }

    public void sendMessage(MessageType messageType, CommandSender sender) {
        if (messageType == MessageType.MESSAGE_NO_PERMISSION) {
            sender.sendMessage(prefix + noPermission);
        }
        if (messageType == MessageType.MESSAGE_NO_PLAYER) {
            sender.sendMessage(prefix + "§4Only players can use this command!");
        }
        if (messageType == MessageType.MESSAGE_RELOAD_SUCCESSFUL) {
            sender.sendMessage(standardPrefix + "§cBookRules successfully §7reloaded!");
        }
        if (messageType == MessageType.MESSAGE_RELOAD_FAILED) {
            sender.sendMessage(standardPrefix + "§cBookRules failed §7to reload! (Check your console!)");
        }
        if (messageType == MessageType.MESSAGE_ALREADY_ACCEPTED) {
            sender.sendMessage(prefix + alreadyAccepted);
        }
        if (messageType == MessageType.MESSAGE_ACCEPT_RULES) {
            sender.sendMessage(prefix + acceptRules);
        }
        if (messageType == MessageType.MESSAGE_STATUS_ACCEPTED) {
            sender.sendMessage(prefix + "§7You §caccepted §7the rules.");
        }
        if (messageType == MessageType.MESSAGE_STATUS_DECLINED) {
            sender.sendMessage(prefix + "§7You didn't §caccept §7the rules or declined them.");
        }
    }

    public void sendMessage(MessageType messageType, String message, Player player) {
        if (messageType == MessageType.MESSAGE_CUSTOM) {
            player.sendMessage(prefix + message);
        }
        if (messageType == MessageType.MESSAGE_CUSTOM_PREFIX) {
            player.sendMessage(standardPrefix + message);
        }
        if (messageType == MessageType.MESSAGE_CUSTOM_NO_PREFIX) {
            player.sendMessage(message);
        }
        if (messageType == MessageType.MESSAGE_REMOVE_ACCEPTED_STATUS) {
            player.sendMessage(standardPrefix + "Successfully removed the §caccepted §7status from the player §c" + message + "§7.");
        }
        if (messageType == MessageType.MESSAGE_REMOVE_ACCEPTED_KICK) {
            player.sendMessage(standardPrefix + "The player §c " + message + " §7is online, kicking.");
        }
        if (messageType == MessageType.MESSAGE_SET_ACCEPTED_STATUS) {
            player.sendMessage(standardPrefix + "You successfully set the status from the player §c" + message + " §7to §caccepted.");
        }
        if (messageType == MessageType.MESSAGE_PLAYER_ALREADY_ACCEPTED) {
            player.sendMessage(standardPrefix + "§7The player §c" + message + " §7already accepted the rules.");
        }
        if (messageType == MessageType.MESSAGE_PLAYER_STATUS_ACCEPTED) {
            player.sendMessage(standardPrefix + "§7The player §c" + message + " §7accepted the rules.");
        }
        if (messageType == MessageType.MESSAGE_PLAYER_STATUS_DECLINED) {
            player.sendMessage(standardPrefix + "§7The player §c" + message + " §7didn't accept the rules or declined them.");
        }
    }

    public void sendMessage(MessageType messageType, Player player) {
        if (messageType == MessageType.MESSAGE_NO_PERMISSION) {
            player.sendMessage(prefix + noPermission);
        }
        if (messageType == MessageType.MESSAGE_NO_PLAYER) {
            player.sendMessage(prefix + "§4Only players can use this command!");
        }
        if (messageType == MessageType.MESSAGE_RELOAD_SUCCESSFUL) {
            player.sendMessage(standardPrefix + "BookRules §csuccessfully §7reloaded!");
        }
        if (messageType == MessageType.MESSAGE_RELOAD_FAILED) {
            player.sendMessage(standardPrefix + "§cBookRules failed §7to reload! (Check your console!)");
        }
        if (messageType == MessageType.MESSAGE_ALREADY_ACCEPTED) {
            player.sendMessage(prefix + alreadyAccepted);
        }
        if (messageType == MessageType.MESSAGE_ACCEPT_RULES) {
            player.sendMessage(prefix + acceptRules);
        }
        if (messageType == MessageType.MESSAGE_STATUS_ACCEPTED) {
            player.sendMessage(standardPrefix + "§7You §caccepted §7the rules.");
        }
        if (messageType == MessageType.MESSAGE_STATUS_DECLINED) {
            player.sendMessage(standardPrefix + "§7You didn't §caccept §7the rules or declined them.");
        }
    }

    public void sendDebug(DebugType debugType, String message) {
        if (!debugMode) {
            return;
        }
        if (debugType == DebugType.DEBUG_DECLINING) {
            Bukkit.getLogger().info(standardPrefix + "Debug:\n §7" + "Player " + message + " clicked the decline button or ran the command '/declinerules'\nRemoving the player from the accepted list (if possible).");
        }
        if (debugType == DebugType.DEBUG_ACCEPTING) {
            Bukkit.getLogger().info(standardPrefix + "Debug:\n §7" + "Player " + message + " clicked the decline button or ran the command '/acceptrules'\nAdding the player to the accepted list (if doesn't exist).");
        }
        if (debugType == DebugType.DEBUG_DECLINED) {
            Bukkit.getLogger().info(standardPrefix + "Debug:\n §7" + "Player " + message + " didn't accept the rules (isn't registered in the list).");
        }
        if (debugType == DebugType.DEBUG_ACCEPTED) {
            Bukkit.getLogger().info(standardPrefix + "Debug:\n §7" + "Player " + message + " did accept the rules (is registered in the list).");
        }
        if (debugType == DebugType.DEBUG_KICK) {
            Bukkit.getLogger().info(standardPrefix + "Debug:\n §7" + message);
        }
        if (debugType == DebugType.DEBUG_WARN) {
            Bukkit.getLogger().warning(standardPrefix + "§4WARN:\n §7" + message);
        }
    }

    public void sendDebug(DebugType debugType, String message, String additional) {
        if (!debugMode) {
            return;
        }
        if (debugType == DebugType.DEBUG_NO_PERMISSION) {
            Bukkit.getLogger().info(standardPrefix + "Debug:\n §7" + "Player " + message + " doesn't have permission (" + additional + "), passing, no action taken.");
        }
    }

    public void sendDebug(String message) {
        if (!debugMode) {
            return;
        }
        Bukkit.getLogger().info(standardPrefix + "Debug:\n §7" + message);
    }

    public void sendDebug(DebugType debugType) {
        if (debugType == DebugType.DEBUG_UNSUPPORTED_VERSION) {
            Bukkit.getLogger().warning(standardPrefix + "§4WARN:\n §7You are running an §4OUTDATED §7version of this plugin! Please update to the latest version at:§4 https://bit.ly/bookrules");
        }
    }

    public enum MessageType {
        MESSAGE_CUSTOM,
        MESSAGE_CUSTOM_PREFIX,
        MESSAGE_CUSTOM_NO_PREFIX,
        MESSAGE_NO_PERMISSION,
        MESSAGE_NO_PLAYER,
        MESSAGE_RELOAD_SUCCESSFUL,
        MESSAGE_RELOAD_FAILED,
        MESSAGE_ALREADY_ACCEPTED,
        MESSAGE_STATUS_ACCEPTED,
        MESSAGE_STATUS_DECLINED,
        MESSAGE_ACCEPT_RULES,
        MESSAGE_REMOVE_ACCEPTED_STATUS,
        MESSAGE_REMOVE_ACCEPTED_KICK,
        MESSAGE_SET_ACCEPTED_STATUS,
        MESSAGE_PLAYER_ALREADY_ACCEPTED,
        MESSAGE_PLAYER_STATUS_ACCEPTED,
        MESSAGE_PLAYER_STATUS_DECLINED
    }

    public enum DebugType {
        DEBUG_DECLINED,
        DEBUG_ACCEPTED,
        DEBUG_NO_PERMISSION,
        DEBUG_DECLINING,
        DEBUG_ACCEPTING,
        DEBUG_KICK,
        DEBUG_WARN,
        DEBUG_UNSUPPORTED_VERSION
    }
}
