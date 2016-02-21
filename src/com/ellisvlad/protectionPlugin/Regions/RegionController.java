package com.ellisvlad.protectionPlugin.Regions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.ellisvlad.protectionPlugin.Logger;
import com.ellisvlad.protectionPlugin.Main;

public class RegionController {
	
	protected HashMap<Integer, Region> loadedRegionsById=new HashMap<>();
	private HashSet<Region> changedRegions=new HashSet<>();
	private RegionCache cache=new RegionCache(this);
	private int nextRegionId;
	
	public RegionController() {
		Logger.out.println("Loading regions...");
		if (!Main.globalConfig.dbConnection.isConnected()) {
			Logger.err.println("Database was not connected! Region controller could not init!");
			return;
		}
		try {
			PreparedStatement ps=Main.globalConfig.dbConnection.prepareStatement("SHOW TABLE STATUS LIKE \"regions\"");
			ResultSet rs=ps.executeQuery();
			rs.next();
			nextRegionId=rs.getInt("Auto_increment");
			ps.close();
		} catch (Exception e) {
			Logger.err.println("Region controller could not init! Could not get next region ID!");
			e.printStackTrace();
			return;
		}
		loadAll();
		Logger.out.println(loadedRegionsById.size()+" regions loaded!");
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
						regionId,
						rs.getInt("pid"),
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
	
	public Set<Region> getRegionsByPosition(int blockX, int blockZ) {
		return cache.getRegionsAt(blockX, blockZ);
	}
	
	private void loadAll() {
		if (!Main.globalConfig.dbConnection.isConnected()) {
			Logger.err.println("Database was not connected! Region controller could not load!");
			return;
		}

		PreparedStatement ps=null;
		try {
			ps=Main.globalConfig.dbConnection.prepareStatement("SELECT * FROM `regions`");
			ResultSet rs=ps.executeQuery();
			while (rs.next()) {
				cache.create(
					rs.getInt("rid"),
					rs.getInt("pid"),
					rs.getInt("minX"),
					rs.getInt("minZ"),
					rs.getInt("maxX"),
					rs.getInt("maxZ"),
					true
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {ps.close();} catch (Exception e) {};
		}
	}
	
	protected void savePending() {
		if (!Main.globalConfig.dbConnection.isConnected()) {
			Logger.err.println("Database was not connected! Region controller could not save!");
			return;
		}

		PreparedStatement ps=null;
		try {
			ps=Main.globalConfig.dbConnection.prepareStatement("UPDATE `regions` SET `pid`=?, `minX`=?, `maxX`=?, `minZ`=?, `maxZ`=? WHERE `rid`=?");
			for (Region r:changedRegions) {
				ps.setInt(1, r.ownerPid);
				ps.setInt(2, r.minX);
				ps.setInt(3, r.maxX);
				ps.setInt(4, r.minZ);
				ps.setInt(5, r.maxZ);
				ps.setInt(6, r.regionId);
				ps.addBatch();
			}
			ps.executeBatch();
			changedRegions.clear();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {ps.close();} catch (Exception e) {};
		}
	}
	
	protected void insert(Region r) {
		if (!Main.globalConfig.dbConnection.isConnected()) {
			Logger.err.println("Database was not connected! Region controller could not save!");
			return;
		}

		PreparedStatement ps=null;
		try {
			ps=Main.globalConfig.dbConnection.prepareStatement("INSERT INTO `regions`(`rid`, `pid`, `minX`, `maxX`, `minZ`, `maxZ`) VALUES (?, ?, ?, ?, ?, ?)");
			ps.setInt(1, r.regionId);
			ps.setInt(2, r.ownerPid);
			ps.setInt(3, r.minX);
			ps.setInt(4, r.maxX);
			ps.setInt(5, r.minZ);
			ps.setInt(6, r.maxZ);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {ps.close();} catch (Exception e) {};
		}
	}
	
	public Region makeNewRegion(int ownerId, int minX, int minZ, int maxX, int maxZ) {
		Region r=cache.get(minX, minZ, maxX, maxZ);
		if (r!=null) return null;
		return cache.create(getNextId(), ownerId, minX, minZ, maxX, maxZ);
	}
	
	private int getNextId() {
		return nextRegionId++;
	}
}
