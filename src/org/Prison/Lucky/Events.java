package org.Prison.Lucky;


import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.Prison.Lucky.Game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;


public class Events implements Listener {

	public static Main plugin;
	public Events(Main instance){
		plugin = instance;
	}
	
/*	@EventHandler
	public void npcClick(NPCRightClickEvent event){
		int id = event.getNPC().getId();
		if (id == 185){
			Player p = event.getClicker();
			Stats s = Stats.getStats(p);
			p.sendMessage("");
			p.sendMessage("§e||------§6§lLucky§b§lBattle§e------");
			p.sendMessage("§e||");
			p.sendMessage("§e|| §7Games Played: §9" + s.getGamesPlayed());
			p.sendMessage("§e|| §7Kills: §c" + s.getKills());
			p.sendMessage("§e|| §7Wins: §b" + s.getWins());
			p.sendMessage("§e||");
			p.sendMessage("§e||-----------------------");
			p.sendMessage("");
		}
	} */
	
	@EventHandler
	public void signCreate(SignChangeEvent event){
		Player p = event.getPlayer();
		if (p.isOp() && event.getLine(0).equalsIgnoreCase("[lb queue]")){
			Game.getInstance().setLocation("Sign", event.getLine(1), event.getBlock().getLocation());
			event.setLine(0, "§8[§6§lLucky§b§l Battle§8]");
			event.setLine(1, "Click to join");
			event.setLine(2, "the queue.");
			event.setLine(3, "§eIn queue: §b0");
			Sign s = (Sign) event.getBlock().getState();
			s.update();
		}
	}
	
	@EventHandler
	public void dropItem(PlayerDropItemEvent event){
		if (Game.getInstance().playerInGame(event.getPlayer())){
			String arena = Game.getInstance().whichArena(event.getPlayer());
			List<UUID> util = Game.getInstance().items.get(arena);
			util.add(event.getItemDrop().getUniqueId());
			Game.getInstance().items.put(arena, util);
		}
	}
	
