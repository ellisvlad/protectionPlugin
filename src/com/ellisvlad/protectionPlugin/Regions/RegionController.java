package com.ellisvlad.protectionPlugin.Regions;

import java.util.HashMap;
import java.util.HashSet;

import com.ellisvlad.protectionPlugin.Logger;
import com.ellisvlad.protectionPlugin.Main;

public class RegionController {
	
	private HashMap<Integer, Region> loadedRegions=new HashMap<>();
	private HashSet<Region> changedRegions=new HashSet<>();
	
	public RegionController() {
		if (!Main.globalConfig.dbConnection.isConnected()) {
			Logger.err.println("Database was not connected! Region controller could not init!");
			return;
		}
	}
	
	public Region getRegionById(int regionId) {
		return null;
	}
	
	public Region getRegionByPosition(int blockX, int blockZ) {
		return null;
	}
	
}
