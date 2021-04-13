package de.skyslycer.bookrules;

import de.skyslycer.bookrules.commands.AcceptRulesCommand;
import de.skyslycer.bookrules.commands.BookRulesCommand;
import de.skyslycer.bookrules.commands.DeclineRulesCommand;
import de.skyslycer.bookrules.commands.RuleBookCommand;
import de.skyslycer.bookrules.events.JoinEvent;
import de.skyslycer.bookrules.events.MoveEvent;
import de.skyslycer.bookrules.util.Data;
import de.skyslycer.bookrules.util.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BookRules extends JavaPlugin {
    public static Data data = new Data();

    @Override
    public void onEnable() {
        data.instantiateFile();

        Bukkit.getConsoleSender().sendMessage(data.prefix + "§aBookRules §7successfully loaded!");
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Bukkit.getConsoleSender().sendMessage(data.prefix + "§aPlaceholderAPI §7successfully registered!");
        }else Bukkit.getConsoleSender().sendMessage(data.prefix + "§4PlaceholderAPI §7couldn't be found! Placeholders are not supported!");

        if(MCVersion.getVersion().isOlderThan(MCVersion.v1_12_R1)) {
            Bukkit.getLogger().warning("§4You are running an unsupported version! Don't expect support/working features!");
        }

        getCommand("declinerules").setExecutor(new DeclineRulesCommand());
        getCommand("acceptrules").setExecutor(new AcceptRulesCommand());
        getCommand("bookrules").setExecutor(new BookRulesCommand());
        getCommand("rulebook").setExecutor(new RuleBookCommand());

        getCommand("bookrules").setTabCompleter(new BookRulesCommand());

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new JoinEvent(), this);
        pluginManager.registerEvents(new MoveEvent(), this);
    }

    public static void debug(String text) {
        if(data.debugMode) {
            Bukkit.getLogger().info("§7[§cBookRules§7] " + text);
        }
    }
}
