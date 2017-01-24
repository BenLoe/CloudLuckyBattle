package org.Prison.Lucky;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.Prison.Lucky.Game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class GameManager {

	public static HashMap<String,Integer> time = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static void manage(){
		
		boolean reset = true;
		
		for (String arena : Game.getInstance().getArenas()){
		
		if (Game.getInstance().enabled.get(arena) || (!Game.getInstance().getGameState(arena).equals(GameState.WAITING) && !Game.getInstance().getGameState(arena).equals(GameState.COUNTDOWN))){	
			
		GameState gs = Game.getInstance().getGameState(arena);
		int inQueue = Game.getInstance().inqueue.get(arena).size();
		if (gs == GameState.WAITING){
			if (inQueue >= 2){
				Game.getInstance().gs.put(arena, GameState.COUNTDOWN);
				time.put(arena, 20);
				Game.getInstance().sendToAllInQueue(Game.getInstance().tag + "§eGame starting in §b20 §eseconds.", arena);
			}
		}else{
			reset = false;
		}
		if (gs == GameState.COUNTDOWN){
			if (inQueue < 2){
				Game.getInstance().gs.put(arena, GameState.WAITING);
				Game.getInstance().sendToAllInQueue(Game.getInstance().tag + "§eNot enough players to start, waiting for more players.", arena);
			}else{
				int newtime = time.get(arena) - 1;
				if (newtime == 0){
					Game.getInstance().startGame(arena);
					time.put(arena, 6);
					Game.getInstance().canMove = false;
					Game.getInstance().gs.put(arena, GameState.WARMUP);
				}else{
					if (newtime == 5 || newtime == 10){
						Game.getInstance().sendToAllInQueue(Game.getInstance().tag + "§eStarting in §b"+ newtime + " §eseconds.", arena);
						if (newtime == 5){
							Game.getInstance().soundToAllInQueue(Sound.CLICK, arena);
						}
					}
					if (newtime == 2 || newtime == 3 || newtime == 4){
						Game.getInstance().sendToAllInQueue(Game.getInstance().tag + "§eStarting in §c" + newtime + " §eseconds.", arena);
						Game.getInstance().soundToAllInQueue(Sound.CLICK, arena);
					}
					if (newtime == 1){
						Game.getInstance().sendToAllInQueue(Game.getInstance().tag + "§eStarting in §c1 §esecond.", arena);
						Game.getInstance().soundToAllInQueue(Sound.CLICK, arena);
					}
					time.put(arena, newtime);
				}
			}
		}
		if (gs == GameState.WARMUP){
			int newtime = time.get(arena) - 1;
			if (newtime == 0){
				Game.getInstance().gs.put(arena, GameState.PREPARE);
				time.put(arena, 62);
				Game.getInstance().canMove = true;
				Game.getInstance().sendToAll(Game.getInstance().tag + "§aBreak the §6§lLucky §ablocks!", arena);
				for (String s : Game.getInstance().ingame.get(arena)){
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
					Game.getInstance().sendToAll(Game.getInstance().tag + "§ePreparation starts in §b" + newtime + "§e seconds.", arena);
				}
				time.put(arena, newtime);
			}
		}
		if (gs == GameState.PREPARE){
			int newtime = time.get(arena) - 1;
			if (Game.getInstance().ingame.get(arena).size() == 1){
				time.put(arena, 10);
				Game.getInstance().gs.put(arena, GameState.WIN);
				Game.getInstance().Win(Bukkit.getPlayer(Game.getInstance().ingame.get(arena).get(0)), arena);		
			}else
			if (Game.getInstance().ingame.get(arena).size() == 0){
				Game.getInstance().gs.put(arena, GameState.WIN);
				time.put(arena, 10);
				Game.getInstance().Win(null, arena);
			}else
			if (newtime == 0){
				time.put(arena, 5);
				Game.getInstance().trueStartGame(arena);
				Game.getInstance().gs.put(arena, GameState.FIGHT);
			}else{
				for (String s : Game.getInstance().ingame.get(arena)){
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
					Game.getInstance().sendToAll(Game.getInstance().tag + "§eOnly §b1 §eminute left to prepare for battle!", arena);
				}
				if (newtime == 30 || newtime == 10 || newtime == 5 || newtime == 4){
					Game.getInstance().sendToAll(Game.getInstance().tag + "§eOnly §b" + newtime + "§e seconds left to prepare for battle!", arena);
				}
				if (newtime == 3 || newtime == 2){
					Game.getInstance().sendToAll(Game.getInstance().tag + "§eOnly §c" + newtime + "§e seconds left to prepare for battle!", arena);
				}
				if (newtime == 1){
					Game.getInstance().sendToAll(Game.getInstance().tag + "§eOnly §c1 §esecond left to prepare for battle!", arena);

				}
				
				if (newtime == 45){
					for(Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Lava1")){
						Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 38){
					for(Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Lava2")){
						Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 31){
					for(Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Lava3")){
						Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 25){
					for(Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Lava4")){
						Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 16){
					for(Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Lava5")){
						Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				if (newtime == 3){
					for(Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Lava6")){
						Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.LAVA);
					}
				}
				time.put(arena, newtime);
			}
		}
		if (gs == GameState.FIGHT){
			if (time.get(arena) != 0){
			int newtime = time.get(arena) - 1;
			if (newtime == 0){
				time.put(arena, 0);
				for(Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Lava7")){
					Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
					loc.getBlock().setType(Material.LAVA);
				}
			}else{
				time.put(arena, newtime);
			}
			}
				if (Game.getInstance().ingame.get(arena).size() == 1){
					time.put(arena, 10);
					Game.getInstance().gs.put(arena, GameState.WIN);
					Game.getInstance().Win(Bukkit.getPlayer(Game.getInstance().ingame.get(arena).get(0)), arena);		
				}else
				if (Game.getInstance().ingame.get(arena).size() == 0){
					Game.getInstance().gs.put(arena, GameState.WIN);
					time.put(arena, 10);
					Game.getInstance().Win(null, arena);
				}
				
		}
		if (gs == GameState.WIN){
			int newtime = time.get(arena) - 1;
			if (newtime == 0){
				for (String s : Game.getInstance().ingame.get(arena)){
					Player p = Bukkit.getPlayer(s);
					p.getInventory().clear();
					p.getInventory().setBoots(null);
					p.getInventory().setLeggings(null);
					p.getInventory().setChestplate(null);
					p.getInventory().setHelmet(null);
					p.updateInventory();
					p.getInventory().setContents(Game.getInstance().invs.get(s));
					p.getInventory().setArmorContents(Game.getInstance().armors.get(s));
					p.updateInventory();
					p.teleport(Game.getInstance().getLocation("Lobby", arena));
					p.setHealth(20.0);
					p.setExp(Game.getInstance().xp.get(p.getName()));
					p.setLevel(Game.getInstance().xpl.get(p.getName()));
					p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
					p.removePotionEffect(PotionEffectType.ABSORPTION);
					p.removePotionEffect(PotionEffectType.REGENERATION);
				}
				for (Entity e : Bukkit.getWorld("Flat").getEntities()){
					if (Game.getInstance().items.get(arena).contains(e.getUniqueId())){
						e.remove();
					}
				}
				if (Files.getDataFile().contains(arena + "Luckies")){
					for (Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Luckies")){
						Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.GOLD_BLOCK);
					}
					}
										
					for (Vector v : (Collection<? extends Vector>) Files.getDataFile().getList(arena + "fences")){
						Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.JUNGLE_FENCE);
					}
				Location loc = Game.getInstance().getLocation("MidBlock", arena);
				loc.getBlock().setType(Material.DIAMOND_BLOCK);
				Game.getInstance().ingame.put(arena, new ArrayList<String>());
				Game.getInstance().items.put(arena, new ArrayList<UUID>());
				Game.getInstance().gs.put(arena, GameState.WAITING);
			}else{
				if (newtime == 9 || newtime == 5){
					if (!Game.getInstance().ingame.isEmpty()){
						Firework(Bukkit.getPlayer(Game.getInstance().ingame.get(arena).get(0)).getLocation().add(0, 1.2, 0), Color.TEAL, 0);
						Firework(Bukkit.getPlayer(Game.getInstance().ingame.get(arena).get(0)).getLocation().add(0, 1.2, 0), Color.LIME, 0);
					}
				}
				if (newtime == 7 || newtime == 3){
					if (!Game.getInstance().ingame.isEmpty()){
						Firework(Bukkit.getPlayer(Game.getInstance().ingame.get(arena).get(0)).getLocation().add(0, 1.2, 0), Color.BLUE, 0);
						Firework(Bukkit.getPlayer(Game.getInstance().ingame.get(arena).get(0)).getLocation().add(0, 1.2, 0), Color.ORANGE, 0);
					}
				}
				if (newtime <= 7 && newtime >= 1){
					for(Vector v : (List<Vector>) Files.getDataFile().getList(arena + "Lava" + newtime)){
						Location loc = new Location(Bukkit.getWorld("Flat"), v.getX(), v.getY(), v.getZ());
						loc.getBlock().setType(Material.AIR);
					}
				}
				time.put(arena, newtime);
			}
		}
		Location loc = Game.getInstance().getLocation("Sign", arena);
		Sign s = (Sign) loc.getBlock().getState();
		s.setLine(3, "§eIn queue: §b" + inQueue);
		s.update();
		}else{
			Location loc = Game.getInstance().getLocation("Sign", arena);
			Sign s = (Sign) loc.getBlock().getState();
			s.setLine(3, "§cArena disabled");
			s.update();
		}
		}
		if (reset){
			Game.getInstance().invs.clear();
			Game.getInstance().xp.clear();
			Game.getInstance().xpl.clear();
			Game.getInstance().armors.clear();
			Game.getInstance().damageamount.clear();
			Game.getInstance().lastdamager.clear();
			Game.getInstance().luckyset.clear();
		}
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
