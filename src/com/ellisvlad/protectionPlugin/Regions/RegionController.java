package com.ellisvlad.protectionPlugin.Regions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
				ret=createRegionFromResultSet(rs);
				cache.create(ret, false);
			} else {
				ret=null;
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
				 Region region=createRegionFromResultSet(rs);
				 cache.create(region, false);
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

		regionSaveStatement saveStatement=null;
		try {
			saveStatement=new regionSaveStatement();
			for (Region r:changedRegions) {
				saveStatement.addRegion(r);
			}
			saveStatement.execute();
			changedRegions.clear();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {saveStatement.close();} catch (Exception e) {};
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

	protected Region createRegionFromResultSet(ResultSet rs) throws SQLException {
		Region ret=new Region(
			rs.getInt("rid"),
			rs.getInt("pid"),
			rs.getInt("minX"),
			rs.getInt("minZ"),
			rs.getInt("maxX"),
			rs.getInt("maxZ")
		);
		ret.name=rs.getString("name");
		ret.greeting=rs.getString("greeting");
		ret.farewell=rs.getString("farewell");
		return ret;
	}
	
	private class regionSaveStatement {
		PreparedStatement statement;
		
		regionSaveStatement() throws Exception {
			statement=Main.globalConfig.dbConnection.prepareStatement("UPDATE `regions` SET `pid`=?, `minX`=?, `maxX`=?, `minZ`=?, `maxZ`=? WHERE `rid`=?");
		}

		public void addRegion(Region r) throws SQLException {
			statement.setInt(1, r.ownerPid);
			statement.setInt(2, r.minX);
			statement.setInt(3, r.maxX);
			statement.setInt(4, r.minZ);
			statement.setInt(5, r.maxZ);
			statement.setInt(6, r.regionId);
			statement.addBatch();
		}

		public void close() throws SQLException {
			statement.close();
		}

		public void execute() throws SQLException {
			statement.executeBatch();
		}
	}
}
