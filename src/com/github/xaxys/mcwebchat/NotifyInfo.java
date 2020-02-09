package com.github.xaxys.mcwebchat;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NotifyInfo implements Listener {
	private static final NotifyInfo LISTENER = new NotifyInfo();

	public static NotifyInfo getListener() {
		return LISTENER;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		DataBase.getDB().InsertMessage(event.getPlayer().getName(), String.format("玩家 %s 加入了游戏！当前在线 %d人",
				event.getPlayer().getName(), Bukkit.getServer().getOnlinePlayers().size()));
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		DataBase.getDB().InsertMessage(event.getPlayer().getName(), String.format("玩家 %s 退出了游戏！当前在线%d人",
				event.getPlayer().getName(), Bukkit.getServer().getOnlinePlayers().size() - 1));
	}
}
