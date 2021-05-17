package de.skyslycer.bookrules.core;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import de.skyslycer.bookrules.api.RulesAPI;
import de.skyslycer.bookrules.util.*;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookManager {
    MessageManager messageManager;
    PermissionManager permissionManager;
    RulesAPI rulesAPI = new RulesAPI();

    public ArrayList<String> bookContent = new ArrayList<>();
    public ArrayList<String> rawBookContent;
    public String acceptText;
    public String acceptButton;
    public String declineButton;

    public void injectData(MessageManager messageManager, PermissionManager permissionManager) {
        this.messageManager = messageManager;
        this.permissionManager = permissionManager;
    }

    public void instantiateContent(YamlFileWriter configFile) {
        if(configFile.getString("accept-button") == null) {
            configFile.setValue("accept-button", "&a[ACCEPT]");
        }
        acceptButton = configFile.getString("accept-button");
        messageManager.sendDebug("Accept button: " + acceptButton);
        acceptButton = ChatColor.translateAlternateColorCodes('&', acceptButton);

        if(configFile.getString("decline-button") == null) {
            configFile.setValue("decline-button", "&4[DECLINE]");
        }
        declineButton = configFile.getString("decline-button");
        messageManager.sendDebug("Decline button: " + declineButton);
        declineButton = ChatColor.translateAlternateColorCodes('&', declineButton);

        if (configFile.getString("accept-text") == null) {
            configFile.setValue("accept-text", "&7If you accept the &7rules, you agree to &7the rules and &7punishments for &7breaking them. If you &7decline, you will be &7kicked. \n\n<acceptbutton> <declinebutton>");
        }
        acceptText = configFile.getString("accept-text");
        messageManager.sendDebug("Text on the last page: " + acceptText);
        acceptText = ChatColor.translateAlternateColorCodes('&', acceptText);

        if (configFile.contains("content")) {
            String page;
            bookContent.clear();
            for (String key : configFile.getKeys("content")) {
                rawBookContent = configFile.getArrayList("content." + key);
                page = String.join("\n§r", rawBookContent);
                page = ChatColor.translateAlternateColorCodes('&', page);
                bookContent.add(page);
            }
        } else {
            //set content
            ArrayList<String> default1 = new ArrayList<>();
            ArrayList<String> default2 = new ArrayList<>();
            ArrayList<String> default3 = new ArrayList<>();
            //first page
            default1.add("&cIf you see this, please contact a server administrator or owner to configure this plugin properly.");
            default1.add("If you want to know how configure this plugin, please visit the plugin page. The wiki is well documented and easy to understand.");
            default1.add("Plugin page: http://bit.ly/bookrules");
            //second page
            default2.add("Colorcode examples:");
            default2.add("&7[&61&7] &6Don't grief!");
            default2.add("&7[&62&7] &6Don't hack!");
            //third page
            default3.add("Minimessage examples:");
            default3.add("<click:run_command:/say hello>Click</click> to say hello");
            default3.add("<hover:show_text:'<red>test'>TEST</hover>");
            default3.add(" ");
            default3.add("If you need help configuring, go to the wiki!");
            default3.add("http://bit.ly/bookrules-wiki");
            configFile.setValue("content.1", default1);
            configFile.setValue("content.2", default2);
            configFile.setValue("content.3", default3);
        }
        configFile.save();
        int i = 1;
        for (String page : bookContent) {
            messageManager.sendDebug("\n" + "Page " + i + ":\n" + page);
            i++;
        }
    }

    public void openBook(Player player, String permission, boolean command) {
        ArrayList<String> bookContent = this.bookContent;
        ArrayList<BaseComponent[]> customBookContent = new ArrayList<>();
        String acceptText = this.acceptText;

        if(!rulesAPI.playerHasAcceptedRules(player.getUniqueId().toString()) || command) {
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
                Component component = MiniMessage.get().parse(page);
                customBookContent.add(BungeeComponentSerializer.get().serialize(component));
            }

            List<Template> templates = Arrays.asList(
                    Template.of("acceptbutton", Component.text(acceptButton).clickEvent(
                            ClickEvent.runCommand("/acceptrules"))),
                    Template.of("declinebutton", Component.text(declineButton).clickEvent(
                            ClickEvent.runCommand("/declinerules")))
            );

            Component component = MiniMessage.get().parse(acceptText, templates);
            BaseComponent[] acceptTextComponent = BungeeComponentSerializer.get().serialize(component);

            for (BaseComponent[] bookContentComponent : customBookContent) {
                bookMeta.spigot().addPage(bookContentComponent);
            }

            messageManager.sendDebug("Opening book to the player " + player.getName() + ".");

            bookMeta.spigot().addPage(acceptTextComponent);
            bookMeta.setTitle("BookRules");
            bookMeta.setAuthor("Server");
            book.setItemMeta(bookMeta);

            if(MCVersion.getVersion().isNewerThan(MCVersion.v1_12_R1)) {
                if (MCVersion.getVersion().isNewerThan(MCVersion.v1_13_R2)) {
                    player.openBook(book);
                    return;
                }

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack old = player.getInventory().getItem(slot);
                player.getInventory().setItem(slot, book);

                ByteBuf buf = Unpooled.buffer(256);
                buf.setByte(0, (byte)0);
                buf.writerIndex(1);

                try {
                    Constructor<?> serializerConstructor = NMSUtils.getNMSClass("PacketDataSerializer").getConstructor(ByteBuf.class);
                    Object packetDataSerializer = serializerConstructor.newInstance(buf);

                    Constructor<?> keyConstructor = NMSUtils.getNMSClass("MinecraftKey").getConstructor(String.class);
                    Object bookKey = keyConstructor.newInstance("minecraft:book_open");

                    Constructor<?> titleConstructor = NMSUtils.getNMSClass("PacketPlayOutCustomPayload").getConstructor(bookKey.getClass(), NMSUtils.getNMSClass("PacketDataSerializer"));
                    Object payload = titleConstructor.newInstance(bookKey, packetDataSerializer);

                    NMSUtils.sendPacket(player, payload);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                player.getInventory().setItem(slot, old);
            } else {
                if (MCVersion.getVersion().isOlderThan(MCVersion.v1_12_R1)) {
                    Bukkit.getLogger().warning("§4You are running an unsupported version! Don't expect support/working features!");
                }

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack old = player.getInventory().getItem(slot);
                player.getInventory().setItem(slot, book);

                try {
                    PacketContainer pc = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
                    pc.getModifier().writeDefaults();
                    ByteBuf bf = Unpooled.buffer(256);
                    bf.setByte(0, 0);
                    bf.writerIndex(1);
                    pc.getStrings().write(0, "MC|BOpen");
                    pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, pc);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                player.getInventory().setItem(slot, old);
            }
        }else messageManager.sendDebug(MessageManager.DebugType.DEBUG_ACCEPTED, player.getName());
    }
}
