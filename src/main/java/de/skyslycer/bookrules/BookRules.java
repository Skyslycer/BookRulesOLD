package de.skyslycer.bookrules;

import de.skyslycer.bookrules.commands.AcceptRulesCommand;
import de.skyslycer.bookrules.commands.BookRulesCommand;
import de.skyslycer.bookrules.commands.DeclineRulesCommand;
import de.skyslycer.bookrules.commands.RuleBookCommand;
import de.skyslycer.bookrules.events.JoinQuitListener;
import de.skyslycer.bookrules.util.BookOpener;
import de.skyslycer.bookrules.util.Data;
import de.skyslycer.bookrules.util.MCVersion;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class BookRules extends JavaPlugin {

    private static BookRules instance;

    private final Data data = new Data();
    private final Map<Player, Location> playerCache = new ConcurrentHashMap<>();

    private Timer timer;

    @Override
    public void onEnable() {
        instance = this;
        data.instantiateFile();
        data.getPlayerData();

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
        pluginManager.registerEvents(new JoinQuitListener(), this);
    }

    public void onDisable() {
        data.setPlayerData();
    }

    public void openBook(Player player) {
        ArrayList<String> bookContent = data.bookContent;
        String acceptText = data.acceptText;

        BookRules.debug("Player " + player.getName() + " joined the server.");

        if(!data.players.contains(player.getUniqueId().toString())) {
            BookRules.debug("Player " + player.getName() + " didn't accept the rules (isn't registered in players.txt).");

            if (data.usePermissions) {
                if (!player.hasPermission("bookrules.onjoin")) {
                    BookRules.debug("Player " + player.getName() + " doesn't have permission (bookrules.rules), passing, no action taken.");
                    return;
                }
            }

            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMeta = (BookMeta) book.getItemMeta();

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                acceptText = PlaceholderAPI.setPlaceholders(player, data.acceptText);
                for (int i = 0; i < bookContent.size(); i++) {
                    String tempText = PlaceholderAPI.setPlaceholders(player, bookContent.get(i));
                    bookContent.set(i, tempText);
                }
            }

            List<Template> templates = Arrays.asList(
                    Template.of("acceptbutton", Component.text(data.acceptButton).color(NamedTextColor.GREEN).clickEvent(
                            ClickEvent.runCommand("/acceptrules"))),
                    Template.of("declinebutton", Component.text(data.declineButton).color(NamedTextColor.RED).clickEvent(
                            ClickEvent.runCommand("/declinerules")))
            );

            Component component = MiniMessage.get().parse(acceptText, templates);
            BaseComponent[] baseComponents = BungeeComponentSerializer.get().serialize(component);

            for (String s : bookContent) {
                bookMeta.addPage(s);
            }

            BookRules.debug("Opening book to the player " + player.getName() + ".");

            bookMeta.spigot().addPage(baseComponents);
            bookMeta.setTitle("BookRules");
            bookMeta.setAuthor("Server");
            book.setItemMeta(bookMeta);
            BookOpener.open(player, book);
        }
    }

    public void startThread() {
        if (timer == null) return;
        this.timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                playerCache.forEach((player, location) -> {
                    if(data.players.contains(player.getUniqueId().toString())) {
                        playerCache.remove(player);
                    } else {
                        if (player.getLocation().distance(location) >= 0.5D)
                            sync(() -> player.teleport(location));
                        sync(() -> openBook(player));
                    }
                });
                if (playerCache.isEmpty()) {
                    timer.cancel();
                    timer = null;
                }
            }
        }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1));
    }

    private void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(this, runnable);
    }

    public static void debug(String text) {
        if(getInstance().data.debugMode) {
            Bukkit.getLogger().info("§7[§cBookRules§7] " + text);
        }
    }

    public Data getData() {
        return data;
    }

    public Map<Player, Location> getPlayerCache() {
        return playerCache;
    }

    public static BookRules getInstance() {
        return instance;
    }
}
