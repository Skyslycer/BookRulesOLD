package de.skyslycer.bookrules.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Constructor;

public class BookOpener {
    public void open(Player player, ItemStack book) {
        if(MCVersion.getVersion().isNewerThan(MCVersion.v1_12_R1)) {
            if(MCVersion.getVersion().isNewerThan(MCVersion.v1_13_R2)) {
                player.openBook(book);
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
        }else {
            if(MCVersion.getVersion().isOlderThan(MCVersion.v1_12_R1)) {
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
    }
}
