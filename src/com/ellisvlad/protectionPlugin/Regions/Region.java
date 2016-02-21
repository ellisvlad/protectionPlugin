package com.ellisvlad.protectionPlugin.Regions;

public class Region {
	
	int regionId, ownerPid;
	int minX, minZ, maxX, maxZ;
	
	protected Region(int regionId, int ownerPid, int x1, int z1, int x2, int z2) {
		this.regionId=regionId;
		this.ownerPid=ownerPid;
		this.minX=Math.min(x1, x2);
		this.minZ=Math.min(z1, z2);
		this.maxX=Math.max(x1, x2);
		this.maxZ=Math.max(z1, z2);
	}
	
	public static Region makeUnownedRegion(int x1, int z1, int x2, int z2) {
		return new Region(-1, -1, x1, z1, x2, z2); 
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Region)) return false;
		Region other=(Region)o;
		if (regionId!=-1 && other.regionId==regionId) return true;
		return (other.minX==minX && other.minZ==minZ && other.maxX==maxX && other.maxZ==maxZ);
	}
	
	@Override
	public String toString() {
		return "{"+regionId+"|"+ownerPid+" : ["+minX+","+minZ+"] -> ["+maxX+","+maxZ+"] : "+getSize()+"}";
	}
	
	public boolean contains(int x, int z) {
		return (x>=minX && x<=maxX && z>=minZ && z<=maxZ);
	}

	public int getSize() {
		int size=0;
		size+=maxX - minX+1;
		size*=maxZ - minZ+1;
		return size;
	}
	
}
