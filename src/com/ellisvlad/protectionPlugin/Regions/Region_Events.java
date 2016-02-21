package com.ellisvlad.protectionPlugin.Regions;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;

import com.ellisvlad.protectionPlugin.Main;

public class Region_Events implements Listener {
	@EventHandler
	public void onPassthrough(PlayerMoveEvent e) {
		if (e.getFrom().getBlockX()==e.getTo().getBlockX() && e.getFrom().getBlockZ()==e.getTo().getBlockZ()) return;

		if (Region.anyPermissionFailures(e.getTo().getBlockX(), e.getTo().getBlockZ(), e.getPlayer(), "passThrough")) {
			e.getPlayer().sendMessage(Main.globalConfig.passThroughDenied);
			e.getPlayer().teleport(e.getTo().clone().zero().add(
				Math.round(e.getFrom().getX()-0.5)+0.5,
				Math.round(e.getFrom().getY()),
				Math.round(e.getFrom().getZ()-0.5)+0.5
			));
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if (Region.anyPermissionFailures(e.getBlock().getX(), e.getBlock().getZ(), e.getPlayer(), "allowBlockPlace")) {
			e.getPlayer().sendMessage(Main.globalConfig.blockPlaceDenied);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (Region.anyPermissionFailures(e.getBlock().getX(), e.getBlock().getZ(), e.getPlayer(), "allowBlockBreak")) {
			e.getPlayer().sendMessage(Main.globalConfig.blockBreakDenied);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPvp(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player) ||
			((e.getDamager() instanceof Arrow) && !((((Arrow)e.getDamager()).getShooter()) instanceof Player))) return;
		if (!(e.getEntity() instanceof Player)) return;
		
		if (Region.anyPermissionFailures(
				e.getEntity().getLocation().getBlockX(),
				e.getEntity().getLocation().getBlockZ(),
				(Player) e.getEntity(),
				"allowPvp")) {
			if (e.getDamager() instanceof Player) e.getDamager().sendMessage(Main.globalConfig.pvpDenied);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onMobDamage(EntityDamageByEntityEvent e) {
		if ((e.getDamager() instanceof Player) ||
			((e.getDamager() instanceof Arrow) && ((((Arrow)e.getDamager()).getShooter()) instanceof Player))) return;
		if (!(e.getEntity() instanceof Player)) return;
		
		if (Region.anyPermissionFailures(
				e.getEntity().getLocation().getBlockX(),
				e.getEntity().getLocation().getBlockZ(),
				(Player) e.getEntity(),
				"allowMobDamage")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onXpDrop(EntitySpawnEvent e) {
		if (e.getEntityType()!=EntityType.EXPERIENCE_ORB) return;

		if (Region.anyPermissionFailures(e.getLocation().getBlockX(), e.getLocation().getBlockZ(), null, "allowXpDrops")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		if (Region.anyPermissionFailures(
				e.getPlayer().getLocation().getBlockX(),
				e.getPlayer().getLocation().getBlockZ(),
				e.getPlayer(),
				"allowItemDrops")) {
			e.getPlayer().sendMessage(Main.globalConfig.itemDropDenied);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onHostileSpawn(EntitySpawnEvent e) {
		if (!(e.getEntity() instanceof Monster)) return;

		if (Region.anyPermissionFailures(
				e.getLocation().getBlockX(),
				e.getLocation().getBlockZ(),
				null,
				"allowHostileMobSpawns")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFriendlyMobsSpawns(EntitySpawnEvent e) {
		if (!(e.getEntity() instanceof Creature) || (e.getEntity() instanceof Monster)) return;

		if (Region.anyPermissionFailures(
				e.getLocation().getBlockX(),
				e.getLocation().getBlockZ(),
				null,
				"allowFriendlyMobsSpawns")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCreeperExplosions(EntityExplodeEvent e) {
		if (e.getEntityType()!=EntityType.CREEPER) return;
		
		if (Region.anyPermissionFailures(
				e.getEntity().getLocation().getBlockX(),
				e.getEntity().getLocation().getBlockZ(),
				null,
				"allowCreeperExplosions")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onOtherExplosions(EntityExplodeEvent e) {
		if (Region.anyPermissionFailures(
				e.getEntity().getLocation().getBlockX(),
				e.getEntity().getLocation().getBlockZ(),
				null,
				"allowOtherExplosions")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEndermanGrief(EntityChangeBlockEvent e) {
		if (!(e.getEntity() instanceof Enderman)) return;
		
		if (Region.anyPermissionFailures(
				e.getEntity().getLocation().getBlockX(),
				e.getEntity().getLocation().getBlockZ(),
				null,
				"allowEndermanGrief")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTeleportIn(EntityTeleportEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
			
		if (Region.anyPermissionFailures(
				e.getTo().getBlockX(),
				e.getTo().getBlockZ(),
				(Player) e.getEntity(),
				"allowPlayerTeleportIn")) {
			((Player)e.getEntity()).sendMessage(Main.globalConfig.teleportInDenied);
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTeleportOut(EntityTeleportEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
			
		if (Region.anyPermissionFailures(
				e.getFrom().getBlockX(),
				e.getFrom().getBlockZ(),
				(Player) e.getEntity(),
				"allowPlayerTeleportOut")) {
			((Player)e.getEntity()).sendMessage(Main.globalConfig.teleportOutDenied);
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onLightBlock(BlockIgniteEvent e) {
		if (e.getCause()!=IgniteCause.FLINT_AND_STEEL) return;
			
		if (Region.anyPermissionFailures(
				e.getBlock().getX(),
				e.getBlock().getZ(),
				e.getPlayer(),
				"allowLighter")) {
			e.getPlayer().sendMessage(Main.globalConfig.lighterDenied);
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFireSpread(BlockIgniteEvent e) {
		if (e.getCause()!=IgniteCause.SPREAD) return;
			
		if (Region.anyPermissionFailures(
				e.getBlock().getX(),
				e.getBlock().getZ(),
				null,
				"allowFireSpread")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onLightning(LightningStrikeEvent e) {
		if (Region.anyPermissionFailures(
				e.getLightning().getLocation().getBlockX(),
				e.getLightning().getLocation().getBlockZ(),
				null,
				"allowLightning")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onOpenChest(InventoryOpenEvent e) {
		if (e.getInventory() instanceof PlayerInventory) return;
		if (e.getInventory().getHolder()==null) return;
			
		if (Region.anyPermissionFailures(
				((BlockState)e.getInventory().getHolder()).getX(),
				((BlockState)e.getInventory().getHolder()).getZ(),
				(Player)e.getPlayer(),
				"allowChestAccess")) {
			e.getPlayer().sendMessage(Main.globalConfig.chestAccessDenied);
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onLiquidFlow(BlockFromToEvent e) {
		if (e.getBlock().getType()!=Material.WATER && e.getBlock().getType()!=Material.STATIONARY_WATER &&
			e.getBlock().getType()!=Material.LAVA && e.getBlock().getType()!=Material.STATIONARY_LAVA) return;
		
		if (Region.anyPermissionFailures(
				e.getToBlock().getLocation().getBlockX(),
				e.getToBlock().getLocation().getBlockZ(),
				null,
				"allowLiquidFlow")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockUse(PlayerInteractEvent e) {
		if (!e.hasBlock()) return;
		if (e.getClickedBlock().getState() instanceof InventoryHolder) return;
			
		if (Region.anyPermissionFailures(
				e.getClickedBlock().getX(),
				e.getClickedBlock().getZ(),
				e.getPlayer(),
				"allowBlockUse")) {
			e.getPlayer().sendMessage(Main.globalConfig.blockUseDenied);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehiclePlace(VehicleCreateEvent e) {
		if (Region.anyPermissionFailures(
				e.getVehicle().getLocation().getBlockX(),
				e.getVehicle().getLocation().getBlockZ(),
				null,
				"allowVehiclePlace")) {
			e.getVehicle().remove();
		}
	}
	
	@EventHandler
	public void onVehicleBreak(VehicleDestroyEvent e) {
		if (Region.anyPermissionFailures(
				e.getVehicle().getLocation().getBlockX(),
				e.getVehicle().getLocation().getBlockZ(),
				null,
				"allowVehicleBreak")) {
			if (e.getAttacker() instanceof Player) e.getAttacker().sendMessage(Main.globalConfig.vehicleBreakDenied);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onLeafDecay(LeavesDecayEvent e) {
		if (Region.anyPermissionFailures(
				e.getBlock().getX(),
				e.getBlock().getZ(),
				null,
				"allowVehiclePlace")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onGrassGrowth(BlockFromToEvent e) {
		if (e.getBlock().getType()!=Material.GRASS) return;
		
		if (Region.anyPermissionFailures(
				e.getBlock().getX(),
				e.getBlock().getZ(),
				null,
				"allowGrassGrowth")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (Region.anyPermissionFailures(
				e.getEntity().getLocation().getBlockX(),
				e.getEntity().getLocation().getBlockZ(),
				null,
				"allowDamage")) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if (Region.anyPermissionFailures(
				e.getPlayer().getLocation().getBlockX(),
				e.getPlayer().getLocation().getBlockZ(),
				e.getPlayer(),
				"canChat")) {
			e.getPlayer().sendMessage(Main.globalConfig.chatDenied);
			e.setCancelled(true);
		}
	}
	
}
