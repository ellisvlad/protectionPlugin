package com.ellisvlad.protectionPlugin.Regions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class RegionCache {
	
	private static final int cacheDepth=3; //Depth of cache, splitting the world into 2^cacheDepth chunks
	
	cacheSubMap cacheMap=new cacheSubMap();
	
	class cacheSubMap {
		Map<Long, cacheSubMap> subMaps=new HashMap<>();
		Set<Region> regions=new HashSet<>();
		
		private cacheSubMap getSubMap(int shiftedX, int shiftedZ) {
			Long mapKey=(((long)shiftedX & 0xFFFFFFFF)<<32) | ((long)shiftedZ & 0xFFFFFFFF);
			System.out.println(mapKey);
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
	}

	public Region getOrCreate(int minX, int minZ, int maxX, int maxZ) {
		Region r=new Region(minX, minZ, maxX, maxZ);
		
		cacheSubMap subMap=cacheMap;
		int cacheX=r.minX, cacheZ=r.minZ;
		for (int i=0; i<cacheDepth; i++) {
			cacheX<<=3;
			cacheZ<<=3;
			subMap=subMap.getSubMap(cacheX, cacheZ);
		}
		for (Region region:subMap.regions) {
			if (region.equals(r)) return region;
		}
		
//		for (int x=r.minX; x!=r.maxX; x++) {
//			for (int z=r.minZ; z!=r.maxZ; z++) {
//			}
//		}
		
		return r;
	}
	
}
