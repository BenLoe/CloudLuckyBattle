package org.Prison.Lucky;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public enum LuckyBlockHandler {

	ENCHANT_BOTTLES(0, "Random"), IRON_SWORD(3, "Sword"), IRON(0, "Random"), GOLDEN_APPLE(2, "Healing"), DIAMONDS(0, "Random"),
	STICKS(0, "Random"), DIAMOND_SWORD(2, "Sword"), BOW(2, "Bow"), ARROWS(0, "Random"), DIAMOND_ENCHANT(1, "Sword"),
	IRON_ENCHANT(2, "Sword"), BOW_ENCHANT(1, "Bow"), HEALTH_POTION(2, "Healing"), NO_HEALTH(3, "Healing"), NO_BOW(3, "Bow"), IRON_ARMOR(3, "Armor"), 
	DIAMOND_ARMOR(2, "Armor"), ENCHANT_ARMOR(1, "Armor"), REGEN_POTION(1, "Healing"), FISHING_ROD(0, "Random"), INSTANT_DAMAGE(0, "Random"), SNOWBALL(0, "Random"),
	;

	private int tier;
	private String type;
	
	private LuckyBlockHandler(int tier, String type){
		this.tier = tier;
		this.type = type;
	}
	
	
	public static List<LuckyBlockHandler> getSet(){
		List<LuckyBlockHandler> util = new ArrayList<>();
		int swordtier = 0;
		int armortier = 0;
		int bowtier = 0;
		int healingtier = 0;
		Random r = new Random();
		switch(r.nextInt(5) + 1){
		case 1:
			swordtier = 1;
			armortier = 3;
			bowtier = 3;
			healingtier = 2;
		break;
		case 2:
			swordtier = 2;
			armortier = 2;
			bowtier = 2;
			healingtier = 2;
			break;
		case 3:
			swordtier = 3;
			armortier = 1;
			bowtier = 2;
			healingtier = 1;
			break;
		case 4:
			swordtier = 3;
			armortier = 2;
			bowtier = 1;
			healingtier = 3;
			break;
		case 5:
			swordtier = 2;
			armortier = 1;
			bowtier = 2;
			healingtier = 2;
			}
		List<LuckyBlockHandler> swordutil = new ArrayList<>();
		for (LuckyBlockHandler l : values()){
			if (l.type == "Sword" && l.tier == swordtier){
				swordutil.add(l);
			}
		}
		List<LuckyBlockHandler> armorutil = new ArrayList<>();
		for (LuckyBlockHandler l : values()){
			if (l.type == "Armor" && l.tier == armortier){
				armorutil.add(l);
			}
		}
		List<LuckyBlockHandler> bowutil = new ArrayList<>();
		for (LuckyBlockHandler l : values()){
			if (l.type == "Bow" && l.tier == bowtier){
				bowutil.add(l);
			}
		}
		List<LuckyBlockHandler> healingutil = new ArrayList<>();
		for (LuckyBlockHandler l : values()){
			if (l.type == "Healing" && l.tier == healingtier){
				healingutil.add(l);
			}
		}
		for (int i = 0; i < 50; i++){
			LuckyBlockHandler l = getRandom();
			if (!util.contains(l)){
				util.add(l);
			}
			if  (util.size() == 7){
				break;
			}
		}
		util.add(r.nextInt(7), swordutil.get(r.nextInt(swordutil.size())));
		util.add(r.nextInt(7), armorutil.get(r.nextInt(armorutil.size())));
		util.add(r.nextInt(7), bowutil.get(r.nextInt(bowutil.size())));
		util.add(r.nextInt(7), healingutil.get(r.nextInt(healingutil.size())));
		return util;
	}
	
	public static LuckyBlockHandler getRandom(){
		List<LuckyBlockHandler> util = new ArrayList<>();
		for (LuckyBlockHandler l : values()){
			if (l.type == "Random"){
				util.add(l);
			}
		}
		Random r = new Random();
		return util.get(r.nextInt(util.size()));
	}
	
	@SuppressWarnings("deprecation")
	public void dropItem(Location loc){
		Random r = new Random();
		ItemStack item = new ItemStack(Material.AIR);
		ItemStack item2 = new ItemStack(Material.AIR);
		switch(this){
		case ARROWS:
			item = new ItemStack(Material.ARROW);
			item.setAmount(r.nextInt(8) + 4);
			break;
		case BOW:
			item = new ItemStack(Material.BOW);
			break;
		case BOW_ENCHANT:
			item = new ItemStack(Material.BOW);
			item.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
			break;
		case DIAMONDS:
			item = new ItemStack(Material.DIAMOND);
			item.setAmount(r.nextInt(4) + 1);
			break;
		case DIAMOND_ARMOR:
			if ((r.nextInt(2) + 1) == 1){
				item = new ItemStack(Material.DIAMOND_BOOTS);
				item2 = new ItemStack(Material.DIAMOND_CHESTPLATE);
			}else{
				item = new ItemStack(Material.DIAMOND_LEGGINGS);
				item2 = new ItemStack(Material.DIAMOND_HELMET);
			}
			break;
		case DIAMOND_ENCHANT:
			item = new ItemStack(Material.DIAMOND_SWORD);
			item.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			break;
		case DIAMOND_SWORD:
			item = new ItemStack(Material.DIAMOND_SWORD);
			break;
		case ENCHANT_ARMOR:
			item = new ItemStack(Material.DIAMOND_CHESTPLATE);
			item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
			item2 = new ItemStack(Material.IRON_BOOTS);
			break;
		case ENCHANT_BOTTLES:
			item = new ItemStack(Material.EXP_BOTTLE);
			item.setAmount(r.nextInt(7) + 3);
			break;
		case FISHING_ROD:
			item = new ItemStack(Material.FISHING_ROD);
			break;
		case GOLDEN_APPLE:
			item = new ItemStack(Material.GOLDEN_APPLE);
			break;
		case HEALTH_POTION:
			item = new ItemStack(373, 1, (short)16421);
			item.setAmount(r.nextInt(2) + 1);
			break;
		case INSTANT_DAMAGE:
			item = new ItemStack(373, 1, (short)16460);
			item.setAmount(r.nextInt(2) + 1);
			break;
		case IRON:
			item = new ItemStack(Material.IRON_INGOT);
			item.setAmount(r.nextInt(8) + 4);
			break;
		case IRON_ARMOR:
			if ((r.nextInt(2) + 1) == 1){
				item = new ItemStack(Material.IRON_BOOTS);
				item2 = new ItemStack(Material.IRON_CHESTPLATE);
			}else{
				item = new ItemStack(Material.IRON_LEGGINGS);
				item2 = new ItemStack(Material.IRON_HELMET);
			}
			break;
		case IRON_ENCHANT:
			item = new ItemStack(Material.IRON_SWORD);
			item.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			break;
		case IRON_SWORD:
			item = new ItemStack(Material.IRON_SWORD);
			break;
		case NO_BOW:
			getRandom().dropItem(loc);
			break;
		case NO_HEALTH:
			getRandom().dropItem(loc);
			break;
		case REGEN_POTION:
			Potion potion = new Potion(PotionType.REGEN);
			potion.setLevel(2);
			potion.setSplash(true);
			potion.setHasExtendedDuration(false);
			item = potion.toItemStack(1);
			break;
		case SNOWBALL:
			item = new ItemStack(Material.SNOW_BALL);
			item.setAmount(r.nextInt(11) + 5);
			break;
		case STICKS:
			item = new ItemStack(Material.STICK);
			item.setAmount(r.nextInt(4) + 2);
			break;
		default:
			break;
		}
		if (item.getType() != Material.AIR){
			loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.2, 0.5), item);
		}
		if (item2.getType() != Material.AIR){
			loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.2, 0.5), item2);
		}
	}
}
