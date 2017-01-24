package org.Prison.Lucky;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.Prison.Lucky.Game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;


public class Main extends JavaPlugin{

	Files files = new Files(this);
	Events events = new Events(this);
	
	public void onEnable(){
		if (!Files.getDataFile().contains("PlayersList")){
			Files.getDataFile().set("PlayersList", new ArrayList<String>());
			Files.saveDataFile();
		}
		Bukkit.getPluginManager().registerEvents(events, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				GameManager.manage();
			}
		}, 20l, 20l);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				for (String arena : Game.getInstance().getArenas()){
					if (Game.getInstance().getGameState(arena) == GameState.FIGHT){
						for (String s : Game.getInstance().ingame.get(arena)){
							Bukkit.getPlayer(s).setFoodLevel(Bukkit.getPlayer(s).getFoodLevel() - 1);
						}
					}
				}
			}
		}, 20l, 65l);
		for (String arena : Game.getInstance().getArenas()){
			Game.getInstance().ingame.put(arena, new ArrayList<String>());
			Game.getInstance().inqueue.put(arena, new ArrayList<String>());
			Game.getInstance().items.put(arena, new ArrayList<UUID>());
			Game.getInstance().gs.put(arena, GameState.WAITING);
			Game.getInstance().enabled.put(arena, Files.getDataFile().getBoolean("Enabled" + arena));			
		}
		MoneyAPI.setupEconomy();
