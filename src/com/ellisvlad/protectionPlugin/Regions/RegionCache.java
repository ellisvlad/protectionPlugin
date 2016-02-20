package com.ellisvlad.protectionPlugin.Regions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeRangeSet;

public class RegionCache {
	
	public List<Map<Integer, Region>> sortedMaps=new ArrayList<Map<Integer, Region>>();
	
	public RegionCache() {
		sortedMaps.add(new TreeMap<Integer, Region>());
		sortedMaps.add(new TreeMap<Integer, Region>());
		sortedMaps.add(new TreeMap<Integer, Region>());
		sortedMaps.add(new TreeMap<Integer, Region>());
	}
	
	public void addEntry(int minX, int maxX, int minZ, int maxZ) {
		Region region=new Region(minX, minZ, maxX, maxZ);
		sortedMaps.get(0).put(minX, region);
		sortedMaps.get(1).put(maxX, region);
		sortedMaps.get(2).put(minZ, region);
		sortedMaps.get(3).put(maxZ, region);
	}
	
	private int binarySearch(Integer[] set, int value) {
		int iLow=0;
        int iHigh=set.length-1;
        int iMid=0;
        while (iLow<=iHigh) {
        	iMid=(iHigh+iLow)/2;
            if (value < set[iMid]) iHigh=iMid-1; else
            if (value > set[iMid]) iLow =iMid+1; else return iMid;
        }
		return iLow;
	}

	public int getRegionsAt(int x) {
		Integer[][] xSets=new Integer[][] {
			sortedMaps.get(0).keySet().toArray(new Integer[sortedMaps.get(0).size()]),
			sortedMaps.get(1).keySet().toArray(new Integer[sortedMaps.get(1).size()])
		};
		int[] indexLimits=new int[]{
			binarySearch(xSets[0], x),
			binarySearch(xSets[1], x)
		};
		if (indexLimits[0] < xSets[1].length-indexLimits[1]) { //Work with mins
			for (int i=0; i!=indexLimits[workingIndex])	{
				
			}
		} else { //Work with maxs
			
		}
		return -1;
	}
	
}
