package de.skyslycer.bookrules.core;

import de.skyslycer.bookrules.BookRules;
import de.skyslycer.bookrules.api.RulesAPI;
import de.skyslycer.bookrules.util.MCVersion;
import de.skyslycer.bookrules.util.NMSUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookManager {

    public ArrayList<String> bookContent = new ArrayList<>();
    public ArrayList<String> rawBookContent;
    public String acceptText;
    public String acceptButton;
    public String declineButton;

    MessageManager messageManager;
    PermissionManager permissionManager;

    RulesAPI rulesAPI = new RulesAPI();

    public void injectData(MessageManager messageManager, PermissionManager permissionManager) {
        this.messageManager = messageManager;
        this.permissionManager = permissionManager;
    }

    public void instantiateContent(FileConfiguration configFile) {
        if (configFile.getString("accept-button") == null) {
            configFile.set("accept-button", "&a[ACCEPT]");
        }
        acceptButton = configFile.getString("accept-button");
        messageManager.sendDebug("Accept button: " + acceptButton);
        acceptButton = BookRules.legacy(acceptButton);

        if (configFile.getString("decline-button") == null) {
            configFile.set("decline-button", "&4[DECLINE]");
        }
        declineButton = configFile.getString("decline-button");
        messageManager.sendDebug("Decline button: " + declineButton);
        declineButton = BookRules.legacy(declineButton);

        if (configFile.getString("accept-text") == null) {
            configFile.set("accept-text",
                    "&7If you accept the &7rules, you agree to &7the rules and &7punishments for &7breaking them. If you &7decline, you will be &7kicked. \n\n<acceptbutton> <declinebutton>");
        }
        acceptText = configFile.getString("accept-text");
        messageManager.sendDebug("Text on the last page: " + acceptText);
        acceptText = BookRules.legacy(acceptText);

        if (configFile.contains("content")) {
            String page;
            bookContent.clear();
            for (String key : configFile.getConfigurationSection("content").getKeys(false)) {
                rawBookContent = (ArrayList<String>) configFile.getList("content." + key);
                page = String.join("§r\n", rawBookContent);
                page = ChatColor.translateAlternateColorCodes('&', page);
                bookContent.add(page);
            }
        } else {
            //set content
            ArrayList<String> default1 = new ArrayList<>();
            ArrayList<String> default2 = new ArrayList<>();
            ArrayList<String> default3 = new ArrayList<>();
            //first page
            default1.add("&cIf you see this, please contact a server administrator or owner to configure this plugin properly.&r");
            default1.add(
                    "If you want to know how configure this plugin, please visit the plugin page. The wiki is well documented and easy to understand.");
            default1.add(" ");
            default1.add("Plugin page: http://bit.ly/bookrules");
            //second page
            default2.add("Colorcode examples:");
            default2.add("&7[&61&7] &6Don't grief!");
            default2.add("&7[&62&7] &6Don't hack!");
            //third page
            default3.add("Minimessage examples:");
            default3.add("<click:run_command:/say hello>Click</click> to say hello");
            default3.add("<hover:show_text:'<red>test'>hover :)</hover>");
            default3.add(" ");
            default3.add("If you need help configuring, go to the wiki!");
            default3.add("http://bit.ly/bookrules-wiki");
            configFile.set("content.1", default1);
            configFile.set("content.2", default2);
            configFile.set("content.3", default3);
        }

        int i = 1;
        for (String page : bookContent) {
            messageManager.sendDebug("\n" + "Page " + i + ":\n" + page);
            i++;
        }
    }

    public void openBook(Player player, String permission, boolean command) {
        if (command) {
            open(player, permission);
        } else {
            rulesAPI.playerHasAcceptedRules(player.getUniqueId().toString()).thenAccept((hasAccepted) -> {
                if (hasAccepted) {
                    messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTED, player.getName());
                } else {
                    open(player, permission);
                }
            });
        }
    }

    public void open(Player player, String permission) {
        ArrayList<String> bookContent = this.bookContent;
        ArrayList<BaseComponent[]> customBookContent = new ArrayList<>();
        String acceptText = this.acceptText;

        if (!permissionManager.hasExtraPermission(player, permission)) {
            messageManager.sendDebug(MessageManager.DebugType.DEBUG_NO_PERMISSION, player.getName(), permission);
            return;
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            acceptText = PlaceholderAPI.setPlaceholders(player, acceptText);
            for (int i = 0; i < bookContent.size(); i++) {
                String tempText = PlaceholderAPI.setPlaceholders(player, bookContent.get(i));
                bookContent.set(i, tempText);
            }
        }

        for (String page : bookContent) {
            Component component = MiniMessage.miniMessage().deserialize(BookRules.legacy(page));
            customBookContent.add(BungeeComponentSerializer.get().serialize(component));
        }

        Component component = MiniMessage.miniMessage().deserialize(acceptText, Placeholder.component("acceptbutton", Component.text(acceptButton).clickEvent(ClickEvent.runCommand("/acceptrules"))),
                        Placeholder.component("declinebutton", Component.text(declineButton).clickEvent(ClickEvent.runCommand("/declinerules"))));
        BaseComponent[] acceptTextComponent = BungeeComponentSerializer.get().serialize(component);

        for (BaseComponent[] bookContentComponent : customBookContent) {
            bookMeta.spigot().addPage(bookContentComponent);
        }

        messageManager.sendDebug("Opening book to the player " + player.getName() + ".");

        bookMeta.spigot().addPage(acceptTextComponent);
        bookMeta.setTitle("BookRules");
        bookMeta.setAuthor("Server");
        book.setItemMeta(bookMeta);

        if (MCVersion.getVersion().isMajorNewerThan(MCVersion.v1_14_R1.getMajorVersion())) {
            player.openBook(book);
            return;
        } else {
            Bukkit.getLogger().warning("§4You are running an unsupported version! Don't expect support/working features!");
        }

        int slot = player.getInventory().getHeldItemSlot();
        ItemStack old = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, book);

        ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, (byte) 0);
        buf.writerIndex(1);

        try {
            Constructor<?> serializerConstructor = NMSUtils.getNMSClass("PacketDataSerializer").getConstructor(ByteBuf.class);
            Object packetDataSerializer = serializerConstructor.newInstance(buf);

            Constructor<?> keyConstructor = NMSUtils.getNMSClass("MinecraftKey").getConstructor(String.class);
            Object bookKey = keyConstructor.newInstance("minecraft:book_open");

            Constructor<?> titleConstructor = NMSUtils.getNMSClass("PacketPlayOutCustomPayload")
                    .getConstructor(bookKey.getClass(), NMSUtils.getNMSClass("PacketDataSerializer"));
            Object payload = titleConstructor.newInstance(bookKey, packetDataSerializer);

            NMSUtils.sendPacket(player, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.getInventory().setItem(slot, old);
    }
}
