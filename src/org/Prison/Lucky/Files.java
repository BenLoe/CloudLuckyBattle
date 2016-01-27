package org.Prison.Lucky;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Files {

	public static File f = new File("plugins/LuckyBattle/Data.yml");
	public static YamlConfiguration dataFile = YamlConfiguration.loadConfiguration(f);
	public static Main plugin;
	public Files(Main instance){
		plugin = instance;
	}
	
	public static FileConfiguration config(){
		return plugin.getConfig();
	}
	
	public static void saveConfig(){
		plugin.saveConfig();
	}
	
	public static void reloadConfig(){
		plugin.reloadConfig();
	}
	
	public static YamlConfiguration getDataFile(){
		return dataFile;
	}
	
	public static void saveDataFile(){
		try {
			getDataFile().save(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
