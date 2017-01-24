package org.Prison.Lucky;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

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
	
	public static Game instance;
	
	public static Game getInstance(){
		if (instance == null){
			instance = new Game();
		}
		return instance;
	}
	
	public enum GameState {
		WAITING, COUNTDOWN, WARMUP, PREPARE, FIGHT, WIN;
	}
	
	public List<String> arenacache = null;
	public HashMap<String,GameState> gs = new HashMap<String,GameState>();
	public boolean canMove = true;
	public HashMap<String,List<String>> ingame = new HashMap<>();
	public HashMap<String,List<String>> inqueue = new HashMap<>();
	public HashMap<String,ItemStack[]> invs = new HashMap<String,ItemStack[]>();
	public HashMap<String,ItemStack[]> armors = new HashMap<String,ItemStack[]>();
	public HashMap<String,Float> xp = new HashMap<String,Float>();
	public HashMap<String,Integer> xpl = new HashMap<String,Integer>();
	public HashMap<String,List<LuckyBlockHandler>> luckyset = new HashMap<>();
	public HashMap<String,List<UUID>> items = new HashMap<>();
	public HashMap<String,Boolean> enabled = new HashMap<>();
	public String tag = "§8[§6§lLucky§b§lBattle§8]: ";
	public HashMap<String,String> lastdamager = new HashMap<String,String>();
	public HashMap<String,HashMap<String,Double>> damageamount = new HashMap<>();
	
	public GameState getGameState(String arena){
		if (gs.get(arena) == null){
			return GameState.WAITING;
		}
		return gs.get(arena);
	}
	
	public List<String> getArenas(){
		if (arenacache == null){
			if (!Files.getDataFile().contains("Arenas")){
				Files.getDataFile().set("Arenas", new ArrayList<String>());
				Files.saveDataFile();
				arenacache = new ArrayList<String>();
				return new ArrayList<String>();
			}else{
				arenacache = Files.getDataFile().getStringList("Arenas");
				return arenacache;
			}
		}else{
			return arenacache;
		}
	}
	public Location getLocation(String type, String arena){
		Location loc = new Location(Bukkit.getWorld(Files.getDataFile().getString(arena + "locations." + type + ".world")), Files.getDataFile().getInt(arena + "locations." + type + ".x"), Files.getDataFile().getInt(arena + "locations." + type + ".y"), Files.getDataFile().getInt(arena + "locations." + type + ".z"));
		return loc;
	}
	
	public String whichArena(Player p){
		for (Entry<String, List<String>> e : ingame.entrySet()){
			if (e.getValue().contains(p.getName())){
				return e.getKey();
			}
		}
		return null;
	}
	
	public void setLocation(String type, String arena, Location loc){
		Files.getDataFile().set(arena + "locations." + type + ".world", loc.getWorld().getName());
		Files.getDataFile().set(arena + "locations." + type + ".x", loc.getBlockX());
		Files.getDataFile().set(arena + "locations." + type + ".y", loc.getBlockY());
		Files.getDataFile().set(arena + "locations." + type + ".z", loc.getBlockZ());
		Files.saveDataFile();
	}
	
	public void addToQueue(Player p, String arena){
		if (inqueue.get(arena).contains(p.getName())){
			for (int i = 1; i <= inqueue.get(arena).size(); i++){
				if (inqueue.get(arena).get(i - 1) == p.getName()){
				p.sendMessage(tag + "§eYou are §b" + i + "§e in queue.");
				}
			}
		}else{
			List<String> util = inqueue.get(arena);
			util.add(p.getName());
			inqueue.put(arena, util);
		p.sendMessage(tag + "§eYou are now §b" + inqueue.get(arena).size() + "§e in queue.");
		if (getGameState(arena) == GameState.COUNTDOWN && inqueue.get(arena).size() <= 4){
			p.sendMessage(tag + "§eThe countdown has already started. Game starting in §b" + GameManager.time.get(arena) + " §eseconds.");
		}
		}
	}
	
	public void sendToAllInQueue(String message, String arena){
		for (int i = 1; i <= 4; i++){
			if (i > inqueue.get(arena).size()){
				break;
			}
			Bukkit.getPlayer(inqueue.get(arena).get(i - 1)).sendMessage(message);
		}
	}
	
	public void soundToAllInQueue(Sound sound, String arena){
		for (int i = 1; i <= 4; i++){
			if (i > inqueue.get(arena).size()){
				break;
			}
			Player p = Bukkit.getPlayer(inqueue.get(arena).get(i - 1));
			p.playSound(p.getLocation(), sound, 1f, 1f);
		}
	}
	
	public void sendToAll(String message, String arena){
		for (String s : ingame.get(arena)){
			Bukkit.getPlayer(s).sendMessage(message);
		}
	}
	
	public void sendToAllPlus(String message, String arena){
		List<String> sent = new ArrayList<>();
		for (String s : ingame.get(arena)){
			if (!sent.contains(s)){
			Bukkit.getPlayer(s).sendMessage(message);
			sent.add(s);
			}
		}
		Location loc = getLocation("Mid", arena);
		for (Player p : Bukkit.getOnlinePlayers()){
			if (p.getWorld().getName().equals(loc.getWorld().getName())){
				if (p.getLocation().distance(loc) < 35 && !sent.contains(p.getName())){
					sent.add(p.getName());
					Bukkit.getPlayer(p.getName()).sendMessage(message);
				}
			}
		}
	}
	
	public void startGame(String arena){
		for (int i = 1; i <= 4; i++){
			if (i > inqueue.get(arena).size()){
				break;
			}
			Player p = Bukkit.getPlayer(inqueue.get(arena).get(i - 1));
			p.setAllowFlight(false);
			p.setFlying(false);
			if (p.getGameMode() != GameMode.SURVIVAL){
				p.setGameMode(GameMode.SURVIVAL);
			}
			Location loc = getLocation("Spawn" + i, arena).add(0.5, 1, 0.5);
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
			List<String> util = ingame.get(arena);
			util.add(p.getName());
			ingame.put(arena, util);
			p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 5, 50));
			p.setHealth(p.getHealth());
		}
		List<String> util = inqueue.get(arena);
		util.removeAll(ingame.get(arena));
		inqueue.put(arena, util);
		sendToAll(tag + "§eGame starting.", arena);
		for (int i = 1; i <= inqueue.get(arena).size(); i++){
			int util1 = i - 1;
			Player p = Bukkit.getPlayer(inqueue.get(arena).get(util1));
			p.sendMessage(tag + "§eA game has started, you have been moved to §b" + i + "§e in queue.");
		}
	}
	
	@SuppressWarnings("unchecked")
	public void trueStartGame(String arena){
		for (String s : ingame.get(arena)){
			Player p = Bukkit.getPlayer(s);
			
			p.sendMessage(tag + "§c§lFIGHT!");
			p.setWalkSpeed(0.2f);
			p.playSound(p.getLocation(), Sound.WOLF_GROWL, 1f, 1f);
			p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1f);
		}
		for (Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Luckies")){
			Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
			loc.getBlock().setType(Material.AIR);
		}
		
		for (Vector v : (Collection<? extends Vector>) Files.getDataFile().getList(arena + "fences")){
			Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
			loc.getBlock().setType(Material.AIR);
			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.WOOD, (byte)0), 0.2f, 0.2f, 0.2f, 0.2f, 15, loc.clone().add(0.5, 0.5, 0.5), 6);
		}
	}
	
	public void Win(Player p, String arena){
		gs.put(arena, GameState.WIN);
		sendToAllPlus("§b✦§a-----------------------§b✦", arena);
		sendToAllPlus("", arena);
		sendToAllPlus("  §a§lWinner:", arena);
		if (p == null){
			sendToAllPlus("  §eWell... no-one won.", arena);
		}else{
			sendToAllPlus("  §l" + p.getName(), arena);
		}
		sendToAllPlus("", arena);
		sendToAllPlus("§b✦§a-----------------------§b✦", arena);
		if (p != null){
			luckyset.remove(p.getName());
			int Wins = 0;
			if (Files.getDataFile().contains("Players." + p.getUniqueId() + ".Wins")){
				Wins = Files.getDataFile().getInt("Players." + p.getUniqueId() + ".Wins");
			}
			Files.getDataFile().set("Players." + p.getUniqueId() + ".Wins", Wins + 1);
			Files.saveDataFile();
			Stats.getStats(p).addGamesPlayed(1);
			int amount = 6000;
			p.sendMessage(ChatColor.GREEN + "+" + amount + "$");
			MoneyAPI.economy.depositPlayer(p.getName(), amount);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void Die(Player p, Player killer, String arena){
		if (killer != null){
			sendToAllPlus(tag + "§c" + p.getName() + " §ewas killed by §c" + killer.getName() + "§e. §c" + (ingame.get(arena).size() - 1) + "§e players remain.", arena);
		}else{
			sendToAllPlus(tag + "§c" + p.getName() + " §edied to an unknown cause. §c" + (ingame.get(arena).size() - 1) + "§e players remain.", arena);
		}
		List<String> list1 = ingame.get(arena);
		list1.remove(p.getName());
		ingame.put(arena, list1);
		for (ItemStack item : p.getInventory().getContents()){
			if (item != null && item.getType() != Material.AIR){
				Item util = p.getWorld().dropItem(p.getLocation().clone().add(0, 0.2, 0), item);
				List<UUID> list = items.get(arena);
				list.add(util.getUniqueId());
				items.put(arena, list);
			}
		}
		for (ItemStack item : p.getInventory().getArmorContents()){
			if (item != null && item.getType() != Material.AIR){
				Item util = p.getWorld().dropItem(p.getLocation().clone().add(0, 0.2, 0), item);
				List<UUID> list = items.get(arena);
				list.add(util.getUniqueId());
				items.put(arena, list);
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
		p.teleport(getLocation("Lobby", arena));
		p.setFireTicks(0);
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
		p.getInventory().setContents(invs.get(p.getName()));
		p.getInventory().setArmorContents(armors.get(p.getName()));
		p.setExp(xp.get(p.getName()));
		p.setLevel(xpl.get(p.getName()));
		p.updateInventory();		
		Stats.getStats(p).addGamesPlayed(1);
		if (damageamount.containsKey(p.getName())){
		for (Entry<String,Double> e : damageamount.get(p.getName()).entrySet()){
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
					util.sendMessage("§c§l+ " + real + " â�¤ §7(Helped to kill player)"); 
				}
			}
		}
		damageamount.remove(p.getName());
		luckyset.remove(p.getName());
		invs.remove(p.getName());
		armors.remove(p.getName());
		xp.remove(p.getName());
		xpl.remove(p.getName());
		}
		if (killer != null){
			killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 70, 1));
			Stats.getStats(killer).addKills(1);
			int amount = 1000;
			killer.sendMessage(ChatColor.GREEN + "+" + amount + "$");
			MoneyAPI.economy.depositPlayer(killer.getName(), amount);
		}
	}
	
	public boolean playerInGame(Player p){
		for (Entry<String,List<String>> e : ingame.entrySet()){
			if (e.getValue().contains(p.getName())){
				return true;
			}
		}
		return false;
	}
	
	public void playSound(Sound s, Location loc, float pitch, float level, String arena){
		List<String> sent = new ArrayList<>();
		for (String s1 : ingame.get(arena)){
			Player p = Bukkit.getPlayer(s1);
			p.playSound(loc, s, level, pitch);
			sent.add(p.getName());
		}
		Location loc1 = getLocation("Mid", arena);
		for (Player p : Bukkit.getOnlinePlayers()){
			if (p.getWorld().getName().equals(loc1.getWorld().getName())){
				if (p.getLocation().distance(loc1) < 35 && !sent.contains(p.getName())){
					sent.add(p.getName());
					p.playSound(loc, s, level, pitch);
				}
			}
		}
	}
}
