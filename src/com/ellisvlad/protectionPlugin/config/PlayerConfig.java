package com.ellisvlad.protectionPlugin.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ellisvlad.protectionPlugin.Main;

public class PlayerConfig {
	
	private int playerId;
	private String tool_id, name;
	private int tool_data;
	private Location points[]=new Location[2];

	public PlayerConfig(int playerId, String name, String tool_id, int tool_data) {
		this.playerId=playerId;
		this.name=name;
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
					rs.getString("name"),
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
				  "INSERT INTO `players`(`uuidLo`, `uuidHi`, `name`, `tool_id`, `tool_data`) VALUES (?, ?, ?, ?, ?);"
			);
			ps.setLong(1, p_uuid.getLeastSignificantBits());
			ps.setLong(2, p_uuid.getMostSignificantBits());
			ps.setString(3, Bukkit.getPlayer(p_uuid).getName());
			ps.setString(4, Main.globalConfig.default_tool_name);
			ps.setInt(5, Main.globalConfig.default_tool_data);
			ps.executeUpdate();
			ps.close();
			
			return getPlayerConfig(owner, p_uuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void saveConfig(GlobalConfig owner, Player player) {
		PlayerConfig pConfig=getPlayerConfig(owner, player.getUniqueId());
		try {
			PreparedStatement ps=Main.globalConfig.dbConnection.prepareStatement(
				"UPDATE `players` SET `name`=?, `tool_id`=?, `tool_data`=? WHERE `pid`=?"
			);
			ps.setString(1, player.getName());
			ps.setString(2, pConfig.tool_id);
			ps.setInt(3, pConfig.tool_data);
			ps.setInt(4, pConfig.playerId);
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getTool_id() {
		return tool_id;
	}

	public String getName() {
		return name;
	}

	public int getTool_data() {
		return tool_data;
	}

	public Location[] getPoints() {
		return points;
	}

	public Location getFirstPoint() {
		return points[0];
	}

	public Location getSecondPoint() {
		return points[1];
	}

	public void setFirstPoint(Location point) {
		points[0]=point;
	}

	public void setSecondPoint(Location point) {
		points[1]=point;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setToolItem(ItemStack is) {
		tool_id=is.getType().toString();
		tool_data=is.getDurability();
	}
	
}