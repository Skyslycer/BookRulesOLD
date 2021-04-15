package de.skyslycer.bookrules.events;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.util.BookOpener;
import de.skyslycer.bookrules.util.Data;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class MoveEvent implements Listener {
    Data data = BookRules.data;
    BookOpener bookOpener = new BookOpener();
    ItemStack book;

    @EventHandler
    public void OnMove(PlayerMoveEvent event) {
        if(!data.players.contains(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            ArrayList<String> bookContent = data.bookContent;
            Player player = event.getPlayer();
            String acceptText = data.acceptText;
            book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMeta = (BookMeta) book.getItemMeta();

            if(data.usePermissions) {
                if(!player.hasPermission("bookrules.onclose")) {
                    return;
                }
            }

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                acceptText = PlaceholderAPI.setPlaceholders(event.getPlayer(), data.acceptText);
                for (int i = 0; i < bookContent.size(); i++) {
                    String tempText = PlaceholderAPI.setPlaceholders(player, bookContent.get(i));
                    bookContent.set(i, tempText);
                }
            }

            List<Template> templates = List.of(
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
            bookOpener.open(player, book);
        }
    }
}
