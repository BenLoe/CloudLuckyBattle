package org.Prison.Lucky;

import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class MoneyAPI {

	public static Economy economy = null;
	
	public static boolean setupEconomy(){
		
        RegisteredServiceProvider<Economy> economyProvider = Main.getPlugin(Main.class).getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        
        return (economy != null);
    }
	
}
