package org.Prison.Lucky;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
				if (Game.gs == GameState.FIGHT){
					for (String s : Game.ingame){
						Bukkit.getPlayer(s).setFoodLevel(Bukkit.getPlayer(s).getFoodLevel() - 1);
					}
				}
			}
		}, 20l, 65l);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				if (Files.getDataFile().getList("PlayersList").size() > 5){
					WinLeaderboard.updateSigns();
					KillLeaderboard.updateSigns();
				}
			}
		}, 20l, 3 * 60 * 20l);
	}
	
	public void onDisable(){
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
			p.setExp(Game.xp.get(s));
			p.updateInventory();
			p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
			p.removePotionEffect(PotionEffectType.ABSORPTION);
			p.removePotionEffect(PotionEffectType.REGENERATION);
			p.teleport(Game.getLocation("Lobby"));
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
					p.sendMessage("§b/LB spawn <1-4> §a- Set spawnpoint.");
					p.sendMessage("§b/LB lobby §a- Sets lobby location.");
					p.sendMessage("§b/LB lucky§a- Set a lucky block location.");
					p.sendMessage("§b/LB lava <1-7> §a- Set lava locations.");
					p.sendMessage("   ");
					p.sendMessage("§e§l§m----------------------------------------");
					return true;
				}
				if (args.length == 1){
					if (args[0].equalsIgnoreCase("spawn")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB spawn <1-4>");
						return true;
					}
					if (args[0].equalsIgnoreCase("lava")){
						p.sendMessage(ChatColor.RED + "Incorrect Syntax: /LB lava <1-7>");
						return true;
					}
					if (args[0].equalsIgnoreCase("lobby")){
						p.sendMessage(ChatColor.GREEN + "Set lobby location.");
						Game.setLocation("Lobby", p.getLocation());
						return true;
					}
					if (args[0].equalsIgnoreCase("lucky")){
						p.sendMessage(ChatColor.GREEN + "Set a lucky block location.");
						List<Vector> util = new ArrayList<>();
						if (Files.getDataFile().contains("Luckies")){
							util.addAll((Collection<? extends Vector>) Files.getDataFile().getList("Luckies"));
						}
						util.add(p.getLocation().getBlock().getLocation().toVector());
						Files.getDataFile().set("Luckies", util);
						Files.saveDataFile();
						return true;
					}
					p.sendMessage(ChatColor.RED + "Unknown Lucky Battle command.");
				}
				if (args.length == 2){
					if (args[0].equalsIgnoreCase("lobby")){
						p.sendMessage(ChatColor.RED + "Inccorect syntax: /LB lobby");
						return true;
					}
					if (args[0].equalsIgnoreCase("lucky")){
						p.sendMessage(ChatColor.RED + "Inccorect syntax: /LB lucky");
						return true;
					}
					if (args[0].equalsIgnoreCase("spawn")){
						int number = Integer.parseInt(args[1]);
						p.sendMessage(ChatColor.GREEN + "Saved spawn location for spawnpoint" + number);
						Game.setLocation("Spawn" + number, p.getLocation());
						return true;
					}
					if (args[0].equalsIgnoreCase("lava")){
						int number = Integer.parseInt(args[1]);
						List<Vector> util = new ArrayList<>();
						if (Files.getDataFile().contains("Lava" + number)){
							util.addAll((Collection<? extends Vector>) Files.getDataFile().getList("Lava" + number));
						}
						util.add(p.getLocation().getBlock().getLocation().toVector());
						Files.getDataFile().set("Lava" + number, util);
						Files.saveDataFile();
						p.sendMessage(ChatColor.GREEN + "Added lava location for group " + number + ".");
						return true;
					}
				}
				if (args.length >= 3){
					p.sendMessage(ChatColor.RED + "Too many arguments.");
					return true;
				}
				}else{
					p.sendMessage(ChatColor.RED + "These commands are for admins only.");
				}
			}
			if (Label.equalsIgnoreCase("leave")){
				if (Game.ingame.contains(p.getName())){
					if (Game.gs == GameState.WARMUP || Game.gs == GameState.FIGHT || Game.gs == GameState.WIN){
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
						p.setExp(Game.xp.get(p.getName()));
						p.setLevel(Game.xpl.get(p.getName()));
						p.setHealth(20.0);
						p.removePotionEffect(PotionEffectType.REGENERATION);
						p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
						p.removePotionEffect(PotionEffectType.ABSORPTION);
						p.updateInventory();
						p.teleport(Game.getLocation("Lobby"));
						Game.ingame.remove(p.getName());
						Game.watching.add(p.getName());
						Game.sendToAll(Game.tag + ChatColor.RED  + p.getName() + ChatColor.YELLOW  + " left the game.");
					}
				}
			}
			return true;
		}
		return true;
	}
}
