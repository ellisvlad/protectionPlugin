package com.ellisvlad.protectionPlugin.Regions;

import java.util.Arrays;
import java.util.LinkedList;

public class RegionCache {
	
	public CacheTreeNode cacheLookupMap[]=new CacheTreeNode[4];

	private class CacheEntry {
		int regionCoordKey[];
		
		CacheEntry(int minX, int maxX, int minZ, int maxZ) {
			this.regionCoordKey=new int[]{minX,maxX, minZ,maxZ};
		}
		
		@Override
		public String toString() {
			return "{"+regionCoordKey[0]+","+regionCoordKey[1]+"}";
		}
	}
	
	private class CacheTreeNode {
		CacheEntry value;
		CacheTreeNode branches[]=new CacheTreeNode[2], parent;
		
		CacheTreeNode(CacheEntry value, CacheTreeNode left, CacheTreeNode right, CacheTreeNode parent) {
			this.value=value;
			this.branches[0]=left;
			this.branches[1]=right;
		}
		
		@Override
		public String toString() {
			CacheEntry[] ordered=traverse(this);
			String ret="";
			for (CacheEntry e:ordered) ret+=e;
			return ret;
		}
	}

	private CacheTreeNode insert(int mapId, CacheEntry value) {
		CacheTreeNode treeEl=cacheLookupMap[mapId];
		if (treeEl==null) return new CacheTreeNode(value, null, null, null);
		
		while (true) {
			int branchId=(value.regionCoordKey[mapId] > treeEl.value.regionCoordKey[mapId] ? 1 : 0);
			if (treeEl.branches[branchId]==null) {
				treeEl.branches[branchId]=new CacheTreeNode(value, null, null, treeEl);
				return cacheLookupMap[mapId];
			}
			treeEl=treeEl.branches[branchId];
		}
	}
	
	public void addEntry(int minX, int minZ, int maxX, int maxZ) {
		for (int mapId=0; mapId!=4; mapId++) {
			cacheLookupMap[mapId]=insert(mapId, new CacheEntry(minX, minZ, maxX, maxZ));
		}
	}
	
	public void searchX(int val) {
		CacheTreeNode tree=cacheLookupMap[0];
		while (tree.value.regionCoordKey[0] > val) {
			if (tree.branches[0]!=null) tree=tree.branches[0]; else ;//TODO
		}
	}

	private CacheEntry[] traverse(CacheTreeNode tree) {
		if (tree==null) return new CacheEntry[0];
		
		LinkedList<CacheEntry> ret=new LinkedList<>();
		ret.addAll(Arrays.asList(traverse(tree.branches[0])));
		ret.add(tree.value);
		ret.addAll(Arrays.asList(traverse(tree.branches[1])));
		return ret.toArray(new CacheEntry[ret.size()]);
	}
	
	private CacheTreeNode buildTree(CacheEntry[] orderedEntries, int iLow, int iHigh) {
		if (iLow==iHigh) return null;
		
		return new CacheTreeNode(
			orderedEntries[(iLow+iHigh)/2],
			buildTree(
				orderedEntries,
				iLow,
				(iLow+iHigh)/2
			), buildTree(
				orderedEntries,
				(iLow+iHigh)/2+1,
				iHigh
			),
			null);
	}
	
	public void pack() {
		for (int mapId=0; mapId!=4; mapId++) {
			CacheEntry[] ordered=traverse(cacheLookupMap[mapId]);
			cacheLookupMap[mapId]=buildTree(ordered, 0, ordered.length);
		}
	}

}
