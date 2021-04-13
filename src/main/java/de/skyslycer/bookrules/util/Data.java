package de.skyslycer.bookrules.util;

import com.google.gson.internal.bind.util.ISO8601Utils;
import de.skyslycer.bookrules.BookRules;
import org.bukkit.ChatColor;

import java.util.ArrayList;

public class Data {
    public String prefix;
    public String acceptText;
    public String acceptButton;
    public String declineButton;
    public String kickText;
    public String page = "";
    public String noPermission;
    public String acceptRules;
    public String alreadyAccepted;
    public boolean debugMode;
    public boolean usePermissions;
    public ArrayList<String> bookContent = new ArrayList<>();
    public ArrayList<String> rawBookContent;

    public void instantiateFile() {
        BookRules.debug("Starting config.yml reading");

        YamlFileWriter configFile = new YamlFileWriter("plugins//BookRules", "config.yml");
        if(configFile.getString("prefix") == null) {
            configFile.setValue("prefix", "&7[&cBookRules&7]");
        }
        prefix = configFile.getString("prefix") + " ";
        BookRules.debug("Prefix: " + prefix);

        if(configFile.getString("accept-text") == null) {
            configFile.setValue("accept-text", "&7If you accept the &7rules, you agree to &7the rules and &7punishments for &7breaking them. If you &7decline, you will be &7kicked. \n\n<acceptbutton> <declinebutton>");
        }
        acceptText = configFile.getString("accept-text");
        BookRules.debug("Text on the last page: " + acceptText);

        if(configFile.getString("accept-button") == null) {
            configFile.setValue("accept-button", "&a[ACCEPT]");
        }
        acceptButton = configFile.getString("accept-button");
        BookRules.debug("Accept button: " + acceptButton);

        if(configFile.getString("decline-button") == null) {
            configFile.setValue("decline-button", "&4[DECLINE]");
        }
        declineButton = configFile.getString("decline-button");
        BookRules.debug("Decline button: " + declineButton);

        if(configFile.getString("kick-text") == null) {
            configFile.setValue("kick-text", "&7In order to &aplay &7on the server, you need to &aagree &7to the rules!");
        }
        kickText = configFile.getString("kick-text");
        BookRules.debug("Kick text: " + kickText);

        if(configFile.getString("no-permission") == null) {
            configFile.setValue("no-permission", "&4You don't have permission to run this command!");
        }
        noPermission = configFile.getString("no-permission");
        BookRules.debug("No permission message: " + noPermission);

        if(configFile.getString("accept-message") == null) {
            configFile.setValue("accept-message", "&7You successfully &aaccepted &7the &arules.");
        }
        acceptRules = configFile.getString("accept-message");
        BookRules.debug("Message sent on accept: " + acceptRules);

        if(configFile.getString("already-accepted-message") == null) {
            configFile.setValue("already-accepted-message", "&7You &calready accepted &7the &crules!");
        }
        alreadyAccepted = configFile.getString("already-accepted-message");
        BookRules.debug("Message sent if the rules are already accepted: " + alreadyAccepted);

        if(configFile.getString("use-permissions") == null) {
            configFile.setValue("use-permissions", false);
        }
        usePermissions = configFile.getBoolean("use-permissions");
        BookRules.debug("Enable permissions: " + usePermissions);

        if(configFile.getString("debug-mode") == null) {
            configFile.setValue("debug-mode", false);
        }
        debugMode = configFile.getBoolean("debug-mode");

        if(configFile.contains("content")) {
            bookContent.clear();
            for(String key : configFile.getKeys("content")) {
                rawBookContent = configFile.getArrayList("content." + key);
                for (String s : rawBookContent) {
                    page = page + s + "\n";
                }
                page = ChatColor.translateAlternateColorCodes('&', page);
                bookContent.add(page);
                page = "";
            }
        }else {
            ArrayList<String> default1 = new ArrayList<>();
            default1.add("&cIf you see this, please contact a server administrator or owner to configure this plugin properly.");
            default1.add("&0If you want to know how configure this plugin, please visit the plugin page. The wiki is well documented and easy to understand.");
            default1.add("Plugin page: http://bit.ly/bookrules");
            ArrayList<String> default2 = new ArrayList<>();
            default2.add("&7[&61&7] &6Don't grief!");
            default2.add("&7[&62&7] &6Don't hack!");
            configFile.setValue("content.1", default1);
            configFile.setValue("content.2", default2);
        }
        configFile.save();
        int i = 1;
        String contentDebug = "Book content (except the last page):";
        for (String page : bookContent) {
            contentDebug = contentDebug + "\n\n" + "Page " + i + ":\n" + page;
            i++;
        }
        BookRules.debug(contentDebug);

        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        acceptText = ChatColor.translateAlternateColorCodes('&', acceptText);
        acceptButton = ChatColor.translateAlternateColorCodes('&', acceptButton);
        declineButton = ChatColor.translateAlternateColorCodes('&', declineButton);
        kickText = ChatColor.translateAlternateColorCodes('&', kickText);
        noPermission = ChatColor.translateAlternateColorCodes('&', noPermission);
        alreadyAccepted = ChatColor.translateAlternateColorCodes('&', alreadyAccepted);
        acceptRules = ChatColor.translateAlternateColorCodes('&', acceptRules);
    }
}
