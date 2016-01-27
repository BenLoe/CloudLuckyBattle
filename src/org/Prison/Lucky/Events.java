package org.Prison.Lucky;


import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.citizensnpcs.api.event.NPCRightClickEvent;
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
	
	@EventHandler
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
	}
	
	@EventHandler
	public void signCreate(SignChangeEvent event){
		Player p = event.getPlayer();
		if (p.isOp() && event.getLine(0).equalsIgnoreCase("[lb queue]")){
			Game.setLocation("Sign", event.getBlock().getLocation());
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
		if (Game.playerInGame(event.getPlayer())){
			Game.items.add(event.getItemDrop().getUniqueId());
		}
	}
	
	@EventHandler
	public void interact(PlayerInteractEvent event){
		Player p = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && p.hasPermission("LuckyBattle.join")){
			Location loc = event.getClickedBlock().getLocation();
			Location loc2 = Game.getLocation("Sign");
			if (loc.getBlockX() == loc2.getBlockX() && loc.getBlockY() == loc2.getBlockY() && loc.getBlockZ() == loc2.getBlockZ()){
				Game.addToQueue(p);
				Sign s = (Sign) loc.getBlock().getState();
				s.setLine(3, "§eIn queue: §b" + Game.inqueue.size());
				s.update();
				return;
			}
		}
	}
	@EventHandler
	public void playerMove(PlayerMoveEvent event){
		Player p = event.getPlayer();
		if (event.getTo().getBlockX() != event.getFrom().getBlockX() || event.getTo().getBlockZ() != event.getFrom().getBlockZ()){
			if (Game.playerInGame(p)){
				if (!Game.canMove){
					event.setTo(event.getFrom());
				}
			}
		}
		if (!Game.watching.contains(p.getName()) && !Game.ingame.contains(p.getName())){
			if (p.getWorld().getName().equals("PrisonMap") && (event.getTo().getBlockX() != event.getFrom().getBlockX() || event.getTo().getBlockZ() != event.getFrom().getBlockZ())){
				if (p.getLocation().getBlock().getLocation().distance(new Location(Bukkit.getWorld("PrisonMap"), -311, 54, 242)) <= 25){
					Game.watching.add(p.getName());
				}
			}
		}else{
			Location util = p.getLocation().clone().subtract(0, 1, 0);
			Location util2 = p.getLocation().clone().subtract(0, 2, 0);
			Location util3 = p.getLocation().clone().subtract(0, 2, 0);
			if (util.getBlock().getType() != Material.SMOOTH_BRICK && util2.getBlock().getType() != Material.SMOOTH_BRICK && util3.getBlock().getType() != Material.SMOOTH_BRICK && util.getBlock().getType() != Material.STAINED_GLASS && util2.getBlock().getType() != Material.STAINED_GLASS && util3.getBlock().getType() != Material.STAINED_GLASS){
				Game.watching.remove(p.getName());
				if (Game.inqueue.contains(p.getName())){
					p.sendMessage(Game.tag + ChatColor.YELLOW + "You left the spectating area, so you were removed from the queue.");
					Game.inqueue.remove(p.getName());
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void playerHitPlayer(EntityDamageByEntityEvent event){
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player && Game.gs == GameState.FIGHT){
			Player p = (Player) event.getEntity();
			Player damager = (Player) event.getDamager();
			if (Game.playerInGame(p)){
				event.setCancelled(false);
				double damage = event.getDamage(DamageModifier.BASE) + event.getDamage(DamageModifier.ARMOR) + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.MAGIC) + event.getDamage(DamageModifier.ABSORPTION);
				Game.lastdamager.put(p.getName(), damager.getName());
				if (Game.damageamount.containsKey(p.getName())){
					if (Game.damageamount.get(p.getName()).containsKey(damager.getName())){
						double d = Game.damageamount.get(p.getName()).get(damager.getName());
						HashMap<String,Double> util = Game.damageamount.get(p.getName());
						util.put(damager.getName(), d + damage);
						Game.damageamount.put(p.getName(), util);
					}else{
						HashMap<String,Double> util = new HashMap<>();
						util.put(damager.getName(), damage);
						Game.damageamount.put(p.getName(), util);
					}
				}else{
					HashMap<String,Double> util = new HashMap<>();
					util.put(damager.getName(), damage);
					Game.damageamount.put(p.getName(), util);
				}
				if (damager.getFoodLevel() < 20){
					damager.setFoodLevel(damager.getFoodLevel() + 2);
				}
				if (p.getHealth() - damage <= 0.5){
					event.setCancelled(true);
					p.damage(0.0);
					Game.Die(p, damager);
				}
			}
		}
		if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player){
			if (event.getDamager() instanceof Arrow){	
				Player p = (Player) event.getEntity();
				Projectile a = (Arrow) event.getDamager();
				Player damager = (Player) a.getShooter();
				double damage = event.getDamage(DamageModifier.BASE) + event.getDamage(DamageModifier.ARMOR) + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.MAGIC) + event.getDamage(DamageModifier.ABSORPTION);
				Game.lastdamager.put(p.getName(), damager.getName());
				if (Game.damageamount.containsKey(p.getName())){
					if (Game.damageamount.get(p.getName()).containsKey(damager.getName())){
						double d = Game.damageamount.get(p.getName()).get(damager.getName());
						HashMap<String,Double> util = Game.damageamount.get(p.getName());
						util.put(damager.getName(), d + damage);
						Game.damageamount.put(p.getName(), util);
					}else{
						HashMap<String,Double> util = new HashMap<>();
						util.put(damager.getName(), damage);
						Game.damageamount.put(p.getName(), util);
					}
				}else{
					HashMap<String,Double> util = new HashMap<>();
					util.put(damager.getName(), damage);
					Game.damageamount.put(p.getName(), util);
				}
				if (damager.getFoodLevel() < 20){
					damager.setFoodLevel(damager.getFoodLevel() + 2);
				}
				if (p.getHealth() - damage <= 0.5){
					event.setCancelled(true);
					p.damage(0.0);
					Game.Die(p, damager);
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void entityDamge(EntityDamageEvent event){
		if (event.getEntity() instanceof Player){
			Player p = (Player) event.getEntity();
			if (Game.playerInGame(p)){
				if (Game.gs == GameState.WIN){
					event.setCancelled(true);
				}else{
					event.setCancelled(false);
					if (event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.PROJECTILE){
					double damage = event.getDamage(DamageModifier.BASE) + event.getDamage(DamageModifier.ARMOR) + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.MAGIC) + event.getDamage(DamageModifier.ABSORPTION);
					if (p.getHealth() - damage <= 0.5){
						event.setCancelled(true);
						p.damage(0.0);
						if (Game.lastdamager.containsKey(p.getName())){
							if (Bukkit.getPlayer(Game.lastdamager.get(p.getName())) != null){
								Game.Die(p, Bukkit.getPlayer(Game.lastdamager.get(p.getName())));
							}else{
								Game.Die(p, null);
							}
						}else{
							Game.Die(p, null);
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
		if (Game.inqueue.contains(p.getName())){
			Game.inqueue.remove(p.getName());
		}
		if (Game.watching.contains(p.getName())){
			Game.watching.remove(p.getName());
		}
		if (Game.ingame.contains(p.getName())){
			
			if (Game.gs == GameState.WARMUP || Game.gs == GameState.PREPARE || Game.gs == GameState.FIGHT || Game.gs == GameState.WIN){
				if (Game.gs == GameState.FIGHT){
					for (ItemStack item : p.getInventory().getContents()){
						if (item != null && item.getType() != Material.AIR){
							Item util = p.getWorld().dropItem(p.getLocation().clone().add(0, 0.2, 0), item);
							Game.items.add(util.getUniqueId());
						}
					}
					for (ItemStack item : p.getInventory().getArmorContents()){
						if (item != null && item.getType() != Material.AIR){
							Item util = p.getWorld().dropItem(p.getLocation().clone().add(0, 0.2, 0), item);
							Game.items.add(util.getUniqueId());
						}
					}
				}
				p.getInventory().clear();
				p.getInventory().setBoots(null);
				p.getInventory().setLeggings(null);
				p.getInventory().setChestplate(null);
				p.getInventory().setHelmet(null);
				p.updateInventory();
				p.getInventory().setContents(Game.invs.get(p.getName()));
				p.getInventory().setArmorContents(Game.armors.get(p.getName()));
				p.setLevel(Game.xpl.get(p.getName()));
				p.setExp(Game.xp.get(p.getName()));
				p.updateInventory();
				p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
				p.removePotionEffect(PotionEffectType.ABSORPTION);
				p.removePotionEffect(PotionEffectType.REGENERATION);
				p.teleport(Game.getLocation("Lobby"));
				Game.ingame.remove(p.getName());
				Game.sendToAll(Game.tag + ChatColor.RED  + p.getName() + ChatColor.YELLOW  + " left the game.");
			}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void blockBreak(BlockBreakEvent event){
		Player p = event.getPlayer();
		if (Game.playerInGame(p)){
			if (Game.gs.equals(GameState.PREPARE)){
				if(Files.getDataFile().getList("Luckies").contains(event.getBlock().getLocation().toVector())){
					if (event.getBlock().getType().equals(Material.GOLD_BLOCK)){
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						List<LuckyBlockHandler> util = Game.luckyset.get(p.getName());
						util.get(0).dropItem(event.getBlock().getLocation());
						util.remove(0);
						Game.luckyset.put(p.getName(), util);
					}
				}
			}
			if (Game.gs == GameState.FIGHT){
				Location loc = event.getBlock().getLocation();
				if (loc.getBlockX() == -311 && loc.getBlockY() == 53 && loc.getBlockZ() == 242){
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
							Game.items.add(zomb.getUniqueId());
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
							Game.items.add(skel.getUniqueId());
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
						Game.items.add(util1.getUniqueId());
						Item util2 = loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND_BOOTS));
						Game.items.add(util2.getUniqueId());	
						break;
					}
					if (item.getType() != Material.AIR){
						Item util = loc.getWorld().dropItemNaturally(loc, item);
						Game.items.add(util.getUniqueId());		
					}
					if (item2.getType() != Material.AIR){
						Item util =loc.getWorld().dropItemNaturally(loc, item2);
						Game.items.add(util.getUniqueId());
					}
				}
			}
		}
	}
}
