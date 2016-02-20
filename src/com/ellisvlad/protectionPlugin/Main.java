package com.ellisvlad.protectionPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	public static Plugin plugin;
	public static GlobalConfig globalConfig;
	
	public static void main(String[] args) { //Test
		DatabaseConnector.init();
		globalConfig=DatabaseConnector.loadDatabaseConfig();
		if (globalConfig==null) {
			System.err.println("Something went wrong when initialising database! Cannot continue!");
			return;
		}
	}
	
	@Override
	public void onEnable() {
		plugin = this;

		Logger.out.println("Initialising...");
		Long startTime=System.currentTimeMillis();
		DatabaseConnector.init();
		globalConfig=DatabaseConnector.loadDatabaseConfig();
		if (globalConfig==null) {
			Logger.err.println("Something went wrong when initialising database! Cannot continue!");
			return;
		}
		
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new EventListener(), this);
		Logger.out.println("All OK after "+(System.currentTimeMillis()-startTime)+"ms");
	}
	
	@Override
	public void onDisable() {
		
	}

}
