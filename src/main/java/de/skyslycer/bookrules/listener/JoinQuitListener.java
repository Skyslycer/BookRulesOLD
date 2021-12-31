package de.skyslycer.bookrules.listener;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.api.RulesAPI;
import de.skyslycer.bookrules.core.BookManager;
import de.skyslycer.bookrules.core.MessageManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class JoinQuitListener implements Listener {
    RulesAPI rulesAPI = new RulesAPI();
    BookRules bookRules;
    BookManager bookManager;
    MessageManager messageManager;
    Map<Player, Location> playerCache;

    public void injectData(BookManager bookManager, BookRules bookRules, MessageManager messageManager, Map<Player, Location> playerCache) {
        this.bookManager = bookManager;
        this.bookRules = bookRules;
        this.messageManager = messageManager;
        this.playerCache = playerCache;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (event.getPlayer().isOp() && !bookRules.isLatestVersion) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§cYou are running an outdated version, please download a newer one at:§4 https://bit.ly/bookrules", event.getPlayer());
        }

        if (event.getPlayer().isOp() && !bookRules.isConfigSuccessful) {
            messageManager.sendMessage(MessageManager.MessageType.MESSAGE_CUSTOM_PREFIX, "§cYour config is not valid! Please check your console and solve the errors!", event.getPlayer());
        }

        rulesAPI.playerHasAcceptedRules(event.getPlayer().getUniqueId().toString()).thenAccept((hasAccepted) -> {
            if (!hasAccepted) {
                bookManager.openBook(event.getPlayer(), "bookrules.onjoin", false);
                playerCache.put(event.getPlayer(), event.getPlayer().getLocation());
            }
        });
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        playerCache.remove(event.getPlayer());
    }
}
