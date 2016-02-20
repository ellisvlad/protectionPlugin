package com.ellisvlad.protectionPlugin.ToolItem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ellisvlad.protectionPlugin.Main;
import com.ellisvlad.protectionPlugin.Utils;
import com.ellisvlad.protectionPlugin.config.PlayerConfig;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;

public class TI_Handler {
	
	private TI_Handler() {};
	
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

	public static ItemStack makeTool(Player p) {
		PlayerConfig pc=Main.globalConfig.getPlayerConfig(p);
		Material mat=Material.getMaterial(pc.tool_id);
		if (mat==null) mat=Material.getMaterial(pc.tool_id);
		
		return Utils.makeItemStack(mat, 1, (short)pc.tool_data, Main.globalConfig.tool_name, Main.globalConfig.tool_description.split("\n"));
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
	
}
