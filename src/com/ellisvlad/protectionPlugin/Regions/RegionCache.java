package com.ellisvlad.protectionPlugin.Regions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegionCache {
	
	private static final int cacheDepth=3; //Depth of cache, splitting the world into 2^cacheDepth chunks.
	
	private RegionController regionController;
	private cacheSubMap cacheMap=new cacheSubMap();
	
	public RegionCache(RegionController regionController) {
		this.regionController=regionController;
	}
	
	class cacheSubMap {
		private Map<Long, cacheSubMap> subMaps=new HashMap<>();
		private Set<Region> regions=new HashSet<>();
		
		private cacheSubMap getSubMap(int shiftedX, int shiftedZ) {
			Long mapKey=(((long)shiftedX & 0xFFFFFFFF)<<32) | ((long)shiftedZ & 0xFFFFFFFF);
			cacheSubMap ret=subMaps.get(mapKey);
			if (ret==null) {
				ret=new cacheSubMap();
				subMaps.put(mapKey, ret);
			}
			return ret;
		}

		public Set<Region> getRegions() {
			return regions;
		}

		public void addRegion(Region r) {
			regions.add(r);
		}
	}

	public Set<Region> getRegionsAt(int x, int z) {
		cacheSubMap subMap=cacheMap;
		int cacheX=x, cacheZ=z;
		for (int i=0; i<cacheDepth; i++) {
			cacheX>>=3;
			cacheZ>>=3;
			subMap=subMap.getSubMap(cacheX, cacheZ);
		}
		Set<Region> ret=new HashSet<>();
		for (Region region:subMap.getRegions()) {
			if (region.contains(x, z)) ret.add(region);
		}
		return ret;
	}
	
	public Region get(int minX, int minZ, int maxX, int maxZ) {
		Region r=new Region(-1, -1, minX, minZ, maxX, maxZ);
		
		cacheSubMap subMap=cacheMap;
		int cacheX=r.minX, cacheZ=r.minZ;
		for (int i=0; i<cacheDepth; i++) {
			cacheX>>=3;
			cacheZ>>=3;
			subMap=subMap.getSubMap(cacheX, cacheZ);
		}
		for (Region region:subMap.regions) {
			if (region.equals(r)) return region;
		}
		return null;
	}
	
	public Region create(int regionId, int ownerId, int minX, int minZ, int maxX, int maxZ) {
		return create(regionId, ownerId, minX, minZ, maxX, maxZ, false);
	}

	public Region create(int regionId, int ownerId, int minX, int minZ, int maxX, int maxZ, boolean doNotInsert) {
		// Method could be optimised
		Region r=new Region(regionId, ownerId, minX, minZ, maxX, maxZ);
		
		for (int x=r.minX>>3; x<=r.maxX>>3; x++) {
			for (int z=r.minZ>>3; z<=r.maxZ>>3; z++) {
				cacheSubMap subMap=cacheMap;
				int cacheX=x, cacheZ=z;
				for (int i=0; i<cacheDepth; i++) {
					subMap=subMap.getSubMap(cacheX, cacheZ);
					cacheX>>=3;
					cacheZ>>=3;
				}
				subMap.addRegion(r);
			}
		}
		regionController.loadedRegionsById.put(regionId, r);
		if (!doNotInsert) regionController.insert(r);
		
		return r;
	}
	
}


