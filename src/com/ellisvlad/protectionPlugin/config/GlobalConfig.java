package com.ellisvlad.protectionPlugin.config;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.ellisvlad.protectionPlugin.Logger;
import com.ellisvlad.protectionPlugin.Database.DatabaseConnection;
import com.ellisvlad.protectionPlugin.Regions.RegionController;

public class GlobalConfig {

	@saveToDatabase
	public String default_tool_name;
	@saveToDatabase
	public int default_tool_data;
	@saveToDatabase
	public String welcome_message;
	@saveToDatabase
	public String help_message;
	@saveToDatabase
	public String tool_name;
	@saveToDatabase
	public String tool_description;
	@saveToDatabase
	public String first_point_selected;
	@saveToDatabase
	public String second_point_selected;
	@saveToDatabase
	public String cleared_selection;
	@saveToDatabase
	public String created_region;
	@saveToDatabase
	public String already_is_region;
	@saveToDatabase
	public int regionSaveInterval;
	@saveToDatabase
	public String passThroughDenied;
	@saveToDatabase
	public String blockBreakDenied;
	@saveToDatabase
	public String blockPlaceDenied;
	@saveToDatabase
	public String pvpDenied;
	@saveToDatabase
	public String itemDropDenied;
	@saveToDatabase
	public String teleportInDenied;
	@saveToDatabase
	public String teleportOutDenied;
	@saveToDatabase
	public String lighterDenied;
	@saveToDatabase
	public String chestAccessDenied;
	@saveToDatabase
	public String blockUseDenied;
	@saveToDatabase
	public String vehicleBreakDenied;
	@saveToDatabase
	public String chatDenied;

	public DatabaseConnection dbConnection;
	public RegionController regionController;
	
	protected HashMap<UUID, PlayerConfig> playerConfigCache=new HashMap<>();
	
	public void validateConfig() {
		if (Material.getMaterial(default_tool_name)==null) {
			Logger.err.println("default_tool_name was invalid! Defaulting to a wooden axe.");
			default_tool_name=Material.WOOD_AXE.name();
		}
	}

	public PlayerConfig getPlayerConfig(Player p) {
		if (p==null) return null;
		return getPlayerConfig(p.getUniqueId());
	}
	public PlayerConfig getPlayerConfig(UUID p_uuid) {
		return PlayerConfig.getPlayerConfig(this, p_uuid);
	}
	
}
