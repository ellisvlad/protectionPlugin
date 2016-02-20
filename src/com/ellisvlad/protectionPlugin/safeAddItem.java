package com.ellisvlad.protectionPlugin;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class safeAddItem {
	
	private safeAddItem() {};
	
	public static void add(final Inventory inv, final ItemStack is) {
		Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
			@Override
			public void run() {
				inv.addItem(is);
			}
		});
	}
}

