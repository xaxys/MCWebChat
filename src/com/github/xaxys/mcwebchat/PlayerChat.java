package com.github.xaxys.mcwebchat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;


public class PlayerChat implements Listener {
	private static final PlayerChat LISTENER = new PlayerChat();
	
	public static PlayerChat getListener() {
		return LISTENER;
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		DataBase.getDB().InsertMessage(event.getPlayer().getName(), event.getMessage());
	}
}
