package com.ellisvlad.protectionPlugin.Regions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.ellisvlad.protectionPlugin.Logger;
import com.ellisvlad.protectionPlugin.Main;

public class RegionController {
	
	private HashMap<Integer, Region> loadedRegionsById=new HashMap<>();
	private HashSet<Region> changedRegions=new HashSet<>();
	private RegionCache cache=new RegionCache();
	private int nextRegionId;
	
	public RegionController() {
		if (!Main.globalConfig.dbConnection.isConnected()) {
			Logger.err.println("Database was not connected! Region controller could not init!");
			return;
		}
		try {
			PreparedStatement ps=Main.globalConfig.dbConnection.prepareStatement("SHOW TABLE STATUS LIKE \"regions\");");
			ResultSet rs=ps.executeQuery();
			rs.next();
			nextRegionId=rs.getInt("Auto_increment");
			ps.close();
		} catch (Exception e) {
			Logger.err.println("Region controller could not init! Could not get next region ID!");
			return;
		}
	}
	
	public Region getRegionById(int regionId) {
		if (!Main.globalConfig.dbConnection.isConnected()) {
			Logger.err.println("Database was not connected! Region controller could not init!");
			return null;
		}
		
		Region ret=loadedRegionsById.get(regionId);
		if (ret!=null) return ret;
		
		PreparedStatement ps=null;
		try {
			ps=Main.globalConfig.dbConnection.prepareStatement("SELECT * FROM `regions` WHERE `rid`=? LIMIT 1");
			ps.setInt(1, regionId);
			ResultSet rs=ps.executeQuery();
			if (rs.next()) {
				loadedRegionsById.put(regionId,
					new Region(
						rs.getInt("minX"),
						rs.getInt("minZ"),
						rs.getInt("maxX"),
						rs.getInt("maxZ")
					)
				);
			} else {
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {ps.close();} catch (Exception e) {};
		}
		return ret;
	}
	
	// !Does not use cache!
	public Region getRegionByPosition(int blockX, int blockZ) {
		if (!Main.globalConfig.dbConnection.isConnected()) {
			Logger.err.println("Database was not connected! Region controller could not init!");
			return null;
		}

		Region ret=null;
		int regionId=0;
		PreparedStatement ps=null;
		try {
			ps=Main.globalConfig.dbConnection.prepareStatement("SELECT * FROM `regions` WHERE `minX`<=? AND `minZ`<=? AND `maxX`>=? AND `maxZ`>=? LIMIT 1");
			ps.setInt(1, blockX);
			ps.setInt(2, blockZ);
			ps.setInt(3, blockX);
			ps.setInt(4, blockZ);
			ResultSet rs=ps.executeQuery();
			if (rs.next()) {
				regionId=rs.getInt("rid");
				ret=new Region(
					rs.getInt("minX"),
					rs.getInt("minZ"),
					rs.getInt("maxX"),
					rs.getInt("maxZ")
				);
			} else {
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {ps.close();} catch (Exception e) {};
		}
		if (ret!=null) loadedRegionsById.put(regionId, ret);
		return ret;
	}
	
	public Region makeNewRegion(int minX, int minZ, int maxX, int maxZ) {
		Region ret=cache.getOrCreate(minX, minZ, maxX, maxZ);
		
		loadedRegionsById.put(nextRegionId, ret);
		
		return ret;
	}
}
