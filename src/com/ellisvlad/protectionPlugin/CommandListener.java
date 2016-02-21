package com.ellisvlad.protectionPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.ellisvlad.protectionPlugin.ToolItem.TI_Handler;

public class CommandListener implements Listener {

	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		Utils.sendMessageNewLines(event.getPlayer(), Main.globalConfig.welcome_message);
		event.setJoinMessage("");
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (!event.getMessage().toLowerCase().startsWith("/p") && !event.getMessage().toLowerCase().startsWith("/protect")) return;
		event.setCancelled(true);
		String[] words=event.getMessage().split(" ");
		
		if (words.length<=1 || words[1].equalsIgnoreCase("help")) { // Help
			Utils.sendMessageNewLines(event.getPlayer(), Main.globalConfig.help_message);
			return;
		}
		if (words[1].toLowerCase().startsWith("t")) { // Tool
			TI_Handler.giveTool(event.getPlayer(), null);
			return;
		}
		
		if (words[1].toLowerCase().startsWith("t")) { // Tool
			TI_Handler.giveTool(event.getPlayer(), null);
			return;
		}
		
		net.minecraft.server.v1_8_R3.ItemStack nis=Utils.getItemStackHandle(event.getPlayer().getItemInHand());
		System.out.println(nis.getTag());
	}

}
