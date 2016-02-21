package com.ellisvlad.protectionPlugin.Regions;

public class RegionPermissions {
	
	public RegionPermissionsValue
		passThrough				=RegionPermissionsGroupValue.All,
		allowBlockPlace			=RegionPermissionsGroupValue.Members,
		allowBlockBreak			=RegionPermissionsGroupValue.Members,
		allowPvp				=RegionPermissionsBooleanValue.True,
		allowMobDamage			=RegionPermissionsBooleanValue.True,
		allowXpDrops			=RegionPermissionsBooleanValue.True,
		allowItemDrops			=RegionPermissionsGroupValue.All,
		allowHostileMobSpawns	=RegionPermissionsBooleanValue.True,
		allowFriendlyMobsSpawns	=RegionPermissionsBooleanValue.True,
		allowCreeperExplosions	=RegionPermissionsBooleanValue.True,
		allowOtherExplosions	=RegionPermissionsBooleanValue.True,
		allowEndermanGrief		=RegionPermissionsBooleanValue.True,
		allowTeleportIn			=RegionPermissionsGroupValue.All,
		allowTeleportOut		=RegionPermissionsGroupValue.All,
		allowTnt				=RegionPermissionsGroupValue.All,
		allowLighter			=RegionPermissionsGroupValue.All,
		allowFireSpread			=RegionPermissionsBooleanValue.True,
		allowLightning			=RegionPermissionsBooleanValue.True,
		allowChestAccess		=RegionPermissionsGroupValue.All,
		allowLiquidFlow			=RegionPermissionsBooleanValue.True,
		allowBlockUse			=RegionPermissionsGroupValue.All,
		allowVehiclePlace		=RegionPermissionsGroupValue.All,
		allowVehicleBreak		=RegionPermissionsGroupValue.All,
		allowSnowFall			=RegionPermissionsBooleanValue.True,
		allowSnowMelt			=RegionPermissionsBooleanValue.True,
		allowIceForm			=RegionPermissionsBooleanValue.True,
		allowIceMelt			=RegionPermissionsBooleanValue.True,
		allowLeafDecay			=RegionPermissionsBooleanValue.True,
		allowGrassGrowth		=RegionPermissionsBooleanValue.True,
		disableDamage			=RegionPermissionsGroupValue.None,
		canChat					=RegionPermissionsGroupValue.All
	;

	public interface RegionPermissionsValue {
	}
	public enum RegionPermissionsGroupValue implements RegionPermissionsValue {
		None, Owner, Members, All
	}
	public enum RegionPermissionsBooleanValue implements RegionPermissionsValue {
		True, False
	}
	
}