/*		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				if (Files.getDataFile().getList("PlayersList").size() > 5){
					WinLeaderboard.updateSigns();
					KillLeaderboard.updateSigns();
				}
			}
		}, 20l, 3 * 60 * 20l); */
	}
	
	public void onDisable(){
		for (String arena : Game.getInstance().getArenas()){
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
			p.setExp(Game.getInstance().xp.get(s));
			p.updateInventory();
			p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
			p.removePotionEffect(PotionEffectType.ABSORPTION);
			p.removePotionEffect(PotionEffectType.REGENERATION);
			p.teleport(Game.getInstance().getLocation("Lobby", arena));
		}
		}
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public boolean onCommand(CommandSender sender, Command cmd,
			String Label, String[] args){
		if (sender instanceof Player){
			Player p = (Player) sender;
			if (Label.equalsIgnoreCase("LB")){
				if (p.hasPermission("SS.Admin")){
				if (args.length == 0){
					p.sendMessage("§e§l§m----------------§e§l[§f§lLuckyBattle§e§l]§e§l§m----------------");
					p.sendMessage("   ");
					p.sendMessage("§b/LB create <arena>");
					p.sendMessage("§b/LB remove <arena>");
					p.sendMessage("§b/LB setlocation <name> <arena> §a- Set locations.");
					p.sendMessage("§b/LB lucky <arena> §a- Set a lucky block location.");
					p.sendMessage("§b/LB lava <1-7> <arena> §a- Set lava locations.");
					p.sendMessage("§b/LB enable <arena>");
					p.sendMessage("§b/LB disable <arena>");					
					p.sendMessage("§b/LB fence <arena>");
					p.sendMessage("   ");
					p.sendMessage("§e§l§m----------------------------------------");
					return true;
				}
				if (args.length == 1){
					if (args[0].equalsIgnoreCase("create")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB create <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("remove")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB remove <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("enable")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB enable <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("disable")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB disable <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("lava")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB lava <1-7> <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("setlocation")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB setlocation <name> <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("lucky")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB lucky <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("fence")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB fence <arena>");
						return true;
					}
					p.sendMessage(ChatColor.RED + "Unknown Lucky Battle command.");
				}
				if (args.length == 2){
					if (args[0].equalsIgnoreCase("enable")){
						if (Game.getInstance().getArenas().contains(args[1])){
							p.sendMessage(ChatColor.GREEN + "Arena enabled.");
							Game.getInstance().enabled.put(args[1], true);
							Files.getDataFile().set("Enabled" + args[1], true);
							Files.saveDataFile();
						}else{
							p.sendMessage(ChatColor.RED + "Arena does not exist.");
						}
					}
					if (args[0].equalsIgnoreCase("disable")){
						if (Game.getInstance().getArenas().contains(args[1])){
							p.sendMessage(ChatColor.GREEN + "Arena disaled.");
							Game.getInstance().enabled.put(args[1], false);
							Files.getDataFile().set("Enabled" + args[1], false);
							Files.saveDataFile();
							if (Game.getInstance().getGameState(args[1]).equals(GameState.WAITING) || Game.getInstance().getGameState(args[1]).equals(GameState.COUNTDOWN)){
								Game.getInstance().sendToAllInQueue(Game.getInstance().tag + "§eThis arena has been disabled so you have been removed from the queue.", args[1]);
							}
							Game.getInstance().inqueue.put(args[1], new ArrayList<String>());
						}else{
							p.sendMessage(ChatColor.RED + "Arena does not exist.");
						}
					}
					if (args[0].equalsIgnoreCase("create")){
						if (Game.getInstance().getArenas().contains(args[1])){
							p.sendMessage(ChatColor.RED + "Arena already exists.");
						}else{
							Game.getInstance().arenacache.add(args[1]);
							Files.getDataFile().set("Arenas", Game.getInstance().arenacache);
							Files.getDataFile().set("Enabled" + args[1], false);
							Files.saveDataFile();
							Game.getInstance().ingame.put(args[1], new ArrayList<String>());
							Game.getInstance().inqueue.put(args[1], new ArrayList<String>());
							Game.getInstance().items.put(args[1], new ArrayList<UUID>());
							Game.getInstance().gs.put(args[1], GameState.WAITING);
							Game.getInstance().enabled.put(args[1], false);
							p.sendMessage(ChatColor.GREEN + "Created new arena.");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("remove")){
						if (Game.getInstance().getArenas().contains(args[1])){
							p.sendMessage(ChatColor.GREEN + "Removed arena.");
							Game.getInstance().arenacache.remove(args[1]);
							Files.getDataFile().set("Arenas", Game.getInstance().arenacache);
							Files.saveDataFile();
						}else{
							p.sendMessage(ChatColor.RED + "Arena does not exist.");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("lava")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB lava <1-7> <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("setlocation")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB setlocation <name> <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("lucky")){
						p.sendMessage(ChatColor.GREEN + "Set a lucky block location for " + args[1] + ".");
						List<Vector> util = new ArrayList<>();
						if (Files.getDataFile().contains(args[1] + "Luckies")){
							util.addAll((Collection<? extends Vector>) Files.getDataFile().getList(args[1] + "Luckies"));
						}
						util.add(p.getLocation().getBlock().getLocation().toVector());
						Files.getDataFile().set(args[1] + "Luckies", util);
						Files.saveDataFile();
						return true;
					}
					if (args[0].equalsIgnoreCase("fence")){
						p.sendMessage(ChatColor.GREEN + "Set a fence location for " + args[1] + ".");
						List<Vector> util = new ArrayList<>();
						if (Files.getDataFile().contains(args[1] + "fences")){
							util.addAll((Collection<? extends Vector>) Files.getDataFile().getList(args[1] + "fences"));
						}
						util.add(p.getLocation().getBlock().getLocation().toVector());
						Files.getDataFile().set(args[1] + "fences", util);
						Files.saveDataFile();
						return true;
					}
					
				}
				if (args.length == 3){
					if (args[0].equalsIgnoreCase("create")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB create <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("remove")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB remove <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("setlocation")){
						Game.getInstance().setLocation(args[1], args[2], p.getLocation().getBlock().getLocation());
						p.sendMessage(ChatColor.GREEN + "Set the location.");
						return true;
					}
					if (args[0].equalsIgnoreCase("lava")){
						int number = Integer.parseInt(args[1]);
						List<Vector> util = new ArrayList<>();
						if (Files.getDataFile().contains(args[2] + "Lava" + number)){
							util.addAll((Collection<? extends Vector>) Files.getDataFile().getList(args[2] + "Lava" + number));
						}
						util.add(p.getLocation().getBlock().getLocation().toVector());
						Files.getDataFile().set(args[2] + "Lava" + number, util);
						Files.saveDataFile();
						p.sendMessage(ChatColor.GREEN + "Added lava location for group " + number + ".");
						return true;
					}
					if (args[0].equalsIgnoreCase("lucky")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB lucky <arena>");
						return true;
					}
					if (args[0].equalsIgnoreCase("fence")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB fence <arena>");
						return true;
					}
				}
				}else{
					p.sendMessage(ChatColor.RED + "These commands are for admins only.");
				}
			}
			if (Label.equalsIgnoreCase("leave")){
				for (String arena : Game.getInstance().getArenas()){
				if (Game.getInstance().ingame.get(arena).contains(p.getName())){
					if (Game.getInstance().getGameState(arena) == GameState.WARMUP || Game.getInstance().getGameState(arena) == GameState.FIGHT || Game.getInstance().getGameState(arena) == GameState.WIN){
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
						p.setExp(Game.getInstance().xp.get(p.getName()));
						p.setLevel(Game.getInstance().xpl.get(p.getName()));
						p.setHealth(20.0);
						p.removePotionEffect(PotionEffectType.REGENERATION);
						p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
						p.removePotionEffect(PotionEffectType.ABSORPTION);
						p.updateInventory();
						p.teleport(Game.getInstance().getLocation("Lobby", arena));
						Game.getInstance().ingame.remove(p.getName());
						Game.getInstance().sendToAll(Game.getInstance().tag + ChatColor.RED  + p.getName() + ChatColor.YELLOW  + " left the Game.getInstance().", arena);
					}
				}
				}
			}
			return true;
		}
		return true;
	}
}
