package org.Prison.Lucky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.Prison.Main.Currency.MoneyAPI;
import org.Prison.Main.InfoBoard.InfoBoard;
import org.Prison.Main.Letter.LetterType;
import org.Prison.Main.Traits.SpeedTrait;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

public class Game {
	
	public enum GameState {
		WAITING, COUNTDOWN, WARMUP, PREPARE, FIGHT, WIN;
		
		public void setGameState(){
			gs = this;
		}
	}
	
	public static GameState gs = GameState.WAITING;
	public static boolean canMove = true;
	public static List<String> ingame = new ArrayList<String>();
	public static List<String> inqueue = new ArrayList<String>();
	public static List<String> watching = new ArrayList<String>();
	public static HashMap<String,ItemStack[]> invs = new HashMap<String,ItemStack[]>();
	public static HashMap<String,ItemStack[]> armors = new HashMap<String,ItemStack[]>();
	public static HashMap<String,Float> xp = new HashMap<String,Float>();
	public static HashMap<String,Integer> xpl = new HashMap<String,Integer>();
	public static HashMap<String,List<LuckyBlockHandler>> luckyset = new HashMap<>();
	public static List<UUID> items = new ArrayList<>();
	public static String tag = "§8[§6§lLucky§b§lBattle§8]: ";
	public static HashMap<String,String> lastdamager = new HashMap<String,String>();
	public static HashMap<String,HashMap<String,Double>> damageamount = new HashMap<>();
	
	public static GameState getGameState(){
	return gs;
	}
	
	public static Location getLocation(String type){
		Location loc = new Location(Bukkit.getWorld(Files.getDataFile().getString(type + ".world")), Files.getDataFile().getInt(type + ".x"), Files.getDataFile().getInt(type + ".y"), Files.getDataFile().getInt(type + ".z"));
		return loc;
	}
	
	public static void setLocation(String type, Location loc){
		Files.getDataFile().set(type + ".world", loc.getWorld().getName());
		Files.getDataFile().set(type + ".x", loc.getBlockX());
		Files.getDataFile().set(type + ".y", loc.getBlockY());
		Files.getDataFile().set(type + ".z", loc.getBlockZ());
		Files.saveDataFile();
	}
	
	public static void addToQueue(Player p){
		if (inqueue.contains(p.getName())){
			for (int i = 1; i <= inqueue.size(); i++){
				if (inqueue.get(i - 1) == p.getName()){
				p.sendMessage(Game.tag + "§eYou are §b" + i + "§e in queue.");
				}
			}
		}else{
		inqueue.add(p.getName());
		p.sendMessage(tag + "§eYou are now §b" + inqueue.size() + "§e in queue.");
		if (Game.gs == GameState.COUNTDOWN && inqueue.size() <= 4){
			p.sendMessage(Game.tag + "§eThe countdown has already started. Game starting in §b" + GameManager.time + " §eseconds.");
		}
		}
	}
	
	public static void sendToAllInQueue(String message){
		for (int i = 1; i <= 4; i++){
			if (i > inqueue.size()){
				break;
			}
			Bukkit.getPlayer(inqueue.get(i - 1)).sendMessage(message);
		}
	}
	
	public static void soundToAllInQueue(Sound sound){
		for (int i = 1; i <= 4; i++){
			if (i > inqueue.size()){
				break;
			}
			Player p = Bukkit.getPlayer(inqueue.get(i - 1));
			p.playSound(p.getLocation(), sound, 1f, 1f);
		}
	}
	
	public static void sendToAll(String message){
		for (String s : ingame){
			Bukkit.getPlayer(s).sendMessage(message);
		}
	}
	
	public static void sendToAllPlus(String message){
		List<String> sent = new ArrayList<>();
		for (String s : ingame){
			if (!sent.contains(s)){
			Bukkit.getPlayer(s).sendMessage(message);
			sent.add(s);
			}
		}
		for (String s : watching){
			if (!sent.contains(s)){
			sent.add(s);
			Bukkit.getPlayer(s).sendMessage(message);
			}
		}
	}
	
