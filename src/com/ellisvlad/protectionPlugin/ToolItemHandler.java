package com.ellisvlad.protectionPlugin;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.ellisvlad.protectionPlugin.config.PlayerConfig;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;

public class ToolItemHandler {
	
	private ToolItemHandler() {};
	
	/**
	 * Gives the tool to the player specified.
	 * @return Returns the new itemstack, or null if there was an error
	 * @param p Player to give to
	 * @param is ItemStack to use as tool, or null if a default stack should be created
	 */
	public static ItemStack giveTool(Player p, ItemStack is) {
		if (is==null) is=makeTool(p);
		Utils.safeAddItem(p.getInventory(), is);
		return is;
	}

	public static CraftItemStack makeTool(Player p) {
		PlayerConfig pc=Main.globalConfig.getPlayerConfig(p);
		Material mat=Material.getMaterial(pc.tool_id);
		if (mat==null) mat=Material.getMaterial(pc.tool_id);
		
		net.minecraft.server.v1_8_R3.ItemStack nis=Utils.getItemStackHandle(
			CraftItemStack.asCraftCopy(new ItemStack(Material.getMaterial(pc.tool_id), 1, (short)pc.tool_data))
		);
		NBTTagCompound tags=new NBTTagCompound();
		NBTTagCompound displayTag=new NBTTagCompound();
		NBTTagList loreTag=new NBTTagList();
		tags.setBoolean("protectionPluginItem", true);
		tags.setBoolean("Unbreakable", true);
		displayTag.setString("Name", "§r"+Main.globalConfig.tool_name);
		for (String str:Main.globalConfig.tool_description.split("\n")) {
			loreTag.add(new NBTTagString("§r"+str));
		}
		displayTag.set("Lore", loreTag);
		tags.set("display", displayTag);
		tags.setInt("HideFlags", 0b111111);
		nis.setTag(tags);
		
		return CraftItemStack.asCraftMirror(nis);
	}
	
	public static void renderToolItem(net.minecraft.server.v1_8_R3.ItemStack is) {
		NBTTagCompound displayTag=(NBTTagCompound)is.getTag().get("display");
		displayTag.setString("Name", "§r"+Main.globalConfig.tool_name);
		NBTTagList loreTag=new NBTTagList();
		for (String str:Main.globalConfig.tool_description.split("\n")) {
			loreTag.add(new NBTTagString("§r"+str));
		}
		displayTag.set("Lore", loreTag);
	}
	
	public static boolean isToolItem(ItemStack is) {
		if (is==null) return false;
		net.minecraft.server.v1_8_R3.ItemStack nis=Utils.getItemStackHandle(is);
		if (nis==null) return false;
		if (!nis.hasTag()) return false;
		if (!nis.getTag().hasKey("protectionPluginItem")) return false;
		return true;
	}

	public static class EventListener implements Listener {
		
		@EventHandler
		public void onDrop(PlayerDropItemEvent e) {
			if (!isToolItem(e.getItemDrop().getItemStack())) return;
			net.minecraft.server.v1_8_R3.ItemStack nis=Utils.getItemStackHandle(e.getItemDrop().getItemStack());
			renderToolItem(nis);
			
			e.setCancelled(true);
		}
		
		@EventHandler
		public void onInteract(PlayerInteractEvent e) {
			ItemStack is;
			if (e.hasItem())
				is=e.getItem();
			else
				is=e.getPlayer().getItemInHand();
			if (!isToolItem(is)) return;
			net.minecraft.server.v1_8_R3.ItemStack nis=Utils.getItemStackHandle(is);
			PlayerConfig pConfig=Main.globalConfig.getPlayerConfig(e.getPlayer());
			renderToolItem(nis);
			
			e.setCancelled(true);

			if (e.getAction()==Action.LEFT_CLICK_BLOCK) {
				Location pos=e.getClickedBlock().getLocation();
				
				if (pConfig.point1==null) { //First Pos
					Utils.sendBeaconLine(pos, e.getPlayer(), DyeColor.ORANGE);
					pConfig.point1=pos;
					Utils.sendMessageNewLines(e.getPlayer(), Main.globalConfig.first_point_selected);
				} else
				if (pConfig.point2==null) { //Second Pos
					Utils.sendBeaconLine(pos, e.getPlayer(), DyeColor.BLUE);
					pConfig.point2=pos;
					Utils.sendMessageNewLines(e.getPlayer(), Main.globalConfig.second_point_selected);
				} else { //Clear both
					Utils.clearBeaconLine(pConfig.point1, e.getPlayer());
					Utils.clearBeaconLine(pConfig.point2, e.getPlayer());
					pConfig.point1=null;
					pConfig.point2=null;
					Utils.sendMessageNewLines(e.getPlayer(), Main.globalConfig.cleared_selection);
				}
			}
			if (e.getAction()==Action.RIGHT_CLICK_BLOCK) {
				Utils.clearBeaconLine(e.getClickedBlock().getLocation(), e.getPlayer());
			}
		}
		
	}
	
}
