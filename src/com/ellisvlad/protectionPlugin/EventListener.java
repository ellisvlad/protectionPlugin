package com.ellisvlad.protectionPlugin;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.ellisvlad.protectionPlugin.database.DatabaseConnector;

public class EventListener implements Listener {

	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		sendMessageNewLines(event.getPlayer(), Main.globalConfig.welcome_message);
		event.setJoinMessage("");
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (!event.getMessage().toLowerCase().startsWith("/p") && !event.getMessage().toLowerCase().startsWith("/protect")) return;
		event.setCancelled(true);
		String[] words=event.getMessage().split(" ");
		Main.globalConfig=DatabaseConnector.loadDatabaseConfig();
		
		Main.globalConfig.help_message="§4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §nHelp§r §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥\n\n"
				+ "  §6§o/p§7§orotect help§r Show this help\n"
				+ "  §6§o/p§7§orotect §6§ot§7§oool§r Aquire tool\n"
				+ "§4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥ §6⬥ §4⬥\n";
		
		if (words.length<=1 || words[1].equalsIgnoreCase("help")) { // Help
			sendMessageNewLines(event.getPlayer(), Main.globalConfig.help_message);
			return;
		}
		if (words[1].toLowerCase().startsWith("t")) { // Tool
			ToolItemHandler.giveTool(event.getPlayer(), null);
			return;
		}
		
		net.minecraft.server.v1_8_R3.ItemStack nis=CraftItemStack.asNMSCopy(event.getPlayer().getItemInHand());
		System.out.println(nis.getTag());
	}
	
	private void sendMessageNewLines(Player p, String str) {
		for (String line:str.split("\n")) {
			p.sendMessage(line);
		}
	}

}
