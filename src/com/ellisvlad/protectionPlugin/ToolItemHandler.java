package com.ellisvlad.protectionPlugin;

import java.lang.reflect.Field;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
		safeAddItem.add(p.getInventory(), is);
		return is;
	}

	public static CraftItemStack makeTool(Player p) {
		PlayerConfig pc=Main.globalConfig.getPlayerConfig(p);
		Material mat=Material.getMaterial(pc.tool_id);
		if (mat==null) mat=Material.getMaterial(pc.tool_id);
		
		net.minecraft.server.v1_8_R3.ItemStack nis=getItemStackHandle(
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
	
	public static void renderIfNecessary(net.minecraft.server.v1_8_R3.ItemStack is, Player p) {
		NBTTagCompound displayTag=(NBTTagCompound)is.getTag().get("display");
		displayTag.setString("Name", "§r"+Main.globalConfig.tool_name);
		NBTTagList loreTag=new NBTTagList();
		for (String str:Main.globalConfig.tool_description.split("\n")) {
			loreTag.add(new NBTTagString("§r"+str));
		}
		displayTag.set("Lore", loreTag);
	}
	
	public static boolean isToolItem(ItemStack is) {
		net.minecraft.server.v1_8_R3.ItemStack nis=getItemStackHandle(is);
		if (!nis.hasTag()) return false;
		if (!nis.getTag().hasKey("protectionPluginItem")) return false;
		return true;
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

	public static class EventListener implements Listener {
		
		@EventHandler
		public void onDrop(PlayerDropItemEvent e) {
			if (!isToolItem(e.getItemDrop().getItemStack())) return;
			net.minecraft.server.v1_8_R3.ItemStack nis=getItemStackHandle(e.getItemDrop().getItemStack());

			renderIfNecessary(nis, e.getPlayer());
			
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
			net.minecraft.server.v1_8_R3.ItemStack nis=getItemStackHandle(is);

			renderIfNecessary(nis, e.getPlayer());
			
			e.setCancelled(true);
		}
		
	}
	
}
