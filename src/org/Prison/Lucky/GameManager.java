package org.Prison.Lucky;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.Prison.Lucky.Game.GameState;
import org.Prison.Main.InfoBoard.InfoBoard;
import org.Prison.Main.Traits.SpeedTrait;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class GameManager {

	public static int time = 0;
	
	@SuppressWarnings("unchecked")
	public static void manage(){
		GameState gs = Game.getGameState();
		int inQueue = Game.inqueue.size();
		if (gs == GameState.WAITING){
			if (inQueue >= 2){
				Game.gs = GameState.COUNTDOWN;
				time = 20;
				Game.sendToAllInQueue(Game.tag + "§eGame starting in §b20 §eseconds.");
			}
		}
		if (gs == GameState.COUNTDOWN){
			if (inQueue < 2){
				Game.gs = GameState.WAITING;
				Game.sendToAllInQueue(Game.tag + "§eNot enough players to start, waiting for more players.");
			}else{
				int newtime = time - 1;
				if (newtime == 0){
					Game.startGame();
					time = 6;
					Game.canMove = false;
					Game.gs = GameState.WARMUP;
				}else{
					if (newtime == 5 || newtime == 10){
						Game.sendToAllInQueue(Game.tag + "§eStarting in §b"+ newtime + " §eseconds.");
						if (newtime == 5){
							Game.soundToAllInQueue(Sound.CLICK);
						}
					}
					if (newtime == 2 || newtime == 3 || newtime == 4){
						Game.sendToAllInQueue(Game.tag + "§eStarting in §c" + newtime + " §eseconds."	);
						Game.soundToAllInQueue(Sound.CLICK);
					}
					if (newtime == 1){
						Game.sendToAllInQueue(Game.tag + "§eStarting in §c1 §esecond.");
						Game.soundToAllInQueue(Sound.CLICK);
					}
					time = newtime;
				}
			}
		}
		if (gs == GameState.WARMUP){
			int newtime = time - 1;
			if (newtime == 0){
				Game.gs = GameState.PREPARE;
				time = 61;
				Game.canMove = true;
				Game.sendToAll(Game.tag + "§aBreak the §6§lLucky §ablocks!");
				for (String s : Game.ingame){
					Player p = Bukkit.getPlayer(s);
					p.setWalkSpeed(0.2f);
					ItemStack sword = new ItemStack(Material.STONE_SWORD);
					ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
					pickaxe.addEnchantment(Enchantment.DIG_SPEED, 1);
					p.getInventory().addItem(sword);
					p.getInventory().addItem(pickaxe);
					p.updateInventory();
				}
			}else{
				if (newtime == 3 || newtime == 2 || newtime == 1){
					Game.sendToAll(Game.tag + "§ePreparation starts in §b" + newtime + "§e seconds.");
				}
				time = newtime;
			}
		}
		if (gs == GameState.PREPARE){
			int newtime = time - 1;
			if (Game.ingame.size() == 1){
				time = 10;
				Game.gs = GameState.WIN;
				Game.Win(Bukkit.getPlayer(Game.ingame.get(0)));		
			}else
			if (Game.ingame.size() == 0){
				Game.gs = GameState.WIN;
				time = 10;
				Game.Win(null);
			}else
			if (newtime == 0){
				time = 5;
				Game.trueStartGame();
				Game.gs = GameState.FIGHT;
			}else{
				for (String s : Game.ingame){
					Player p = Bukkit.getPlayer(s);
					p.setFireTicks(0);
					String formated = "";
					if (newtime >= 60){
						if (newtime == 70){
							formated = "1:10";
						}else{
						formated = "1:0" + (newtime - 60);
						}
					}
					if (newtime < 60){
						if (newtime < 10){
							formated = "0:0" + newtime;
						}else{
							formated = "0:" + newtime;
						}
					}
					sendActionBar(p, "§6§l" + formated);
				}
				if (newtime == 60){
					Game.sendToAll(Game.tag + "§eOnly §b1 §eminute left to prepare for battle!");
				}
				if (newtime == 30 || newtime == 10 || newtime == 5 || newtime == 4){
					Game.sendToAll(Game.tag + "§eOnly §b" + newtime + "§e seconds left to prepare for battle!");
				}
				if (newtime == 3 || newtime == 2){
					Game.sendToAll(Game.tag + "§eOnly §c" + newtime + "§e seconds left to prepare for battle!");
				}
				if (newtime == 1){
					Game.sendToAll(Game.tag + "§eOnly §c1 §esecond left to prepare for battle!");

				}
				
				if (newtime == 45){
					for(Vector v : (List<Vector>) Files.getDataFile().getList("Lava1")){
						Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 38){
					for(Vector v : (List<Vector>) Files.getDataFile().getList("Lava2")){
						Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 31){
					for(Vector v : (List<Vector>) Files.getDataFile().getList("Lava3")){
						Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 25){
					for(Vector v : (List<Vector>) Files.getDataFile().getList("Lava4")){
						Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 16){
					for(Vector v : (List<Vector>) Files.getDataFile().getList("Lava5")){
						Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 3){
					for(Vector v : (List<Vector>) Files.getDataFile().getList("Lava6")){
						Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				time = newtime;
			}
		}
		if (gs == GameState.FIGHT){
			if (time != 0){
			int newtime = time - 1;
			if (newtime == 0){
				time = 0;
				for(Vector v : (List<Vector>) Files.getDataFile().getList("Lava7")){
					Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
					loc.getBlock().setType(Material.LAVA);
				}
			}else{
			time = newtime;
			}
			}
				if (Game.ingame.size() == 1){
					time = 10;
					Game.gs = GameState.WIN;
					Game.Win(Bukkit.getPlayer(Game.ingame.get(0)));		
				}else
				if (Game.ingame.size() == 0){
					Game.gs = GameState.WIN;
					time = 10;
					Game.Win(null);
				}
				
		}
		if (gs == GameState.WIN){
			int newtime = time - 1;
			if (newtime == 0){
				for (String s : Game.ingame){
					Player p = Bukkit.getPlayer(s);
					p.getInventory().clear();
					p.getInventory().setBoots(null);
					p.getInventory().setLeggings(null);
					p.getInventory().setChestplate(null);
					p.getInventory().setHelmet(null);
					p.updateInventory();
					p.getInventory().setContents(Game.invs.get(s));
					p.getInventory().setArmorContents(Game.armors.get(s));
					p.updateInventory();
					p.teleport(Game.getLocation("Lobby"));
					p.setHealth(20.0);
					p.setExp(Game.xp.get(p.getName()));
					p.setLevel(Game.xpl.get(p.getName()));
					p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
					p.removePotionEffect(PotionEffectType.ABSORPTION);
					p.removePotionEffect(PotionEffectType.REGENERATION);
					SpeedTrait.setCorrectSpeed(p);
					InfoBoard.setBoardFor(p);
				}
				for (Entity e : Bukkit.getWorld("PrisonMap").getEntities()){
					if (Game.items.contains(e.getUniqueId())){
						e.remove();
					}
				}
				if (Files.getDataFile().contains("Luckies")){
					for (Vector v : (List<Vector>) Files.getDataFile().getList("Luckies")){
						Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.GOLD_BLOCK);
					}
					}
					Game.luckyset.clear();
					Game.lastdamager.clear();
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
						loc.getBlock().setType(Material.JUNGLE_FENCE);
					}
				Location loc = new Location(Bukkit.getWorld("PrisonMap"), -311, 53, 242);
				loc.getBlock().setType(Material.DIAMOND_BLOCK);
				Game.ingame.clear();
				Game.invs.clear();
				Game.armors.clear();
				Game.xp.clear();
				Game.xpl.clear();
				Game.items.clear();
				Game.damageamount.clear();
				Game.gs = GameState.WAITING;
				Bukkit.getScoreboardManager().getMainScoreboard().getObjective("LuckyHealth").unregister();
			}else{
				if (newtime == 9 || newtime == 5){
					if (!Game.ingame.isEmpty()){
						Firework(Bukkit.getPlayer(Game.ingame.get(0)).getLocation().add(0, 1.2, 0), Color.TEAL, 0);
						Firework(Bukkit.getPlayer(Game.ingame.get(0)).getLocation().add(0, 1.2, 0), Color.LIME, 0);
					}
				}
				if (newtime == 7 || newtime == 3){
					if (!Game.ingame.isEmpty()){
						Firework(Bukkit.getPlayer(Game.ingame.get(0)).getLocation().add(0, 1.2, 0), Color.BLUE, 0);
						Firework(Bukkit.getPlayer(Game.ingame.get(0)).getLocation().add(0, 1.2, 0), Color.ORANGE, 0);
					}
				}
				if (newtime <= 7 && newtime >= 1){
					for(Vector v : (List<Vector>) Files.getDataFile().getList("Lava" + newtime)){
						Location loc = new Location(Bukkit.getWorld("PrisonMap"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.AIR);
					}
				}
				time = newtime;
			}
		}
		Location loc = Game.getLocation("Sign");
		Sign s = (Sign) loc.getBlock().getState();
		s.setLine(3, "§eIn queue: §b" + inQueue);
		s.update();
	}
	
	public static void Firework(Location loc, Color c, int power){
		Firework fw = (Firework) loc.getWorld().spawn(loc, Firework.class);
		FireworkEffect effect = FireworkEffect.builder().trail(true).flicker(false).withColor(c).with(Type.BALL).build();
		FireworkMeta fwm = fw.getFireworkMeta();
		fwm.clearEffects();
		fwm.addEffect(effect);
		Field f1;
		try {
			f1 = fwm.getClass().getDeclaredField("power");
			f1.setAccessible(true);
			try {
				f1.set(fwm, power);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		fw.setFireworkMeta(fwm);
		}
	
	 public static void sendActionBar(Player p, String message) {
		  IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
		  PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
		  ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
		 }
}
