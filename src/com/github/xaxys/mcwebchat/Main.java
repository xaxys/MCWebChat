package com.github.xaxys.mcwebchat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	public boolean Abort = false;
	public boolean DEBUG = false;
	public static Main PLUGIN;
	public static Configuration Conf;
	public static FileConfiguration InfoConf;
	public static String Format;
	public static String Format2;
	
	public void onLoad() {
		getLogger().info("§d§lMCWebChat §9§l插件已加载！§6作者：xa");
		PLUGIN = this;
		saveDefaultConfig();
	}

	public void onEnable() {
		getLogger().info("§d§lMCWebChat §9§l插件已启用！§6作者：xa");
		Config.setFolder();
		Conf = getConfig();
		InfoConf = Config.getInfoConfig();
		loadConfig();
		Bukkit.getPluginManager().registerEvents(PlayerChat.getListener(), this);
		Bukkit.getPluginManager().registerEvents(NotifyInfo.getListener(), this);
		ShowMessage sm = new ShowMessage(InfoConf.getLong("LastMessageId", 0L));
		getServer().getScheduler().runTaskAsynchronously(this, sm);
	}
	
	public void loadConfig() {
		Format = getConfig().getString("Format").replace("&", "§");
		Format2 = getConfig().getString("Format2").replace("&", "§");
	}
	
	public void onDisable() {
		Abort = true;
		Bukkit.getScheduler().cancelTasks(this);
		InfoConf.set("LastMessageId", ShowMessage.getShowMessage().getMessageId());
		Config.saveInfoConfig(InfoConf);
	}
	
}
