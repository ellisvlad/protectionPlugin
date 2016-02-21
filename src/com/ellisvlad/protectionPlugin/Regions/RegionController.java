package com.ellisvlad.protectionPlugin.Regions;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.ellisvlad.protectionPlugin.Logger;
import com.ellisvlad.protectionPlugin.Main;
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions.RegionPermissionsBooleanValue;
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions.RegionPermissionsGroupValue;

public class RegionController {
	
	protected HashMap<Integer, Region> loadedRegionsById=new HashMap<>();
	private HashSet<Region> changedRegions=new HashSet<>();
	private RegionCache cache=new RegionCache(this);
	private int nextRegionId;

	private static String regionInsertStr=null;
	
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
		
		new RegionSaver();
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
	
	public void savePending() {
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
			if (regionInsertStr==null) {
				RegionPermissions perms=new RegionPermissions();
				regionInsertStr="INSERT INTO `regions`(`rid`, `pid`, `minX`, `maxX`, `minZ`, `maxZ`, `name`, `greeting`, `farewell`, ";
				int i=9;
				for (Field f:perms.getClass().getDeclaredFields()) {
					regionInsertStr+="`"+f.getName()+"`,";
					i++;
				}
				regionInsertStr=regionInsertStr.substring(0, regionInsertStr.length()-1);
				regionInsertStr+=") VALUES (";
				for (; i>0; i--) regionInsertStr+="?,";
				regionInsertStr=regionInsertStr.substring(0, regionInsertStr.length()-1);
				regionInsertStr+=")";
			}

			ps=Main.globalConfig.dbConnection.prepareStatement(regionInsertStr);
			int i=1;
			ps.setInt(i++, r.regionId);
			ps.setInt(i++, r.ownerPid);
			ps.setInt(i++, r.minX);
			ps.setInt(i++, r.maxX);
			ps.setInt(i++, r.minZ);
			ps.setInt(i++, r.maxZ);
			ps.setString(i++, r.name);
			ps.setString(i++, r.greeting);
			ps.setString(i++, r.farewell);
			for (Field f:RegionPermissions.class.getDeclaredFields()) {
				if (f.get(r.perms) instanceof RegionPermissionsGroupValue) {
					ps.setString(i++, ((RegionPermissionsGroupValue)f.get(r.perms)).toString());
				}
				if (f.get(r.perms) instanceof RegionPermissionsBooleanValue) {
					ps.setBoolean(i++, ((RegionPermissionsBooleanValue)f.get(r.perms))==RegionPermissionsBooleanValue.True);
				}
			}
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
	
	protected void pendSave(Region r) {
		changedRegions.add(r);
	}
	
	private static class regionSaveStatement {
		PreparedStatement statement;
		static String regionUpdaterStr=null;
		
		regionSaveStatement() throws Exception {
			if (regionUpdaterStr==null) {
				RegionPermissions perms=new RegionPermissions();
				regionUpdaterStr="UPDATE `regions` SET `pid`=?, `minX`=?, `maxX`=?, `minZ`=?, `maxZ`=?, `name`=?, `greeting`=?, `farewell`=?,";
				for (Field f:perms.getClass().getDeclaredFields()) {
					if (f.get(perms) instanceof RegionPermissionsGroupValue) {
						regionUpdaterStr+="`"+f.getName()+"`=?,";
					}
					if (f.get(perms) instanceof RegionPermissionsBooleanValue) {
						regionUpdaterStr+="`"+f.getName()+"`=?,";
					}
				}
				regionUpdaterStr=regionUpdaterStr.substring(0, regionUpdaterStr.length()-1);
				regionUpdaterStr+="WHERE `rid`=?";
			}
			
			statement=Main.globalConfig.dbConnection.prepareStatement(regionUpdaterStr);
		}

		public void addRegion(Region r) throws Exception {
			int i=1;
			statement.setInt(i++, r.ownerPid);
			statement.setInt(i++, r.minX);
			statement.setInt(i++, r.maxX);
			statement.setInt(i++, r.minZ);
			statement.setInt(i++, r.maxZ);
			statement.setString(i++, r.name);
			statement.setString(i++, r.greeting);
			statement.setString(i++, r.farewell);

			for (Field f:RegionPermissions.class.getDeclaredFields()) {
				if (f.get(r.perms) instanceof RegionPermissionsGroupValue) {
					statement.setString(i++, ((RegionPermissionsGroupValue)f.get(r.perms)).toString());
				}
				if (f.get(r.perms) instanceof RegionPermissionsBooleanValue) {
					statement.setBoolean(i++, ((RegionPermissionsBooleanValue)f.get(r.perms))==RegionPermissionsBooleanValue.True);
				}
			}
			
			statement.setInt(i++, r.regionId);
			statement.addBatch();
		}

		public void close() throws SQLException {
			statement.close();
		}

		public void execute() throws SQLException {
			statement.executeBatch();
		}
	}
	
	private class RegionSaver extends Thread {
		
		public RegionSaver() {
			start();
		}
		
		public void run() {
			while (true) {
				try {Thread.sleep(Main.globalConfig.regionSaveInterval);} catch (Exception e) {};
				savePending();
			}
		}
		
	}
}
