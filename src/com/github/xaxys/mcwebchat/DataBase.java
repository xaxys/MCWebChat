package com.github.xaxys.mcwebchat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DataBase {
	public static DataBase DB;
	private String driver = "com.mysql.jdbc.Driver";
	private String url;
	private String user;
	private String password;
	private Connection conn;
	private Statement statement;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static DataBase getDB() {
		if (DB == null) {
			DB = new DataBase();
			DB.Connect();
		}
		return DB;
	}

	public void Connect() {
		url = String.format("jdbc:mysql://%s:%s/%s?characterEncoding=utf-8", Main.Conf.getString("Host"),
				Main.Conf.getString("Port"), Main.Conf.getString("Database"));
		user = Main.Conf.getString("User");
		password = Main.Conf.getString("Password");
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
			if (!conn.isClosed())
				Main.PLUGIN.getLogger().info("Succeeded connecting to the Database!");
			statement = conn.createStatement();

		} catch (ClassNotFoundException e) {
			System.out.println("Sorry,can`t find the Driver!");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void reConnect() {
		try {
			if (conn.isClosed()) {
				Main.PLUGIN.getLogger().info("Database connection has been closed!");
				Main.PLUGIN.getLogger().info("Reconnecting...");
				Connect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void InsertMessage(String name, String message) {
		InsertMessage(name, null, message);
	}

	public void InsertMessage(String name, String target, String message) {
		String id = GetUserId(name);
		if (id == null) {
			CreateUser(name);
			id = GetUserId(name);
		}
		String targetid = "0";
		if (target != null) {
			targetid = GetUserId(target);
			if (targetid == null) {
				CreateUser(target);
				targetid = GetUserId(target);
			}
		}
		InsertMessageById(id, targetid, message);
	}

	public void InsertMessageById(String id, String targetid, String message) {
		try {
			Date date = new Date();
			String s = sdf.format(date);
			String sql = String.format(
					"INSERT INTO message (sender_id,receiver_id,content,send_time,created_at,updated_at,frommc) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', 1)",
					id, targetid, message, Long.toString(date.getTime() / 1000L), s, s);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			reConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String GetUserId(String name) {
		String id = null;
		String sql = String.format("SELECT id FROM user WHERE username='%s'", name);
		try(ResultSet rs = statement.executeQuery(sql)) {
			if (rs.next()) {
				id = rs.getString("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			reConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	public void CreateUser(String name) {
		try {
			Date date = new Date();
			String s = sdf.format(date);
			String sql = String.format(
					"INSERT INTO user (username,created_at,updated_at,registed) VALUES ('%s','%s','%s',0)", name, s, s);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			reConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Long GetLastMessageId() {
		Long lastId = 0L;
		String sql = "SELECT COUNT(*) FROM message";
		try(ResultSet rs = statement.executeQuery(sql)) {
			if (rs.next()) {
				lastId = rs.getLong("COUNT(*)");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			reConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastId;
	}

	public Long GetMessage(Long messageId) {
		//处理公聊信息
		Long idx = messageId;
		String sql = String.format(
				"SELECT message.id, message.content, user.username FROM message JOIN user ON message.sender_id=user.id WHERE message.frommc=0 AND message.id > %s AND message.receiver_id=0 ORDER BY message.id",
				messageId.toString());
		try(ResultSet rs = statement.executeQuery(sql)) {
			if (rs.next()) {
				do {
					String name = rs.getString("username");
					String message = rs.getString("content");
					Bukkit.broadcastMessage(Main.Format1.replace("%PLAYER%", name).replace("%WORD%", message));
				} while (rs.next());
				idx = rs.getLong("id");
			} 
		} catch (SQLException e) {
			e.printStackTrace();
			reConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//处理私聊信息
		Long idx2 = messageId;
		sql = String.format(
				"SELECT message.id, message.content , a.username AS sender, b.username AS receiver FROM message JOIN user as a ON message.sender_id=a.id JOIN user as b ON message.receiver_id=b.id WHERE message.frommc=0 AND message.id > %s ORDER BY message.id",
				messageId.toString());
		try(ResultSet rs = statement.executeQuery(sql)) {
			if (rs.next()) {
				do {
					String sender = rs.getString("sender");
					String receiver = rs.getString("receiver");
					String message = rs.getString("content");
					Player p = Bukkit.getPlayer(receiver);
					if (p != null) {
						p.sendMessage(Main.Format2.replace("%SENDER%", sender).replace("%RECEIVER%", p.getDisplayName())
								.replace("%WORD%", message));
					}
				} while (rs.next());
				idx2 = rs.getLong("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			reConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Math.max(idx, idx2);
	}
}