	@EventHandler
	public void interact(PlayerInteractEvent event){
		Player p = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && p.hasPermission("LuckyBattle.join")){
			Location loc = event.getClickedBlock().getLocation();
			for (String arena : Game.getInstance().getArenas()){
				Location loc2 = Game.getInstance().getLocation("Sign", arena);
				if (loc.getBlockX() == loc2.getBlockX() && loc.getBlockY() == loc2.getBlockY() && loc.getBlockZ() == loc2.getBlockZ()){
					if (Game.getInstance().enabled.get(arena)){
						Game.getInstance().addToQueue(p, arena);
						Sign s = (Sign) loc.getBlock().getState();
						s.setLine(3, "§eIn queue: §b" + Game.getInstance().inqueue.get(arena).size());
						s.update();
					}else{
						p.sendMessage(Game.getInstance().tag + "§cArena disabled.");
					}
					return;
				}
			}
		}
	}
	@EventHandler
	public void playerMove(PlayerMoveEvent event){
		Player p = event.getPlayer();
		if (event.getTo().getBlockX() != event.getFrom().getBlockX() || event.getTo().getBlockZ() != event.getFrom().getBlockZ()){
			if (Game.getInstance().playerInGame(p)){
				if (!Game.getInstance().canMove){
					event.setTo(event.getFrom());
				}
			}
		}
		for (String arena : Game.getInstance().getArenas()){
			if (Game.getInstance().inqueue.get(arena).contains(p.getName())){
				Location loc = Game.getInstance().getLocation("Mid", arena);
				if (p.getWorld().getName().equals(loc.getWorld().getName())){
					if (p.getLocation().distance(loc) > 30){
						List<String> util = Game.getInstance().inqueue.get(arena);
						util.remove(p.getName());
						Game.getInstance().inqueue.put(arena, util);
						p.sendMessage(Game.getInstance().tag + "§eYou left the spectating area so you were removed from the queue.");
					}
				}else{
					List<String> util = Game.getInstance().inqueue.get(arena);
					util.remove(p.getName());
					Game.getInstance().inqueue.put(arena, util);
					p.sendMessage(Game.getInstance().tag + "§eYou left the spectating area so you were removed from the queue.");
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void playerHitPlayer(EntityDamageByEntityEvent event){
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player){
			Player p = (Player) event.getEntity();
			Player damager = (Player) event.getDamager();
			if (Game.getInstance().playerInGame(p)){
				if (Game.getInstance().getGameState(Game.getInstance().whichArena(p)) == GameState.FIGHT){
				event.setCancelled(false);
				double damage = event.getDamage(DamageModifier.BASE) + event.getDamage(DamageModifier.ARMOR) + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.MAGIC) + event.getDamage(DamageModifier.ABSORPTION);
				Game.getInstance().lastdamager.put(p.getName(), damager.getName());
				if (Game.getInstance().damageamount.containsKey(p.getName())){
					if (Game.getInstance().damageamount.get(p.getName()).containsKey(damager.getName())){
						double d = Game.getInstance().damageamount.get(p.getName()).get(damager.getName());
						HashMap<String,Double> util = Game.getInstance().damageamount.get(p.getName());
						util.put(damager.getName(), d + damage);
						Game.getInstance().damageamount.put(p.getName(), util);
					}else{
						HashMap<String,Double> util = new HashMap<>();
						util.put(damager.getName(), damage);
						Game.getInstance().damageamount.put(p.getName(), util);
					}
				}else{
					HashMap<String,Double> util = new HashMap<>();
					util.put(damager.getName(), damage);
					Game.getInstance().damageamount.put(p.getName(), util);
				}
				if (damager.getFoodLevel() < 20){
					damager.setFoodLevel(damager.getFoodLevel() + 2);
				}
				if (p.getHealth() - damage <= 0.5){
					event.setCancelled(true);
					p.damage(0.0);
					Game.getInstance().Die(p, damager, Game.getInstance().whichArena(p));
				}
			}
			}
		}
		if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player){
			if (event.getDamager() instanceof Arrow){
				Player p = (Player) event.getEntity();
				if (Game.getInstance().playerInGame(p)){
					if (Game.getInstance().getGameState(Game.getInstance().whichArena(p)) == GameState.FIGHT){
				Projectile a = (Arrow) event.getDamager();
				Player damager = (Player) a.getShooter();
				double damage = event.getDamage(DamageModifier.BASE) + event.getDamage(DamageModifier.ARMOR) + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.MAGIC) + event.getDamage(DamageModifier.ABSORPTION);
				Game.getInstance().lastdamager.put(p.getName(), damager.getName());
				if (Game.getInstance().damageamount.containsKey(p.getName())){
					if (Game.getInstance().damageamount.get(p.getName()).containsKey(damager.getName())){
						double d = Game.getInstance().damageamount.get(p.getName()).get(damager.getName());
						HashMap<String,Double> util = Game.getInstance().damageamount.get(p.getName());
						util.put(damager.getName(), d + damage);
						Game.getInstance().damageamount.put(p.getName(), util);
					}else{
						HashMap<String,Double> util = new HashMap<>();
						util.put(damager.getName(), damage);
						Game.getInstance().damageamount.put(p.getName(), util);
					}
				}else{
					HashMap<String,Double> util = new HashMap<>();
					util.put(damager.getName(), damage);
					Game.getInstance().damageamount.put(p.getName(), util);
				}
				if (damager.getFoodLevel() < 20){
					damager.setFoodLevel(damager.getFoodLevel() + 2);
				}
				if (p.getHealth() - damage <= 0.5){
					event.setCancelled(true);
					p.damage(0.0);
					Game.getInstance().Die(p, damager, Game.getInstance().whichArena(p));
				}
			}
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void entityDamge(EntityDamageEvent event){
		if (event.getEntity() instanceof Player){
			Player p = (Player) event.getEntity();
			if (Game.getInstance().playerInGame(p)){
				if (Game.getInstance().getGameState(Game.getInstance().whichArena(p)) == GameState.WIN){
					event.setCancelled(true);
				}else{
					event.setCancelled(false);
					if (event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.PROJECTILE){
					double damage = event.getDamage(DamageModifier.BASE) + event.getDamage(DamageModifier.ARMOR) + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.MAGIC) + event.getDamage(DamageModifier.ABSORPTION);
					if (p.getHealth() - damage <= 0.5){
						event.setCancelled(true);
						p.damage(0.0);
						if (Game.getInstance().lastdamager.containsKey(p.getName())){
							if (Bukkit.getPlayer(Game.getInstance().lastdamager.get(p.getName())) != null){
								Game.getInstance().Die(p, Bukkit.getPlayer(Game.getInstance().lastdamager.get(p.getName())), Game.getInstance().whichArena(p));
							}else{
								Game.getInstance().Die(p, null, Game.getInstance().whichArena(p));
							}
						}else{
							Game.getInstance().Die(p, null, Game.getInstance().whichArena(p));
						}
					}
				}
				}
			}
		}
	}
	
	@EventHandler
	public void playerLeave(PlayerQuitEvent event){
		Player p = event.getPlayer();
		for (String arena : Game.getInstance().getArenas()){
		if (Game.getInstance().inqueue.get(arena).contains(p.getName())){
			List<String> util = Game.getInstance().inqueue.get(arena);
			util.remove(p.getName());
			Game.getInstance().inqueue.put(arena, util);
		}
		if (Game.getInstance().ingame.get(arena).contains(p.getName())){
			
			if (Game.getInstance().getGameState(arena) == GameState.WARMUP || Game.getInstance().getGameState(arena) == GameState.PREPARE || Game.getInstance().getGameState(arena) == GameState.FIGHT || Game.getInstance().getGameState(arena) == GameState.WIN){
				if (Game.getInstance().getGameState(arena) == GameState.FIGHT){
					for (ItemStack item : p.getInventory().getContents()){
						if (item != null && item.getType() != Material.AIR){
							Item util = p.getWorld().dropItem(p.getLocation().clone().add(0, 0.2, 0), item);
							List<UUID> list = Game.getInstance().items.get(arena);
							list.add(util.getUniqueId());
							Game.getInstance().items.put(arena, list);
						}
					}
					for (ItemStack item : p.getInventory().getArmorContents()){
						if (item != null && item.getType() != Material.AIR){
							Item util = p.getWorld().dropItem(p.getLocation().clone().add(0, 0.2, 0), item);
							List<UUID> list = Game.getInstance().items.get(arena);
							list.add(util.getUniqueId());
							Game.getInstance().items.put(arena, list);
						}
					}
				}
				p.getInventory().clear();
				p.getInventory().setBoots(null);
				p.getInventory().setLeggings(null);
				p.getInventory().setChestplate(null);
				p.getInventory().setHelmet(null);
				p.updateInventory();
				p.getInventory().setContents(Game.getInstance().invs.get(p.getName()));
				p.getInventory().setArmorContents(Game.getInstance().armors.get(p.getName()));
				p.setLevel(Game.getInstance().xpl.get(p.getName()));
				p.setExp(Game.getInstance().xp.get(p.getName()));
				p.updateInventory();
				p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
				p.removePotionEffect(PotionEffectType.ABSORPTION);
				p.removePotionEffect(PotionEffectType.REGENERATION);
				p.teleport(Game.getInstance().getLocation("Lobby", arena));
				List<String> list = Game.getInstance().ingame.get(arena);
				list.remove(p.getName());
				Game.getInstance().ingame.put(arena, list);
				Game.getInstance().sendToAll(Game.getInstance().tag + ChatColor.RED  + p.getName() + ChatColor.YELLOW  + " left the Game.getInstance().", arena);
			}
		}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void blockBreak(BlockBreakEvent event){
		Player p = event.getPlayer();
		if (Game.getInstance().playerInGame(p)){
			if (Game.getInstance().getGameState(Game.getInstance().whichArena(p)).equals(GameState.PREPARE)){
				if(Files.getDataFile().getList(Game.getInstance().whichArena(p) + "Luckies").contains(event.getBlock().getLocation().toVector())){
					if (event.getBlock().getType().equals(Material.GOLD_BLOCK)){
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						List<LuckyBlockHandler> util = Game.getInstance().luckyset.get(p.getName());
						util.get(0).dropItem(event.getBlock().getLocation());
						util.remove(0);
						Game.getInstance().luckyset.put(p.getName(), util);
					}
				}
			}
			if (Game.getInstance().getGameState(Game.getInstance().whichArena(p)) == GameState.FIGHT){
				Location loc = event.getBlock().getLocation();
				boolean go = false;
				String arena = "";
				for (String arena1 : Game.getInstance().getArenas()){
					Location loc1 = Game.getInstance().getLocation("MidBlock", arena1);
					if (loc.getBlockX() == loc1.getBlockX() && loc.getBlockY() == loc1.getBlockY() && loc.getBlockZ() == loc1.getBlockZ()){
						go = true;
						arena = arena1;
						break;
					}
				}
				if (go){
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					int r = new Random().nextInt(8) + 1;
					ItemStack item = new ItemStack(Material.AIR);
					ItemStack item2 = new ItemStack(Material.AIR);
					loc.add(0.5, 0.5, 0.5);
					switch(r){
					case 1:
						item = new ItemStack(Material.DIAMOND_SWORD);
						item.addEnchantment(Enchantment.DAMAGE_ALL, 2);
						break;
					case 2: 
						item = new ItemStack(Material.DIAMOND_SWORD);
						item.addEnchantment(Enchantment.FIRE_ASPECT, 1);
						break;
					case 3:
						for (int i = 0; i < 5; i++){
							double x = (-0.3 + (0.3 - -0.3) * new Random().nextDouble());
							double z = (-0.3 + (0.3 - -0.3) * new Random().nextDouble());
							Zombie zomb = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
							zomb.setVelocity(new Vector(x, 0.1, z));
							List<UUID> list = Game.getInstance().items.get(arena);
							list.add(zomb.getUniqueId());
							Game.getInstance().items.put(arena, list);
						}
						break;
					case 4:
						item = new ItemStack(Material.BOW);
						item.addEnchantment(Enchantment.ARROW_DAMAGE, 3);
						item2 = new ItemStack(Material.ARROW);
						item2.setAmount(20);
						break;
					case 5:
						for (int i = 0; i < 4; i++){
							double x = (-0.3 + (0.3 - -0.3) * new Random().nextDouble());
							double z = (-0.3 + (0.3 - -0.3) * new Random().nextDouble());
							Skeleton skel = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
							skel.setVelocity(new Vector(x, 0.1, z));
							List<UUID> list = Game.getInstance().items.get(arena);
							list.add(skel.getUniqueId());
							Game.getInstance().items.put(arena, list);
						}
						break;
					case 6:
						item = new ItemStack(Material.DIAMOND_LEGGINGS);
						item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
						item2 = new ItemStack(Material.DIAMOND_HELMET);
						item2.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
						break;
					case 7:
						item = new ItemStack(373, 2, (short)16417);
						item2 = new ItemStack(373, 1, (short)16420);
						break;
					case 8:
						item = new ItemStack(Material.DIAMOND_HELMET);
						item2 = new ItemStack(Material.DIAMOND_CHESTPLATE);
						Item util1 = loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND_LEGGINGS));
						List<UUID> list = Game.getInstance().items.get(arena);
						list.add(util1.getUniqueId());
						Item util2 = loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND_BOOTS));
						list.add(util2.getUniqueId());
						Game.getInstance().items.put(arena, list);
						break;
					}
					if (item.getType() != Material.AIR){
						Item util = loc.getWorld().dropItemNaturally(loc, item);
						List<UUID> list = Game.getInstance().items.get(arena);
						list.add(util.getUniqueId());
						Game.getInstance().items.put(arena, list);		
					}
					if (item2.getType() != Material.AIR){
						Item util =loc.getWorld().dropItemNaturally(loc, item2);
						List<UUID> list = Game.getInstance().items.get(arena);
						list.add(util.getUniqueId());
						Game.getInstance().items.put(arena, list);	
					}
				}
			}
		}
	}
}
