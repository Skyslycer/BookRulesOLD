package de.skyslycer.bookrules.listener;

import de.skyslycer.bookrules.core.BookManager;
import de.skyslycer.bookrules.api.RulesAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    RulesAPI rulesAPI = new RulesAPI();
    BookManager bookManager;

    public void injectData(BookManager bookManager) {
        this.bookManager = bookManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if(!rulesAPI.playerHasAcceptedRules(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            bookManager.openBook(event.getPlayer(), "bookrules.onclose", false);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if(!rulesAPI.playerHasAcceptedRules(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            bookManager.openBook(event.getPlayer(), "bookrules.onclose", false);
        }
    }
}
