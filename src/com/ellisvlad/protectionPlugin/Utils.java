package com.ellisvlad.protectionPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;

public class Utils {
	
	private Utils() {};
	
	public static void safeAddItem(final Inventory inv, final ItemStack is) {
		Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
			@Override
			public void run() {
				inv.addItem(is);
			}
		});
	}
	
	public static void safePacketSend(final CraftPlayer player, final Packet<?> packet) {
		Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
			@Override
			public void run() {
				player.getHandle().playerConnection.sendPacket(packet);
			}
		});
	}
	
	public static net.minecraft.server.v1_8_R3.ItemStack getItemStackHandle(ItemStack is) {
		try {
			Field field=CraftItemStack.class.getDeclaredField("handle");
			field.setAccessible(true);
			return (net.minecraft.server.v1_8_R3.ItemStack)field.get(is);
		} catch (Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void sendBeaconLine(Location pos, Player p, DyeColor color) {
		try {
			PacketPlayOutMultiBlockChange packetChangeBlocks=new PacketPlayOutMultiBlockChange(
				0, new short[]{}, ((CraftChunk)pos.getChunk()).getHandle()
			);
			
			short blockPos=1;
			blockPos|=(pos.getBlockZ()&0xF)<<8;
			blockPos|=(pos.getBlockX()&0xF)<<12;

			ArrayList<PacketPlayOutMultiBlockChange.MultiBlockChangeInfo> blocks=new ArrayList<>();
			blocks.add(packetChangeBlocks.new MultiBlockChangeInfo(blockPos++, Blocks.BEACON.getBlockData()));
			while ((blockPos & 0xFF) !=254) {
				if (!pos.getChunk().getBlock(blockPos>>12 & 15, blockPos & 255, blockPos>>8 & 15).isEmpty())
					blocks.add(packetChangeBlocks.new MultiBlockChangeInfo(blockPos,
						CraftMagicNumbers.getBlock(Material.STAINED_GLASS).fromLegacyData(color.getWoolData())
					));
				blockPos++;
			}
			
			Field field=PacketPlayOutMultiBlockChange.class.getDeclaredField("b");
			field.setAccessible(true);
			field.set(packetChangeBlocks, blocks.toArray(new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[blocks.size()]));
			
			safePacketSend((CraftPlayer)p, packetChangeBlocks);
			for (int x=-1; x<=1; x++) {
				for (int z=-1; z<=1; z++) {
					PacketPlayOutBlockChange packetChangeBlock=new PacketPlayOutBlockChange(
						((CraftWorld)pos.getWorld()).getHandle(), new BlockPosition(pos.getBlockX()+x, 0, pos.getBlockZ()+z)
					);
					packetChangeBlock.block=Blocks.DIAMOND_BLOCK.getBlockData();
					safePacketSend((CraftPlayer)p, packetChangeBlock);
				}
			}
		} catch (Exception e1) {
			Logger.err.println("Could not send beacson line!");
			e1.printStackTrace();
		}
	}
	
	public static void clearBeaconLine(Location pos, Player p) {
		try {
			short[] blocks=new short[254];
			
			short blockPos=1;
			blockPos|=(pos.getBlockZ()&0xF)<<8;
			blockPos|=(pos.getBlockX()&0xF)<<12;
			
			for (int i=0; i!=254; i++) blocks[i] = blockPos++;
			
			PacketPlayOutMultiBlockChange packetChangeBlocks=new PacketPlayOutMultiBlockChange(
				blocks.length, blocks, ((CraftChunk)pos.getChunk()).getHandle()
			);
			safePacketSend((CraftPlayer)p, packetChangeBlocks);
			for (int x=-1; x<=1; x++) {
				for (int z=-1; z<=1; z++) {
					PacketPlayOutBlockChange packetChangeBlock=new PacketPlayOutBlockChange(
						((CraftWorld)pos.getWorld()).getHandle(), new BlockPosition(pos.getBlockX()+x, 0, pos.getBlockZ()+z)
					);
					safePacketSend((CraftPlayer)p, packetChangeBlock);
				}
			}
		} catch (Exception e1) {
			Logger.err.println("Could not send beacson line!");
			e1.printStackTrace();
		}
	}
	
	public static void sendMessageNewLines(Player p, String str) {
		for (String line:str.split("\n")) {
			p.sendMessage(line);
		}
	}
	
}

