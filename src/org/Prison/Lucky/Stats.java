package org.Prison.Lucky;

import java.util.List;

import org.bukkit.entity.Player;



public class Stats {

	private int Wins, GamesPlayed, Kills;
	private Player p;
	
	public Stats(int shards, int eshards, int kills, Player p){
		this.Wins = shards;
		this.GamesPlayed = eshards;
		this.Kills = kills;
		this.p = p;
	}
	
	public static Stats getStats(Player p){
		int Wins = 0;
		int GamesPlayed = 0;
		int Kills = 0;
		if (Files.getDataFile().contains("Players." + p.getUniqueId() + ".Wins")){
			Wins = Files.getDataFile().getInt("Players." + p.getUniqueId() + ".Wins");
		}
		if (Files.getDataFile().contains("Players." + p.getUniqueId() + ".GamesPlayed")){
			GamesPlayed = Files.getDataFile().getInt("Players." + p.getUniqueId() + ".GamesPlayed");
		}
		if (Files.getDataFile().contains("Players." + p.getUniqueId() + ".Kills")){
			Kills = Files.getDataFile().getInt("Players." + p.getUniqueId() + ".Kills");
		}
		return new Stats(Wins, GamesPlayed, Kills, p);
	}
	
	public int getWins(){
		return this.Wins;
	}
	
	public int getKills(){
		return this.Kills;
	}
	
	public int getGamesPlayed(){
		return this.GamesPlayed;
	}
	
	public Stats setWins(int i){
		Files.getDataFile().set("Players." + p.getUniqueId() + ".Wins", i);
		Files.saveDataFile();
		return new Stats(i, GamesPlayed, Kills, p);
	}
	
	public Stats setEnchantedShards(int i){
		Files.getDataFile().set("Players." + p.getUniqueId() + ".GamesPlayed", i);
		Files.saveDataFile();
		return new Stats(Wins, i, Kills, p);
	}
	
	public Stats setKills(int i){
		Files.getDataFile().set("Players." + p.getUniqueId() + ".Kills", i);
		Files.saveDataFile();
		return new Stats(Wins, GamesPlayed, i, p);
	}
	
	public Stats addWins(int i){
		Files.getDataFile().set("Players." + p.getUniqueId() + ".Wins", Wins + i);
		Files.saveDataFile();
		List<String> moneys = Files.getDataFile().getStringList("PlayersList");
		if (!moneys.contains(p.getUniqueId().toString())){
			moneys.add(p.getUniqueId().toString());
			Files.getDataFile().set("PlayersList", moneys);
			Files.saveDataFile();
		}
		return new Stats(Wins + i, GamesPlayed, Kills, p);
	}
	
	public Stats addGamesPlayed(int i){
		Files.getDataFile().set("Players." + p.getUniqueId() + ".GamesPlayed", GamesPlayed + i);
		Files.saveDataFile();
		List<String> moneys = Files.getDataFile().getStringList("PlayersList");
		if (!moneys.contains(p.getUniqueId().toString())){
			moneys.add(p.getUniqueId().toString());
			Files.getDataFile().set("PlayersList", moneys);
			Files.saveDataFile();
		}
		return new Stats(Wins, GamesPlayed + i, Kills, p);
	}
	
	public Stats addKills(int i){
		Files.getDataFile().set("Players." + p.getUniqueId() + ".Kills", Kills + i);
		Files.saveDataFile();
		List<String> moneys = Files.getDataFile().getStringList("PlayersList");
		if (!moneys.contains(p.getUniqueId().toString())){
			moneys.add(p.getUniqueId().toString());
			Files.getDataFile().set("PlayersList", moneys);
			Files.saveDataFile();
		}
		return new Stats(Wins, GamesPlayed, Kills + 1, p);
	}
	
}
