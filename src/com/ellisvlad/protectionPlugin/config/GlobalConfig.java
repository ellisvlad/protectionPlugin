package com.ellisvlad.protectionPlugin.config;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.ellisvlad.protectionPlugin.Logger;

public class GlobalConfig {

	@saveToDatabase
	public String default_tool_id;
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
	
	protected HashMap<UUID, PlayerConfig> playerConfigCache=new HashMap<>();
	
	public void validateConfig() {
		if (Material.getMaterial(default_tool_id)==null) {
			Logger.err.println("default_tool_id was invalid! Defaulting to a wooden axe.");
			default_tool_id=Material.WOOD_AXE.name();
		}
	}

	public PlayerConfig getPlayerConfig(Player p) {
		return getPlayerConfig(p.getUniqueId());
	}
	public PlayerConfig getPlayerConfig(UUID p_uuid) {
		return PlayerConfig.getPlayerConfig(this, p_uuid);
	}
	
}
