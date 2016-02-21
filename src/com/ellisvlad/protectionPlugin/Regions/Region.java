package com.ellisvlad.protectionPlugin.Regions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.ellisvlad.protectionPlugin.Main;
import com.ellisvlad.protectionPlugin.config.PlayerConfig;

public class Region {
	
	int regionId, ownerPid;
	Set<Integer> members;
	int minX, minZ, maxX, maxZ;
	
	RegionPermissions perms;
	String name;
	String greeting, farewell;
	
	protected Region(int regionId, int ownerPid, int x1, int z1, int x2, int z2) {
		this.regionId=regionId;
		this.ownerPid=ownerPid;
		this.members=new HashSet<>();
		this.minX=Math.min(x1, x2);
		this.minZ=Math.min(z1, z2);
		this.maxX=Math.max(x1, x2);
		this.maxZ=Math.max(z1, z2);
		this.perms=new RegionPermissions();
		this.name="";
		this.greeting="";
		this.farewell="";
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
		return "{"+regionId+"|"+ownerPid+" : ["+minX+","+minZ+"] -> ["+maxX+","+maxZ+"] : "+getSize()+" : "+members+"}";
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

	public void addMember(int playerId) {
		members.add(playerId);
	}
	public void addMember(Player p) {
		PlayerConfig pConfig=Main.globalConfig.getPlayerConfig(p);
		addMember(pConfig.getPlayerId());
	}

	public void removeMember(int playerId) {
		members.remove(playerId);
	}
	public void removeMember(Player p) {
		PlayerConfig pConfig=Main.globalConfig.getPlayerConfig(p);
		removeMember(pConfig.getPlayerId());
	}

	public boolean hasMember(int playerId) {
		return members.contains(playerId);
	}
	public boolean hasMember(Player p) {
		PlayerConfig pConfig=Main.globalConfig.getPlayerConfig(p);
		return hasMember(pConfig.getPlayerId());
	}

	public void setName(String name) {
		this.name=name;
		Main.globalConfig.regionController.pendSave(this);
	}

	public String getName() {
		return name;
	}
	
}
