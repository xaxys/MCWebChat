package com.github.xaxys.mcwebchat;

import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	public static Main PLUGIN;
	public static Configuration Conf;
	public static String Format1;
	public static String Format2;
	public static String Format3;
	public static Long Delay;
	public boolean DebugMode;
	private ShowMessage sm;

	public void onLoad() {
		getLogger().info("§d§lMCWebChat §9§l插件已加载！§6作者：xa");
		PLUGIN = this;
		saveResource("config.yml", false);
	}

	public void onEnable() {
		getLogger().info("§d§lMCWebChat §9§l插件已启用！§6作者：xa");
		loadConfig();
		Bukkit.getPluginManager().registerEvents(PlayerChat.getListener(), this);
		Bukkit.getPluginManager().registerEvents(NotifyInfo.getListener(), this);
		startTask();
	}
	
	public void onDisable() {
		stopTask();
	}

	public void loadConfig() {
		reloadConfig();
		Conf = getConfig();
		DebugMode = Conf.getBoolean("DebugMode", false);
		Delay = Conf.getLong("Delay", 1000L);
		Format1 = Conf.getString("Format1").replace("&", "§");
		Format2 = Conf.getString("Format2").replace("&", "§");
		Format3 = Conf.getString("Format3").replace("&", "§");
		
		System.out.println("DebugMode: " + DebugMode);
		System.out.println("Delay: " + Delay);
		System.out.println("Format1: " + Format1);
		System.out.println("Format2: " + Format2);
		System.out.println("Format3: " + Format3);
	}
	
	public void startTask() {
		sm = new ShowMessage(DataBase.getDB().GetLastMessageId(), Delay);
		sm.Start();
	}
	
	public void stopTask() {
		sm.Stop();
	}

	private final HashMap<String, Chatter> map = new HashMap<>();

	interface Sender {
		public String getName();
		public String getDisplayName();
		public void sendMessage(String s);
		public boolean Online();
	}
	
	class ConsoleSender implements Sender {
		CommandSender sender;
		public ConsoleSender(CommandSender s) {
			sender = s;
		}
		public String getName() {
			return sender.getName();
		}
		public String getDisplayName() {
			return sender.getName();
		}
		public void sendMessage(String s) {
			sender.sendMessage(s);
		}
		public boolean Online() { return true; };
	}
	
	class PlayerSender implements Sender {
		Player sender;
		public PlayerSender(Player p) {
			sender = p;
		}
		public String getName() {
			return sender.getName();
		}
		public String getDisplayName() {
			return sender.getDisplayName();
		}
		public void sendMessage(String s) {
			sender.sendMessage(s);
		}
		public boolean Online() {
			return sender != null;
		};
	}
	
	class Chatter {
		String name = null;
		Sender p = null;
		String WebId = null;

		public Chatter(String name) throws SQLException {
			this.name = name;
			Player player = getOnlinePlayer(name);
			PlayerSender ps = (player == null) ? null : new PlayerSender(player);
			this.p = ps;
			this.WebId = DataBase.getDB().GetUserId(name);
			if (this.p != null && this.WebId == null) {
				DataBase.getDB().CreateUser(name);
				this.WebId = DataBase.getDB().GetUserId(name);
			}
		}
		
		public Chatter(Sender s) throws SQLException {
			this.name = s.getName();
			p = s.Online() ? s : null;
			if (s instanceof PlayerSender) {
				this.WebId = DataBase.getDB().GetUserId(name);
				if (this.p != null && this.WebId == null) {
					DataBase.getDB().CreateUser(name);
					this.WebId = DataBase.getDB().GetUserId(name);
				}
			}
		}

		public boolean Exist() {
			return Online() || WebId != null;
		}
		
		public boolean Online() {
			Update();
			if (p != null) p = p.Online() ? p : null;
			return p != null;
		}
		
		private void Update() {
			if (p instanceof PlayerSender) {
				Player player = getOnlinePlayer(name);
				PlayerSender ps = (player == null) ? null : new PlayerSender(player);
				this.p = ps;
			}
		}

	}

	private void addMap(String p1, Chatter p2) {
		if (map.containsKey(p1)) {
			map.replace(p1, p2);
		} else {
			map.put(p1, p2);
		}
	}
	

	private void sendMessage(Chatter from, Chatter to, String message) {
		if (!to.Exist()) {
			from.p.sendMessage("§4错误:§c 该玩家不在线/不存在！");
			return;
		}
		String s = message.replaceAll("&", "§");
		String toName = to.Online() ? to.p.getDisplayName() : Format3.replace("%PLAYER%", to.name);
		if (from.Online()) {
			from.p.sendMessage("§6[我 -> §a" + toName + "§6] §r" + s);
		}
		if (to.Online()) {
			to.p.sendMessage("§6[§a" + from.p.getDisplayName() + "§6 -> 我] §r" + s);
		}
		addMap(from.name, to);
		addMap(to.name, from);
		if (from.WebId != null && to.WebId != null) {
			DataBase.getDB().InsertMessageById(from.WebId, to.WebId, s);
		}
	}

	private void replyMessage(Chatter from, String message) throws SQLException {
		if (map.containsKey(from.name)) {
			Chatter to = map.get(from.name);
			sendMessage(from, to, message);
			addMap(to.name, from);
		} else {
			from.p.sendMessage("§4错误:§c 你没有可回复的玩家！");
		}
	}
	
	private String combineMessage(final String[] s, final int start) {
		StringBuilder sb = new StringBuilder(s[start]);
		for (int i = start + 1; i < s.length; i++) {
			sb.append(" " + s[i]);
		}
		return sb.toString();
	}

	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (label.equalsIgnoreCase("msg") || label.equalsIgnoreCase("m") || label.equalsIgnoreCase("w")
				|| label.equalsIgnoreCase("whisper") || label.equalsIgnoreCase("t") || label.equalsIgnoreCase("tell")) {
			if (sender instanceof Player) {
				if (args.length >= 2) {
					try {
						Sender p = new PlayerSender((Player)sender);
						Chatter from = new Chatter(p);
						Chatter to = new Chatter(args[0]);
						sendMessage(from, to, combineMessage(args, 1));
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return true;
				}
				sender.sendMessage("/msg <玩家> <文本> 私聊某玩家");
			} else if (args.length >= 2) {
				try {
					ConsoleSender cs = new ConsoleSender(sender);
					Chatter from = new Chatter(cs);
					Chatter to = new Chatter(args[0]);
					sendMessage(from, to, combineMessage(args, 1));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				sender.sendMessage("msg <玩家> <文本>");
				return true;
			}
		} else if (label.equalsIgnoreCase("r") || label.equalsIgnoreCase("reply")) {
			if (sender instanceof Player) {
				if (args.length >= 1) {
					try {
						Sender p = new PlayerSender((Player)sender);
						Chatter from = new Chatter(p);
						replyMessage(from, combineMessage(args, 0));
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return true;
				}
				sender.sendMessage("/r <文本> 回复会话");
			} else {
				if (args.length >= 1) {
					try {
						ConsoleSender cs = new ConsoleSender(sender);
						Chatter from = new Chatter(cs);
						replyMessage(from, combineMessage(args, 0));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					sender.sendMessage("r <文本> 回复会话");
				}
				return true;
			}
		} else if (label.equalsIgnoreCase("mcwebchatr")) {
			stopTask();
			loadConfig();
			startTask();
		}
		return true;
	}
	
	private Player getOnlinePlayer(String name) {
		return Bukkit.getOnlinePlayers().parallelStream()
				.filter(p -> p.getName().equalsIgnoreCase(name))
				.findFirst()
				.orElse(null);
	}
}
