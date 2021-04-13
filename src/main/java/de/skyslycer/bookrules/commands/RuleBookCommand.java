package de.skyslycer.bookrules.commands;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class RuleBookCommand implements CommandExecutor {
    Data data = BookRules.data;
    BookOpener bookOpener = new BookOpener();
    ItemStack book;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<String> bookContent = data.bookContent;
        String acceptText = data.acceptText;
        ItemStack book;

        if(!(sender instanceof Player)) {
            sender.sendMessage(data.prefix + "§4Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        if(data.usePermissions) {
            if(!player.hasPermission("bookrules.rules")) {
                player.sendMessage(data.prefix + data.noPermission);
                BookRules.debug("Player " + player.getName() + " doesn't have permission (bookrules.rules), passing, no action taken.");
                return true;
            }
        }

        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            acceptText = PlaceholderAPI.setPlaceholders(player, data.acceptText);
            for(int i = 0; i < bookContent.size(); i++) {
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

        for(int i = 0; i < bookContent.size(); i++) {
            bookMeta.addPage(bookContent.get(i));
        }

        BookRules.debug("Opening book to the player " + player.getName() + ".");

        bookMeta.spigot().addPage(baseComponents);
        bookMeta.setTitle("BookRules");
        bookMeta.setAuthor("Server");
        book.setItemMeta(bookMeta);
        bookOpener.open(player, book);
        return false;
    }
}
