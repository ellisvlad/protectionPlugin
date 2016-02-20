package com.ellisvlad.protectionPlugin.ToolItem.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ellisvlad.protectionPlugin.Utils;

public class TI_Gui_Main {

	public static Inventory inv;
	
	public TI_Gui_Main() {
		inv=Bukkit.createInventory(null, 45, "Protection Settings");
		for (int i=0; i!=9; i++) {
			inv.setItem(i, new ItemStack(i%8==0?Material.SLIME_BALL:Material.SNOW_BALL)); // Top
			inv.setItem(36+i, new ItemStack(i%8==0?Material.SLIME_BALL:Material.SNOW_BALL)); // Bottom
		}
		for (int i=0; i!=3; i++) {
			inv.setItem(9+i*9, new ItemStack(Material.SNOW_BALL)); // Left
			inv.setItem(17+i*9, new ItemStack(Material.SNOW_BALL)); // Right
		}

		inv.setItem(20, Utils.makeItemStack(Material.BOOK, 1, 0, "My areas", new String[]{"Hi"}, false));
		inv.setItem(21, Utils.makeItemStack(Material.BOOK, 1, 0, "My areas", new String[]{"Hi"}, false));
	}
	
	public void show(Player p) {
		p.openInventory(inv);
	}
	
}
