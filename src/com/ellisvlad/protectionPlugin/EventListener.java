package com.ellisvlad.protectionPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {

	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
	}

}
