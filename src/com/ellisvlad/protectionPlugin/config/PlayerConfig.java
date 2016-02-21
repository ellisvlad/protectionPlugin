package com.ellisvlad.protectionPlugin.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import org.bukkit.Location;

import com.ellisvlad.protectionPlugin.Main;
import com.ellisvlad.protectionPlugin.Database.DatabaseConnection;

public class PlayerConfig {
	
	public int playerId;
	public String tool_id;
	public int tool_data;
	public Location points[]=new Location[2];

	public PlayerConfig(int playerId, String tool_id, int tool_data) {
		this.playerId=playerId;
		this.tool_id=tool_id;
		this.tool_data=tool_data;
	}

	public static PlayerConfig getPlayerConfig(GlobalConfig owner, UUID p_uuid) {
		PlayerConfig ret=owner.playerConfigCache.get(p_uuid);
		if (ret!=null) return ret;
		
		PreparedStatement ps=null;
		try {
			ps=Main.globalConfig.dbConnection.prepareStatement(
				"SELECT * FROM `players` WHERE `uuidLo`=? AND `uuidHi`=?"
			);
			ps.setLong(1, p_uuid.getLeastSignificantBits());
			ps.setLong(2, p_uuid.getMostSignificantBits());
			ResultSet rs=ps.executeQuery();
			if (!rs.next()) { // Not in database!
				return makeDefaultConfig(owner, p_uuid);
			} else {
				ret=new PlayerConfig(
					rs.getInt("pid"),
					rs.getString("tool_id"),
					rs.getInt("tool_data")
				);
			}
			owner.playerConfigCache.put(p_uuid, ret);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {ps.close();} catch (Exception e) {e.printStackTrace();}
		}
		return null;
	}

	public static PlayerConfig makeDefaultConfig(GlobalConfig owner, UUID p_uuid) {
		try {
			PreparedStatement ps=Main.globalConfig.dbConnection.prepareStatement(
				  "INSERT INTO `players`(`uuidLo`, `uuidHi`, `tool_id`, `tool_data`) VALUES (?, ?, ?, ?);"
			);
			ps.setLong(1, p_uuid.getLeastSignificantBits());
			ps.setLong(2, p_uuid.getMostSignificantBits());
			ps.setString(3, Main.globalConfig.default_tool_name);
			ps.setInt(4, Main.globalConfig.default_tool_data);
			ps.executeUpdate();
			ps.close();
			
			return getPlayerConfig(owner, p_uuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}