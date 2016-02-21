package com.ellisvlad.protectionPlugin.ToolItem;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.ellisvlad.protectionPlugin.Main;
import com.ellisvlad.protectionPlugin.Utils;
import com.ellisvlad.protectionPlugin.Utils.delayedPromptTextResponseHandler;
import com.ellisvlad.protectionPlugin.Regions.Region;
import com.ellisvlad.protectionPlugin.ToolItem.gui.TI_Gui_Main;
import com.ellisvlad.protectionPlugin.config.PlayerConfig;

public class TI_BeaconManager {

	public static void onLeftClick(Location pos, Player player, PlayerConfig pConfig) {
		if (pConfig.getFirstPoint()==null) { //First Pos
			Utils.sendBeaconLine(pos, player, DyeColor.ORANGE);
			pConfig.setFirstPoint(pos);
			Utils.sendMessageNewLines(player, Main.globalConfig.first_point_selected);
		} else
		if (pConfig.getSecondPoint()==null) { //Second Pos
			Utils.sendBeaconLine(pos, player, DyeColor.BLUE);
			pConfig.setSecondPoint(pos);
			Utils.sendMessageNewLines(player, Main.globalConfig.second_point_selected);
		} else { //Create region or Clear both
			Utils.clearBeaconLine(pConfig.getFirstPoint(), player);
			Utils.clearBeaconLine(pConfig.getSecondPoint(), player);

			if (player.isSneaking()) {
				Region region=Main.globalConfig.regionController.makeNewRegion(
					pConfig.getPlayerId(),
					pConfig.getFirstPoint().getBlockX(),
					pConfig.getFirstPoint().getBlockZ(),
					pConfig.getSecondPoint().getBlockX(),
					pConfig.getSecondPoint().getBlockZ()
				);
				
				if (region==null) {
					Utils.sendMessageNewLines(player,
						Main.globalConfig.already_is_region
							.replace("{pos1}", "["+pConfig.getFirstPoint().getBlockX()+","+pConfig.getFirstPoint().getBlockZ()+"]")
							.replace("{pos2}", "["+pConfig.getSecondPoint().getBlockX()+","+pConfig.getSecondPoint().getBlockZ()+"]")
					);
				} else {
					Utils.delayedPromptText(player.getLocation(), player, new String[]{"Give your", "region a", "name:", "Unnamed_Region"}, new delayedPromptTextResponseHandler() {
						@Override
						public void handle(String[] lines, Player player) {
							PlayerConfig pConfig=Main.globalConfig.getPlayerConfig(player);
							Region region=Region.makeUnownedRegion(
								pConfig.getFirstPoint().getBlockX(),
								pConfig.getFirstPoint().getBlockZ(),
								pConfig.getSecondPoint().getBlockX(),
								pConfig.getSecondPoint().getBlockZ()
							);

							for (Region r:Main.globalConfig.regionController.getRegionsByPosition(pConfig.getFirstPoint().getBlockX(), pConfig.getFirstPoint().getBlockZ())) {
								if (region.equals(r)) {
									region=r;
									break;
								}
							}
							
							region.setName(lines[3]);
							
							Utils.sendMessageNewLines(player,
								Main.globalConfig.created_region
									.replace("{pos1}", "["+pConfig.getFirstPoint().getBlockX()+","+pConfig.getFirstPoint().getBlockZ()+"]")
									.replace("{pos2}", "["+pConfig.getSecondPoint().getBlockX()+","+pConfig.getSecondPoint().getBlockZ()+"]")
									.replace("{size}", ""+region.getSize())
									.replace("{name}", ""+region.getName())
							);

							pConfig.setSecondPoint(null);
							pConfig.setSecondPoint(null);
						}
					});
				}
			} else { //Clear
				Utils.sendMessageNewLines(player, Main.globalConfig.cleared_selection);

				pConfig.setSecondPoint(null);
				pConfig.setSecondPoint(null);
			}
		}
	}

	public static void onRightClick(Location pos, Player player, PlayerConfig pConfig) {
		if (player.isSneaking()) { //Gui
			new TI_Gui_Main().show(player);
		} else { //Push boundry
			for (int i=0; i<2; i++) {
				if (pConfig.getPoints()[i]==null) continue;

				List<Pair<Block, BlockFace>> blocks=Utils.getDirectionsInRay(player.getLineOfSight((Set<Material>)null, 3));
				blocks=Utils.filterBlockDirListToXY(blocks, pConfig.getPoints()[i].getBlockX(), pConfig.getPoints()[i].getBlockZ());
				if (blocks.size()!=0) {
					new Utils.delayedBeaconMove(pConfig.getPoints()[i], player, blocks.get(0).getRight());
					pConfig.getPoints()[i]=pConfig.getPoints()[i].getBlock().getRelative(blocks.get(0).getRight()).getLocation();
				}
			}
		}
	}
}
