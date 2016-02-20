package com.ellisvlad.protectionPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public static Plugin plugin;
	
	public static void main(String[] args) { //Test
		new DatabaseConnector();
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new EventListener(), this);
	}
	
	@Override
	public void onDisable() {
		
	}

}
