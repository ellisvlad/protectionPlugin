package com.ellisvlad.protectionPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.ellisvlad.protectionPlugin.Database.DatabaseConnection;
import com.ellisvlad.protectionPlugin.Regions.RegionController;
import com.ellisvlad.protectionPlugin.Regions.Region_Events;
import com.ellisvlad.protectionPlugin.ToolItem.TI_EventListener;
import com.ellisvlad.protectionPlugin.ToolItem.gui.TI_Gui_Main;
import com.ellisvlad.protectionPlugin.config.GlobalConfig;

public class Main extends JavaPlugin {
	
	public static Plugin plugin;
	public static GlobalConfig globalConfig;
	
	public static void main(String[] args) { //Test
//		Region r=Region.makeUnownedRegion(0, 0, 0, 0);
//		r.addMember(22);
//		r.addMember(33);
//		r.addMember(5);
//		System.out.println(r);
//		System.out.println(r.hasMember(2));
//		System.out.println(r.hasMember(33));
//		r.removeMember(33);
//		System.out.println(r.hasMember(33));
		
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
		
		try {ProtocolLibrary.getProtocolManager();} catch (Exception e) {
			Logger.err.println("ProtocolLib did not load!");
			return;
		}
		
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
		pm.registerEvents(new Region_Events(), this);
		pm.registerEvents(new TI_Gui_Main.TI_Gui_Listener(), this);
		
		globalConfig.regionController=new RegionController();
		
		
		Logger.out.println("All OK after "+(System.currentTimeMillis()-startTime)+"ms");
	}
	
	@Override
	public void onDisable() {
		globalConfig.regionController.savePending();
	}

}
