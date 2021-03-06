package com.ellisvlad.protectionPlugin.Regions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import com.ellisvlad.protectionPlugin.Main;
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions.RegionPermissionsBooleanValue;
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions.RegionPermissionsGroupValue;
import com.ellisvlad.protectionPlugin.Regions.RegionPermissions.RegionPermissionsValue;
import com.ellisvlad.protectionPlugin.config.PlayerConfig;

public class Region {
	
	int regionId, ownerPid;
	Set<Integer> members;
	int minX, minZ, maxX, maxZ;

	String name;
	RegionPermissions perms;
	
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

	public int getId() {
		return regionId;
	}

	public int getSize() {
		int size=0;
		size+=maxX - minX+1;
		size*=maxZ - minZ+1;
		return size;
	}

	public RegionPermissions getPermissions() {
		return perms;
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

	public static boolean allows(Region region, RegionPermissionsValue permission, PlayerConfig player) {
		if (permission instanceof RegionPermissionsBooleanValue) return ((RegionPermissionsBooleanValue)permission)==RegionPermissionsBooleanValue.True;
		
		if (permission instanceof RegionPermissionsGroupValue) {
			RegionPermissionsGroupValue perm=(RegionPermissionsGroupValue)permission;
			switch (perm) {
			case All:
				return true;
			case Members:
				return region.ownerPid==player.getPlayerId() || region.hasMember(player.getPlayerId());
			case Owner:
				return region.ownerPid==player.getPlayerId();
			case None:
				return false;
			}
		}
		
		return false;
	}
	
	public static boolean anyPermissionFailures(int x, int z, Player p, String permissionName) {
		try {
			PlayerConfig pc=Main.globalConfig.getPlayerConfig(p);
			for (Region region:Main.globalConfig.regionController.getRegionsByPosition(x, z)) {
				if (!Region.allows(region, (RegionPermissionsValue)RegionPermissions.class.getField(permissionName).get(region.perms), pc)) {
					return true;
				}
			}
		} catch (Exception e) {};
		return false;
	}

	public String getLocation() {
		return "["+minX+","+minZ+"]->["+maxX+","+maxZ+"]";
	}

	public String listMembers() {
		if (members.size()==0) return "No members!";
		
		String ret="";
		Map<Integer, PlayerConfig> pConfigs=Main.globalConfig.getCachedPlayerListById();
		for (int i:members) {
			ret+=pConfigs.get(i).getName()+", ";
		}
		ret=ret.substring(0, ret.length()-2);
		
		return ret;
	}
	
}
