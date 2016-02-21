package com.ellisvlad.protectionPlugin.Regions;

public class Region {
	
	int minX, minZ, maxX, maxZ;
	
	protected Region(int minX, int minZ, int maxX, int maxZ) {
		this.minX=minX;
		this.minZ=minZ;
		this.maxX=maxX;
		this.maxZ=maxZ;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Region)) return false;
		Region other=(Region)o;
		if (other.minX==minX && other.minZ==minZ && other.maxX==maxX && other.maxZ==maxZ) return true;
		return false;
	}
	
}
