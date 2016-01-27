package org.Prison.Lucky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.Prison.Main.Leaderboard.CompareValues;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public class KillLeaderboard {

	@SuppressWarnings("deprecation")
	public static Map<String,Integer> getLeaderboard(){
		Map<String,Integer> moneys = new HashMap<String,Integer>();
		CompareValues comp = new CompareValues(moneys);
		TreeMap<String,Integer> sorted = new TreeMap<String,Integer>(comp);
		for (String s : Files.getDataFile().getStringList("PlayersList")){
			int wins = 0;
			if (Files.getDataFile().contains("Players." + s + ".Kills")){
				wins = Files.getDataFile().getInt("Players." + s + ".Kills");
			}
			moneys.put(s, wins);
		}
		sorted.putAll(moneys);
		return sorted;
	}
	

	public static void updateSigns(){
		Map<String,Integer> leadersutil = getLeaderboard();
		List<String> leaders = new ArrayList<String>();
		for (Entry<String,Integer> e : leadersutil.entrySet()){
			leaders.add((String) e.getKey());
		}
		for (int i = 0; i <= 4 ; i++){
			int place = i + 1;
			Location loc = new Location(Bukkit.getWorld("PrisonMap"), -335, 56, 244).subtract(0, 1, i);
			Sign sign = (Sign) loc.getBlock().getState();
			String name = org.Prison.Main.Files.getDataFile().getString("Players." + leaders.get(i) + ".Name");
			String uuid = leaders.get(i);
			int wins = 0;
			if (Files.getDataFile().contains("Players." + uuid + ".Kills")){
				wins = Files.getDataFile().getInt("Players." + uuid + ".Kills");
			}
			sign.setLine(0, "§8§m--§c§l" + place + "§8§m--");
			sign.setLine(1, "§c§l" + trimName(name));
			sign.setLine(2, "§8" + wins);
			sign.setLine(3, "§7§oMost kills");
			sign.update();
		}
	}
	
	public static String trimName(String s){
		if (s.length() > 15){
			return s.substring(0, 15);
		}
		return s;
	}
}
