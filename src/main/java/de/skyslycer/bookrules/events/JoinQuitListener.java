package de.skyslycer.bookrules.events;

import de.skyslycer.bookrules.BookRules;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        final BookRules bookRules = BookRules.getInstance();

        if(!bookRules.getData().players.contains(event.getPlayer().getUniqueId().toString())) {
            bookRules.startThread();
            bookRules.openBook(event.getPlayer(), "bookrules.onjoin");
            bookRules.getPlayerCache().put(event.getPlayer(), event.getPlayer().getLocation());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        BookRules.getInstance().getPlayerCache().remove(event.getPlayer());
    }
}
