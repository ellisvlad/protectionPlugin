package com.ellisvlad.protectionPlugin.ToolItem;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.ellisvlad.protectionPlugin.Main;
import com.ellisvlad.protectionPlugin.Utils;
import com.ellisvlad.protectionPlugin.config.PlayerConfig;

public class TI_EventListener implements Listener {
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (e.getTo().getBlockY()==e.getFrom().getBlockY()) return;
		PlayerConfig pConfig=Main.globalConfig.getPlayerConfig(e.getPlayer());
		
		if (pConfig.getFirstPoint()!=null) {
			
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		if (!TI_Handler.isToolItem(e.getItemDrop().getItemStack())) return;
		net.minecraft.server.v1_8_R3.ItemStack nis=Utils.getItemStackHandle(e.getItemDrop().getItemStack());
		TI_Handler.renderToolItem(nis);
		
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		ItemStack is;
		if (e.hasItem())
			is=e.getItem();
		else
			is=e.getPlayer().getItemInHand();
		if (!TI_Handler.isToolItem(is)) return;
		net.minecraft.server.v1_8_R3.ItemStack nis=Utils.getItemStackHandle(is);
		PlayerConfig pConfig=Main.globalConfig.getPlayerConfig(e.getPlayer());
		TI_Handler.renderToolItem(nis);
		
		e.setCancelled(true);

		if (e.getAction()==Action.LEFT_CLICK_BLOCK) {
			Location pos=e.getClickedBlock().getLocation();
			TI_BeaconManager.onLeftClick(pos, e.getPlayer(), pConfig);
		}
		if (e.getAction()==Action.RIGHT_CLICK_BLOCK || e.getAction()==Action.RIGHT_CLICK_AIR) {
			Location pos=(e.hasBlock()?e.getClickedBlock().getLocation():null);
			TI_BeaconManager.onRightClick(pos, e.getPlayer(), pConfig);
		}
	}
		
	}