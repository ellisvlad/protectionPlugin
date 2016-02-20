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
import com.ellisvlad.protectionPlugin.ToolItem.gui.TI_Gui_Main;
import com.ellisvlad.protectionPlugin.config.PlayerConfig;

public class TI_BeaconManager {

	public static void onLeftClick(Location pos, Player player, PlayerConfig pConfig) {
		if (pConfig.points[0]==null) { //First Pos
			Utils.sendBeaconLine(pos, player, DyeColor.ORANGE);
			pConfig.points[0]=pos;
			Utils.sendMessageNewLines(player, Main.globalConfig.first_point_selected);
		} else
		if (pConfig.points[1]==null) { //Second Pos
			Utils.sendBeaconLine(pos, player, DyeColor.BLUE);
			pConfig.points[1]=pos;
			Utils.sendMessageNewLines(player, Main.globalConfig.second_point_selected);
		} else { //Create region or Clear both
			Utils.clearBeaconLine(pConfig.points[0], player);
			Utils.clearBeaconLine(pConfig.points[1], player);

			if (player.isSneaking()) {
				//createRegion();
				double size=0;
				size+=Math.abs(pConfig.points[0].getBlockX() - pConfig.points[1].getBlockX())+1;
				size*=Math.abs(pConfig.points[0].getBlockZ() - pConfig.points[1].getBlockZ())+1;
				Utils.sendMessageNewLines(player,
					Main.globalConfig.created_region
						.replace("{pos1}", pConfig.points[0].toString())
						.replace("{pos2}", pConfig.points[1].toString())
						.replace("{size}", ""+size)
				);
			} else { //Clear
				Utils.sendMessageNewLines(player, Main.globalConfig.cleared_selection);
			}
			
			pConfig.points[0]=null;
			pConfig.points[1]=null;
		}
	}

	public static void onRightClick(Location pos, Player player, PlayerConfig pConfig) {
		if (player.isSneaking()) { //Gui
			new TI_Gui_Main().show(player);
		} else { //Push boundry
			for (int i=0; i<2; i++) {
				if (pConfig.points[i]==null) continue;

				List<Pair<Block, BlockFace>> blocks=Utils.getDirectionsInRay(player.getLineOfSight((Set<Material>)null, 3));
				blocks=Utils.filterBlockDirListToXY(blocks, pConfig.points[i].getBlockX(), pConfig.points[i].getBlockZ());
				if (blocks.size()!=0) {
					new Utils.delayedBeaconMove(pConfig.points[i], player, blocks.get(0).getRight());
					pConfig.points[i]=pConfig.points[i].getBlock().getRelative(blocks.get(0).getRight()).getLocation();
				}
			}
		}
	}
}
