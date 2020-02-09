package com.github.xaxys.mcwebchat;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	
	private static File Folder = null;
	public static void setFolder() {
		Folder = Main.PLUGIN.getDataFolder();
	}
	
	public static FileConfiguration getInfoConfig() {
		File file = new File(Folder.getAbsolutePath(), File.separator + "DO_NOT_MODIFY.yml");
		file.setWritable(true, false);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return YamlConfiguration.loadConfiguration(file);
	}
	
	public static void saveInfoConfig(FileConfiguration fc) {
		File file = new File(Folder.getAbsolutePath(), File.separator + "DO_NOT_MODIFY.yml");
		file.setWritable(true, false);
		try {
			fc.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		};
	}
}
