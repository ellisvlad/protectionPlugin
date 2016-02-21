package com.ellisvlad.protectionPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUpdateSign;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenSignEditor;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateSign;
import net.minecraft.server.v1_8_R3.World;

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

	public static ItemStack makeItemStack(Material mat, int amount, int damage, String name, String[] lore) {
		return makeItemStack(mat, amount, damage, name, lore, false);
	}
	public static ItemStack makeItemStack(Material mat, int amount, int damage, String name, String[] lore, boolean isPluginItem) {
		net.minecraft.server.v1_8_R3.ItemStack nis=Utils.getItemStackHandle(
			CraftItemStack.asCraftCopy(new ItemStack(mat, amount, (short)damage))
		);
		NBTTagCompound tags=new NBTTagCompound();
		NBTTagCompound displayTag=new NBTTagCompound();
		NBTTagList loreTag=new NBTTagList();
		if (isPluginItem) {
			tags.setBoolean("protectionPluginItem", true);
			tags.setBoolean("Unbreakable", true);
			tags.setInt("HideFlags", 0b111111);
		}
		if (name!=null) displayTag.setString("Name", "§r"+name);
		if (lore!=null) {
			for (String str:lore) {
				loreTag.add(new NBTTagString("§r"+str));
			}
		}
		displayTag.set("Lore", loreTag);
		tags.set("display", displayTag);
		nis.setTag(tags);
		
		return CraftItemStack.asCraftMirror(nis);
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
			blocks.add(packetChangeBlocks.new MultiBlockChangeInfo(blockPos++,
				CraftMagicNumbers.getBlock(Material.STAINED_GLASS).fromLegacyData(color.getWoolData())
			));
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
	
	public static List<Pair<Block, BlockFace>> getDirectionsInRay(List<Block> blocks) {
		List<Pair<Block, BlockFace>> ret=new ArrayList<>();
		Block prevBlock=null;
		for (Block block:blocks) {
			if (prevBlock!=null) {
				BlockFace direction = null;
				Location relPos=block.getLocation().clone().subtract(prevBlock.getLocation());
				if (relPos.getX()>0) direction=BlockFace.EAST; else if (relPos.getX()<0) direction=BlockFace.WEST;
				if (relPos.getY()>0) direction=BlockFace.UP; else if (relPos.getY()<0) direction=BlockFace.DOWN;
				if (relPos.getZ()>0) direction=BlockFace.SOUTH; else if (relPos.getZ()<0) direction=BlockFace.NORTH;
				ret.add(Pair.of(block, direction));
			}
			prevBlock=block;
		}
		return ret;
	}
	
	public static List<Pair<Block, BlockFace>> filterBlockDirListToXY(List<Pair<Block, BlockFace>> blocks, int x, int z) {
		Iterator<Pair<Block, BlockFace>> blocksItr=blocks.iterator();
		while (blocksItr.hasNext()) {
			Pair<Block, BlockFace> blockDir=blocksItr.next();
			if (blockDir.getLeft().getLocation().getBlockX()!=x || blockDir.getLeft().getLocation().getBlockZ()!=z) blocksItr.remove();
		}
		return blocks;
	}
	
	public static class delayedBeaconMove extends Thread {
		
		Location pos;
		Player player;
		BlockFace rel;
		
		public delayedBeaconMove(Location pos, Player player, BlockFace rel) {
			this.pos=pos;
			this.player=player;
			this.rel=rel;
			start();
		}
		
		@Override
		public void run() {
			Utils.clearBeaconLine(pos, player);
			pos=pos.getBlock().getRelative(rel).getLocation();
			try {Thread.sleep(1000/20);} catch (Exception e) {}; //1 tick
			Utils.sendBeaconLine(pos, player, DyeColor.YELLOW);
		}
		
	}

	public static void delayedPromptText(Location pos, Player player, String[] text, delayedPromptTextResponseHandler returnMethod) {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Main.plugin, PacketType.Play.Client.UPDATE_SIGN) {
        	@Override
            public void onPacketReceiving(PacketEvent event) {
        		PacketPlayInUpdateSign packet=(PacketPlayInUpdateSign)event.getPacket().getHandle();
        		delayedPromptTextResponseHandler handler=delayedPromptTextClass.returnHandlers.remove(packet.a().asLong());
        		if (handler!=null) {
        			List<String> lines=new ArrayList<String>(4);
        			for (IChatBaseComponent line:packet.b()) lines.add(line.getText());
        			handler.handle(lines.toArray(new String[4]), event.getPlayer());
        			
        			PacketPlayOutBlockChange packetChangeBlock=new PacketPlayOutBlockChange(((CraftWorld)event.getPlayer().getWorld()).getHandle(), packet.a());
        			packetChangeBlock.block=Blocks.AIR.getBlockData();
        			Utils.safePacketSend((CraftPlayer)event.getPlayer(), packetChangeBlock);
        		}
            }
        });
		
		new delayedPromptTextClass(pos, player, text, returnMethod);
	}
	
	public static interface delayedPromptTextResponseHandler {
		public void handle(String[] lines, Player player);
	}
	
	private static class delayedPromptTextClass extends Thread {
		
		World world;
		BlockPosition pos;
		Player player;
		String text[];
		public static HashMap<Long, delayedPromptTextResponseHandler> returnHandlers=new HashMap<>();
		
		private delayedPromptTextClass(Location pos, Player player, String[] text, delayedPromptTextResponseHandler responseHandler) {
			this.world=((CraftWorld)pos.getWorld()).getHandle();
			this.pos=new BlockPosition(player.getLocation().getBlockX(), 254, player.getLocation().getBlockZ());
			this.player=player;
			this.text=text;
			returnHandlers.put(this.pos.asLong(), responseHandler);
			start();
		}
		
		@Override
		public void run() {
			PacketPlayOutBlockChange packetChangeBlock=new PacketPlayOutBlockChange(world, pos);
			packetChangeBlock.block=Blocks.STANDING_SIGN.getBlockData();
			Utils.safePacketSend((CraftPlayer)player, packetChangeBlock);

			PacketPlayOutUpdateSign packetSignData=new PacketPlayOutUpdateSign(
				world, pos,
				new ChatComponentText[]{
					new ChatComponentText(text[0]),
					new ChatComponentText(text[1]),
					new ChatComponentText(text[2]),
					new ChatComponentText(text[3])
				}
			);
			Utils.safePacketSend((CraftPlayer)player, packetSignData);
			
			try {Thread.sleep(1000/20);} catch (Exception e) {}; //1 tick
			
			PacketPlayOutOpenSignEditor packetSignEditor=new PacketPlayOutOpenSignEditor(pos);
			Utils.safePacketSend((CraftPlayer)player, packetSignEditor);
		}
		
	}

}