	public static void startGame(){
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getMainScoreboard();
		Objective health = board.registerNewObjective("LuckyHealth", "health");
		health.setDisplayName("§c§l❤");
		health.setDisplaySlot(DisplaySlot.BELOW_NAME);
		for (int i = 1; i <= 4; i++){
			if (i > inqueue.size()){
				break;
			}
			Player p = Bukkit.getPlayer(inqueue.get(i - 1));
			p.setAllowFlight(false);
			p.setFlying(false);
			if (p.getGameMode() != GameMode.SURVIVAL){
				p.setGameMode(GameMode.SURVIVAL);
			}
			Location loc = Game.getLocation("Spawn" + i).add(0.5, 1, 0.5);
			p.teleport(loc);
			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10, 2));
			p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 100000, 9));
			invs.put(p.getName(), p.getInventory().getContents());
			armors.put(p.getName(), p.getInventory().getArmorContents());
			xp.put(p.getName(), p.getExp());
			xpl.put(p.getName(), p.getLevel());
			p.setExp(0.0f);
			p.setLevel(0);
			p.getInventory().clear();
			p.getInventory().setBoots(null);
			p.getInventory().setLeggings(null);
			p.getInventory().setChestplate(null);
			p.getInventory().setHelmet(null);
			p.updateInventory();
			p.setWalkSpeed(0.2f);
			ParticleEffect.FIREWORKS_SPARK.display(0.3f, 0.8f, 0.3f, 0.05f, 20, loc, 100);
			luckyset.put(p.getName(), LuckyBlockHandler.getSet());
			ingame.add(p.getName());
			p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 5, 50));
			p.setScoreboard(board);
			p.setHealth(p.getHealth());
		}
		inqueue.removeAll(ingame);
		sendToAll(Game.tag + "§eGame starting.");
		for (int i = 1; i <= inqueue.size(); i++){
			int util = i - 1;
			Player p = Bukkit.getPlayer(inqueue.get(util));
			p.sendMessage(Game.tag + "§eA game has started, you have been moved to §b" + i + "§e in queue.");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void trueStartGame(){
		for (String s : ingame){
			Player p = Bukkit.getPlayer(s);
			
			p.sendMessage(Game.tag + "§c§lFIGHT!");
			p.setWalkSpeed(0.2f);
			p.playSound(p.getLocation(), Sound.DIG_WOOD, 1f, 1f);
			p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1f);
		}
		for (Vector v : (List<Vector>) Files.getDataFile().getList("Luckies")){
			Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
			loc.getBlock().setType(Material.AIR);
		}
		List<Vector> util = new ArrayList<>();
		//1
		util.add(new Vector(-327, 49, 243));
		util.add(new Vector(-327, 49, 242));
		util.add(new Vector(-327, 49, 241));
		util.add(new Vector(-327, 50, 243));
		util.add(new Vector(-327, 50, 242));
		util.add(new Vector(-327, 50, 241));
		util.add(new Vector(-327, 51, 243));
		util.add(new Vector(-327, 51, 242));
		util.add(new Vector(-327, 51, 241));
		util.add(new Vector(-327, 52, 242));
		//2
		util.add(new Vector(-310, 49, 258));
		util.add(new Vector(-311, 49, 258));
		util.add(new Vector(-312, 49, 258));
		util.add(new Vector(-310, 50, 258));
		util.add(new Vector(-311, 50, 258));
		util.add(new Vector(-312, 50, 258));
		util.add(new Vector(-310, 51, 258));
		util.add(new Vector(-311, 51, 258));
		util.add(new Vector(-312, 51, 258));
		util.add(new Vector(-311, 52, 258));
		//3
		util.add(new Vector(-295, 49, 243));
		util.add(new Vector(-295, 50, 243));
		util.add(new Vector(-295, 51, 243));
		util.add(new Vector(-295, 49, 242));
		util.add(new Vector(-295, 50, 242));
		util.add(new Vector(-295, 51, 242));
		util.add(new Vector(-295, 52, 242));
		util.add(new Vector(-295, 49, 241));
		util.add(new Vector(-295, 50, 241));
		util.add(new Vector(-295, 51, 241));
		//4
		util.add(new Vector(-310, 49, 226));
		util.add(new Vector(-310, 50, 226));
		util.add(new Vector(-310, 51, 226));
		util.add(new Vector(-311, 49, 226));
		util.add(new Vector(-311, 50, 226));
		util.add(new Vector(-311, 51, 226));
		util.add(new Vector(-311, 52, 226));
		util.add(new Vector(-312, 49, 226));
		util.add(new Vector(-312, 50, 226));
		util.add(new Vector(-312, 51, 226));
		for (Vector v : util){
			Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
			loc.getBlock().setType(Material.AIR);
			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.WOOD, (byte)0), 0.2f, 0.2f, 0.2f, 0.2f, 15, loc.clone().add(0.5, 0.5, 0.5), 6);
		}
	}
	
	public static void Win(Player p){
		Game.gs = GameState.WIN;
		sendToAllPlus("§b✦§a-----------------------§b✦");
		sendToAllPlus("");
		sendToAllPlus("  §a§lWinner:");
		if (p == null){
			sendToAllPlus("  §eWell... no-one won.");
		}else{
			sendToAllPlus("  §l" + p.getName());
		}
		sendToAllPlus("");
		sendToAllPlus("§b✦§a-----------------------§b✦");
		if (p != null){
			int Wins = 0;
			if (Files.getDataFile().contains("Players." + p.getUniqueId() + ".Wins")){
				Wins = Files.getDataFile().getInt("Players." + p.getUniqueId() + ".Wins");
			}
			Files.getDataFile().set("Players." + p.getUniqueId() + ".Wins", Wins + 1);
			Files.saveDataFile();
			Stats.getStats(p).addGamesPlayed(1);
			int needed = 0;
			if (LetterType.getPlayerLetter(p) == LetterType.A){
				needed = 2000;
			}else{
				needed = LetterType.getPlayerLetter(p).getNeeded().getMoney();
			}
			int amount = Math.round(needed / 30);
			p.sendMessage(ChatColor.GREEN + "+" + amount + "$");
			MoneyAPI.addMoney(p, amount);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void Die(Player p, Player killer){
		if (killer != null){
			sendToAllPlus(Game.tag + "§c" + p.getName() + " §ewas killed by §c" + killer.getName() + "§e. §c" + (Game.ingame.size() - 1) + "§e players remain.");
		}else{
			sendToAllPlus(Game.tag + "§c" + p.getName() + " §edied to an unknown cause. §c" + (Game.ingame.size() - 1) + "§e players remain.");
		}
		Game.ingame.remove(p.getName());
		for (ItemStack item : p.getInventory().getContents()){
			if (item != null && item.getType() != Material.AIR){
				Item util = p.getWorld().dropItem(p.getLocation().clone().add(0, 0.2, 0), item);
				items.add(util.getUniqueId());
			}
		}
		for (ItemStack item : p.getInventory().getArmorContents()){
			if (item != null && item.getType() != Material.AIR){
				Item util = p.getWorld().dropItem(p.getLocation().clone().add(0, 0.2, 0), item);
				items.add(util.getUniqueId());
			}
		}
		ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.REDSTONE_BLOCK, (byte)0), 0.2f, 0.7f, 0.2f, 0.0f, 200, p.getLocation().clone().add(0, 0.25, 0), 20);
		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 2));
		Title t = new Title("§c§lYou Died");
		t.setFadeInTime(1);
		t.setStayTime(2);
		t.setFadeInTime(1);
		t.send(p);	
		p.setHealth(p.getMaxHealth());
		p.setVelocity(new Vector(0, 0, 0));
		p.teleport(getLocation("Lobby"));
		SpeedTrait.setCorrectSpeed(p);
		p.getInventory().clear();
		p.getInventory().clear();
		p.getInventory().setBoots(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setHelmet(null);
		p.updateInventory();
		p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
		p.removePotionEffect(PotionEffectType.REGENERATION);
		p.removePotionEffect(PotionEffectType.ABSORPTION);
		p.getInventory().setContents(Game.invs.get(p.getName()));
		p.getInventory().setArmorContents(Game.armors.get(p.getName()));
		p.setExp(Game.xp.get(p.getName()));
		p.setLevel(Game.xpl.get(p.getName()));
		p.updateInventory();		
		InfoBoard.setBoardFor(p);
		Stats.getStats(p).addGamesPlayed(1);
		if (Game.damageamount.containsKey(p.getName())){
		for (Entry<String,Double> e : Game.damageamount.get(p.getName()).entrySet()){
			if (Bukkit.getPlayer(e.getKey()) != null){
				Player util = Bukkit.getPlayer(e.getKey());
				double d = e.getValue() * 0.4;
				if (util.getHealth() + d >= 60.0){
					d = 60.0 - util.getHealth();
				}
				int d1 = (int) Math.round(d * 10);
				double real = d1 / 10;
				double tobeset = util.getHealth() + real;
				if (tobeset >= 60.0){
					tobeset = 60.0;
					real = 60.0 - util.getHealth();
				}
				util.setHealth(tobeset);
				if (killer != null){
					if (killer.getName() != util.getName()){
						util.sendMessage("§c§l+ " + real + " ❤ §7(Helped to kill player)");
					}else{
						util.sendMessage("§c§l+ " + real + " ❤ §7(Killed player)");
					}
				}else{
					util.sendMessage("§c§l+ " + real + " ❤ §7(Helped to kill player)"); 
				}
			}
		}
		Game.damageamount.remove(p.getName());
		}
		if (killer != null){
			int needed = 0;
			killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 70, 1));
			Stats.getStats(killer).addKills(1);
			if (LetterType.getPlayerLetter(killer) == LetterType.A){
				needed = 2000;
			}else{
				needed = LetterType.getPlayerLetter(killer).getNeeded().getMoney();
			}
			int amount = Math.round(needed / 40);
			killer.sendMessage(ChatColor.GREEN + "+" + amount + "$");
			MoneyAPI.addMoney(killer, amount);
		}
		if (!Game.watching.contains(p.getName())){
			Game.watching.add(p.getName());
		}
	}
	
	public static boolean playerInGame(Player p){
		if (ingame.contains(p.getName())){
			return true;
		}
		return false;
	}
	
	public static void playSound(Sound s, Location loc, float pitch, float level){
		for (String s1 : Game.ingame){
			Player p = Bukkit.getPlayer(s1);
			p.playSound(loc, s, level, pitch);
		}
		for (String s2 : Game.watching){
			Player p = Bukkit.getPlayer(s2);
			p.playSound(loc, s, level, pitch);
		}
	}
}
