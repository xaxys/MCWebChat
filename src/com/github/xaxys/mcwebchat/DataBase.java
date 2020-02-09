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
			Connection conn = DriverManager.getConnection(url, user, password);
			if (!conn.isClosed())
				Main.PLUGIN.getLogger().info("Succeeded connecting to the Database!");
			statement = conn.createStatement();

		} catch (ClassNotFoundException e) {
			System.out.println("Sorry,can`t find the Driver!");
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void InsertMessage(String name, String message) {
		try {
			String sql = String.format("SELECT id FROM user WHERE username='%s'", name);
			ResultSet rs = statement.executeQuery(sql);
			if (!rs.next()) {
				rs.close();
				Date date = new Date();
				String s = sdf.format(date);
				String sql2 = String.format("INSERT INTO user (username,created_at,updated_at,registered) VALUES ('%s','%s','%s',0)", name, s, s);
				statement.executeUpdate(sql2);
				rs = statement.executeQuery(sql);
			} else {
				String id = rs.getString("id");
				rs.close();
				Date date = new Date();
				String s = sdf.format(date);
				sql = String.format(
						"INSERT INTO message (sender_id,receiver_id,content,send_time,created_at,updated_at,frommc) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', 1)",
						id, "0", message, Long.toString(date.getTime() / 1000L), s, s);
				statement.executeUpdate(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Long GetMessage(Long messageId) {
		try {
			String sql = String.format("SELECT message.id, message.content, user.username FROM message JOIN user ON message.sender_id=user.id WHERE message.frommc=0 AND message.id > %s AND message.receiver_id=0 ORDER BY message.id", messageId.toString());
			ResultSet rs = statement.executeQuery(sql);
			Long idx = messageId;
			if (rs.next()) {
				do {
					idx = rs.getLong("id");
					String name = rs.getString("username");
					String message = rs.getString("content");
					Bukkit.broadcastMessage(Main.Format.replace("%PLAYER%", name).replace("%WORD%", message));
				} while (rs.next());
			}
			rs.close();

			String sql2 = String.format(
					"SELECT message.id, message.content , a.username AS sender, b.username AS receiver FROM message JOIN user as a ON message.sender_id=a.id JOIN user as b ON message.receiver_id=b.id WHERE message.frommc=0 AND message.id > %s ORDER BY message.id",
					messageId.toString());
			rs = statement.executeQuery(sql2);
			Long idx2 = messageId;
			if (rs.next()) {
				do {
					idx = rs.getLong("id");
					String sender = rs.getString("sender");
					String receiver = rs.getString("receiver");
					String message = rs.getString("content");
					Player p = Bukkit.getPlayer(receiver);
					if (p != null) {
						p.sendMessage(Main.Format2.replace("%SENDER%", sender).replace("%RECEIVER%", receiver)
								.replace("%WORD%", message));
					}
				} while (rs.next());
			}
			rs.close();
			if (idx > idx2)
				return idx;
			else
				return idx2;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
