package com.ellisvlad.protectionPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.ellisvlad.protectionPlugin.Database.DatabaseConnection;
import com.ellisvlad.protectionPlugin.Regions.RegionCache;
import com.ellisvlad.protectionPlugin.Regions.RegionController;
import com.ellisvlad.protectionPlugin.ToolItem.TI_EventListener;
import com.ellisvlad.protectionPlugin.config.GlobalConfig;

public class Main extends JavaPlugin {
	
	public static Plugin plugin;
	public static GlobalConfig globalConfig;
	
	public static void main(String[] args) { //Test
		RegionCache cache=new RegionCache();
		cache.addEntry(5, 9, 0, 0);
		cache.addEntry(7, 16, 0, 0);
		cache.addEntry(14, 17, 0, 0);
		cache.addEntry(18, 22, 0, 0);
		cache.addEntry(19, 21, 0, 0);

		cache.pack();
		
		cache.searchX(10);
		
//		GlobalConfig c=new GlobalConfig();
//		UUID uuid=UUID.randomUUID();
//		c.getPlayerConfig(uuid);
		
//		DatabaseConnector.init();
//		globalConfig=DatabaseConnector.loadDatabaseConfig();
//		if (globalConfig==null) {
//			System.err.println("Something went wrong when initialising database! Cannot continue!");
//			return;
//		}
	}
	
	@Override
	public void onEnable() {
		plugin = this;

		Logger.out.println("Initialising...");
		Long startTime=System.currentTimeMillis();
		DatabaseConnection dbConnection=new DatabaseConnection();
		globalConfig=dbConnection.loadDatabaseConfig();
		if (globalConfig==null) {
			Logger.err.println("Something went wrong when initialising database! Cannot continue!");
			return;
		}
		globalConfig.dbConnection=dbConnection;
		
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new CommandListener(), this);
		pm.registerEvents(new TI_EventListener(), this);
		
		globalConfig.regionController=new RegionController();
		
		Logger.out.println("All OK after "+(System.currentTimeMillis()-startTime)+"ms");
	}
	
	@Override
	public void onDisable() {
		
	}

}
