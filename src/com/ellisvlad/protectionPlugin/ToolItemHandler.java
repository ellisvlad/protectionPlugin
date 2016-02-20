package com.ellisvlad.protectionPlugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.ellisvlad.protectionPlugin.config.PlayerConfig;

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

	public static ItemStack makeTool(Player p) {
		PlayerConfig pc=Main.globalConfig.getPlayerConfig(p);
		Material mat=Material.getMaterial(pc.tool_id);
		if (mat==null) mat=Material.getMaterial(pc.tool_id);
		return new ItemStack(Material.getMaterial(pc.tool_id), 1, (short)pc.tool_data);
	}

	public static class EventListener implements Listener {
		
	}
	
}
