package com.ellisvlad.protectionPlugin.ToolItem.gui;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.ellisvlad.protectionPlugin.Main;
import com.ellisvlad.protectionPlugin.Utils;
import com.ellisvlad.protectionPlugin.Regions.Region;
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions.RegionPermissionsBooleanValue;
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions.RegionPermissionsGroupValue;
import com.ellisvlad.protectionPlugin.ToolItem.TI_Handler;
import com.ellisvlad.protectionPlugin.config.PlayerConfig;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

public class TI_Gui_Main {

	public static Inventory mainInventory=null;
	
	public TI_Gui_Main() {
		if (mainInventory!=null) return;
		
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Main.plugin, PacketType.Play.Client.SET_CREATIVE_SLOT) {
        	@Override
            public void onPacketReceiving(PacketEvent event) {
        		if (event.getPacket().getItemModifier()==null || event.getPacket().getItemModifier().read(0)==null || event.getPacket().getItemModifier().read(0).getType()==Material.AIR) return;
    			if (!TI_Gui_Listener.waitingForItem.remove(event.getPlayer())) return;

    			PacketPlayOutGameStateChange packetGamemode=new PacketPlayOutGameStateChange(3, event.getPlayer().getGameMode().ordinal());
    			Utils.safePacketSend((CraftPlayer)event.getPlayer(), packetGamemode);
    			
    			ItemStack is=event.getPacket().getItemModifier().read(0);
    			PlayerConfig pConfig=Main.globalConfig.getPlayerConfig(event.getPlayer());
    			
    			pConfig.setToolItem(is);
    			PlayerConfig.saveConfig(Main.globalConfig, event.getPlayer());
    			for (int i=0; i!=event.getPlayer().getInventory().getSize(); i++) {
    				if (event.getPlayer().getInventory().getItem(i)!=null &&
    					event.getPlayer().getInventory().getItem(i).getType()!=Material.AIR) {
    					if (TI_Handler.isToolItem(event.getPlayer().getInventory().getItem(i))) {
    						event.getPlayer().getInventory().setItem(i, null);
    					}
    				}
    			}
    			
    			event.getPlayer().sendMessage("Protection tool set to "+is.getType()+":"+is.getDurability());
    			event.setCancelled(true);
            }
        });
		
		mainInventory=Bukkit.createInventory(null, 45, "Protection Config");
		for (int i=0; i!=9; i++) {
			mainInventory.setItem(i, new ItemStack(i%8==0?Material.SLIME_BALL:Material.SNOW_BALL)); // Top
			mainInventory.setItem(36+i, new ItemStack(i%8==0?Material.SLIME_BALL:Material.SNOW_BALL)); // Bottom
		}
		for (int i=0; i!=3; i++) {
			mainInventory.setItem(9+i*9, new ItemStack(Material.SNOW_BALL)); // Left
			mainInventory.setItem(17+i*9, new ItemStack(Material.SNOW_BALL)); // Right
		}

		mainInventory.setItem(21, Utils.makeItemStack(Material.BOOK, 1, 0, "My Areas", new String[]{"Click to show areas"}, false));
		mainInventory.setItem(22, Utils.makeItemStack(Material.WOOD_AXE, 1, 0, "My Tool", new String[]{"Click to edit tool"}, false));
		mainInventory.setItem(23, Utils.makeItemStack(Material.BARRIER, 1, 0, "Cancel", new String[]{"Click to close Gui"}, false));
	}
	
	public void show(Player p) {
		p.openInventory(mainInventory);
	}
	
	public static class TI_Gui_Listener implements Listener {
		
		static Set<Player> waitingForItem=new HashSet<>();
		
		@EventHandler
		public void onMove(PlayerMoveEvent e) {
			if (e.getFrom().getBlockX()==e.getTo().getBlockX() && e.getFrom().getBlockZ()==e.getTo().getBlockZ()) return;
			if (!waitingForItem.remove(e.getPlayer())) return;

			PacketPlayOutGameStateChange packetGamemode=new PacketPlayOutGameStateChange(3, e.getPlayer().getGameMode().ordinal());
			Utils.safePacketSend((CraftPlayer)e.getPlayer(), packetGamemode);
		}
		
		@EventHandler
		public void onClick(InventoryClickEvent e) {
			if (e.getRawSlot()>=45) return;
			PlayerConfig pConfig=Main.globalConfig.getPlayerConfig((Player)e.getWhoClicked());

			if (e.getInventory().equals(mainInventory)) {
				e.setCancelled(true);
				
				switch (e.getRawSlot()) {
				case 21: //My areas
					e.getWhoClicked().openInventory(makeMyAreas(0, pConfig.getPlayerId()));
				break;
				case 22: //My tool
					e.getWhoClicked().closeInventory();
					
					waitingForItem.add((Player)e.getWhoClicked());
					
					PacketPlayOutTitle titlePacket=new PacketPlayOutTitle(EnumTitleAction.TITLE, new ChatComponentText(" "));
					Utils.safePacketSend((CraftPlayer)e.getWhoClicked(), titlePacket);
					
					titlePacket=new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, new ChatComponentText("§6Put an item into your hotbar from creative"));
					Utils.safePacketSend((CraftPlayer)e.getWhoClicked(), titlePacket);
					
					titlePacket=new PacketPlayOutTitle(EnumTitleAction.TIMES, null, 20, 40, 5);
					Utils.safePacketSend((CraftPlayer)e.getWhoClicked(), titlePacket);
					
					PacketPlayOutGameStateChange packetGamemode=new PacketPlayOutGameStateChange(3, 1);
					Utils.safePacketSend((CraftPlayer)e.getWhoClicked(), packetGamemode);
				break;
				case 23: //Close
					e.getWhoClicked().closeInventory();
				break;
				}
				return;
			}
			if (e.getInventory().getName().startsWith("My Areas - Page ")) {
				e.setCancelled(true);
				
				if (e.getCurrentItem().getType()==Material.BOOK) {
					// Very hacky! Improve this later
					int regionId=Integer.parseInt(Utils.getItemStackHandle(e.getCurrentItem()).getTag().getCompound("display").getList("Lore", 8).getString(0).substring(10));
					e.getWhoClicked().openInventory(makeAreaInfo(Main.globalConfig.regionController.getRegionById(regionId)));
					return;
				}
				
				switch (e.getRawSlot()) {
				case 33: //Next page
					if (e.getInventory().getItem(33).getDurability()==10) {
						e.getWhoClicked().openInventory(makeMyAreas(Integer.parseInt(e.getInventory().getName().substring(16)), pConfig.getPlayerId()));
					}
				break;
				case 34: // Back
					e.getWhoClicked().openInventory(mainInventory);
				break;
				}
				return;
			}
			if (e.getInventory().getName().equals("Permissions View")) {
				e.setCancelled(true);
				
				if (e.getRawSlot()==4 || e.getRawSlot()==44) { //Back
					e.getWhoClicked().openInventory(makeMyAreas(0, pConfig.getPlayerId()));
					return;
				}
				
				if (e.getCurrentItem()==null || e.getCurrentItem().getType()==Material.AIR) return;
				
				NBTTagCompound nbt=Utils.getItemStackHandle(e.getCurrentItem()).getTag();
				int regionId=Integer.parseInt(Utils.getItemStackHandle(e.getInventory().getItem(4)).getTag().getCompound("display").getList("Lore", 8).getString(0).substring(10));
				Region region=Main.globalConfig.regionController.getRegionById(regionId);
				String fieldName=nbt.getCompound("display").getString("Name").substring(4);
				String value=nbt.getCompound("display").getList("Lore", 8).getString(0).substring(13);
				
				try {
					Field field=region.getPermissions().getClass().getField(fieldName);
					if (field.get(region.getPermissions()) instanceof RegionPermissionsGroupValue) {
						switch (value) {
						case "None": value="Owner"; break;
						case "Owner": value="Members"; break;
						case "Members": value="All"; break;
						case "All": value="None"; break;
						}
						field.set(region.getPermissions(), RegionPermissionsGroupValue.valueOf(value));
					}
					if (field.get(region.getPermissions()) instanceof RegionPermissionsBooleanValue) {
						field.set(region.getPermissions(), RegionPermissionsBooleanValue.valueOf(value.equals("True")?"False":"True"));
					}
				} catch (Exception e1) {};
				
				Main.globalConfig.regionController.pendSave(region);
				e.getWhoClicked().openInventory(makeAreaInfo(Main.globalConfig.regionController.getRegionById(regionId)));
				
				return;
			}
		}

		private Inventory makeMyAreas(int i, int playerId) {
			Inventory inv=Bukkit.createInventory(null, 45, "My Areas - Page "+(i+1));
			for (int j=0; j!=9; j++) {
				inv.setItem(j, new ItemStack(j%8==0?Material.SLIME_BALL:Material.SNOW_BALL)); // Top
				inv.setItem(36+j, new ItemStack(j%8==0?Material.SLIME_BALL:Material.SNOW_BALL)); // Bottom
			}
			for (int j=0; j!=3; j++) {
				inv.setItem(9+j*9, new ItemStack(Material.SNOW_BALL)); // Left
				inv.setItem(17+j*9, new ItemStack(Material.SNOW_BALL)); // Right
			}

			int ignoreCount=i*33, regionPos=10;
			Set<Region> regions=Main.globalConfig.regionController.getRegionsByOwner(playerId);
			for (Region region:regions) {
				if (ignoreCount>0) ignoreCount--; else {
					inv.setItem(regionPos++, Utils.makeItemStack(Material.BOOK, 1, 0, "§6Region", new String[]{
						"§7ID: §r"+region.getId(),
						"§7Name: §r"+region.getName(),
						"§7Location: §r"+region.getLocation(),
						"§7Members: §r"+region.listMembers(),
						"",
						"§6Click to see permissions"
					}, false));
					if (regionPos==34) break;
					if (regionPos%9 == 8) regionPos+=2;
				}
			}
			if (regionPos==34)
				inv.setItem(33, Utils.makeItemStack(Material.INK_SACK, 1, 10, "§6Next Page", new String[]{""}, false));
			else
				inv.setItem(33, Utils.makeItemStack(Material.INK_SACK, 1, 8, "§7Next Page", new String[]{""}, false));
			inv.setItem(34, Utils.makeItemStack(Material.BARRIER, 1, 0, "< Back", new String[]{"Click to go back"}, false));
			
			return inv;
		}

		private Inventory makeAreaInfo(Region region) {
			Inventory inv=Bukkit.createInventory(null, 45, "Permissions View");
			
			inv.setItem(4, Utils.makeItemStack(Material.BOOK, 1, 0, "§6Region", new String[]{
				"§7ID: §r"+region.getId(),
				"§7Name: §r"+region.getName(),
				"§7Location: §r"+region.getLocation(),
				"§7Members: §r"+region.listMembers(),
				"",
				"§6Click to see go back to overview"
			}, false));
			
			int i=10;
			try {
				for (Field f:region.getPermissions().getClass().getDeclaredFields()) {
					if (f.get(region.getPermissions()) instanceof RegionPermissionsGroupValue) {
						RegionPermissionsGroupValue value=(RegionPermissionsGroupValue)f.get(region.getPermissions());
						int damage=0;
						switch (value) {
						case None: damage=14; break;
						case All: damage=5; break;
						case Members: damage=4; break;
						case Owner: damage=1; break;
						}
						inv.setItem(i++, Utils.makeItemStack(Material.WOOL, 1, damage,
							"§6"+f.getName(), new String[]{
							"§7Value: §r"+value.name(),
							"",
							"§6Click to toggle"
						}, false));
					}
					if (f.get(region.getPermissions()) instanceof RegionPermissionsBooleanValue) {
						RegionPermissionsBooleanValue value=(RegionPermissionsBooleanValue)f.get(region.getPermissions());
						inv.setItem(i++, Utils.makeItemStack(Material.INK_SACK, 1, value==RegionPermissionsBooleanValue.True?8:10,
							"§6"+f.getName(), new String[]{
							"§7Value: §r"+value.name(),
							"",
							"§6Click to toggle"
						}, false));
					}

					if (i%9 == 8) i+=2;
					if (i==35) return inv;
				}
			} catch (Exception e) {
				e.printStackTrace();
			};
			inv.setItem(44, Utils.makeItemStack(Material.BARRIER, 1, 0, "< Back", new String[]{"Click to go back"}, false));

			return inv;
		}
		
	}
	
}
