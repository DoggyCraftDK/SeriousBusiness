package com.dogonfire.seriousbusiness;

import java.util.UUID;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.dogonfire.seriousbusiness.commands.CompanyCommandExecuter;
import com.dogonfire.seriousbusiness.commands.JobCommandExecuter;
import com.dogonfire.seriousbusiness.commands.LandCommandExecuter;
import com.dogonfire.seriousbusiness.commands.ShopCommandExecuter;
import com.dogonfire.tasks.InfoTask;

public class Company extends JavaPlugin
{
	private Economy economyManager;
	private CompanyManager companyManager = null;
	private PlayerManager playerManager = null;
	private SignManager signManager = null;
	private ChestManager chestManager = null;
	private LandManager landManager = null;
	private PermissionsManager permissionsManager = null;
	private SeriousBusinessConfiguration configuration = null;
	static private Company instance;
	

	static public Company instance()
	{
		return instance;
	}
	
	public LandManager getLandManager()
	{
		return landManager;
	}
	
	public PermissionsManager getPermissionsManager()
	{
		return this.permissionsManager;
	}

	public SignManager getSignManager()
	{
		return this.signManager;
	}
	
	public ChestManager getChestManager()
	{
		return this.chestManager;
	}

	public Economy getEconomyManager()
	{
		return economyManager;
	}
	
	public Material getMaterialById(String materialString)
	{		
		try
		{
			Material itemType = Material.valueOf(materialString);
			
			return itemType;
		}
		catch(Exception ex)
		{
			return null;
		}
	}

	public void log(String message)
	{
		Logger.getLogger("minecraft").info(message);
	}

	public void logDebug(String message)
	{
		if (SeriousBusinessConfiguration.instance().isDebug())
		{
			Logger.getLogger("minecraft").info("[Debug] " + message);
		}
	}

	public void sendInfo(UUID playerId, String message, int delay)
	{
		Player player = getServer().getPlayer(playerId);
		if (player == null)
		{
			logDebug("sendInfo can not find online player with id " + playerId);
			return;
		}
		getServer().getScheduler().runTaskLater(this, new InfoTask(this, playerId, message), delay);
	}

	public void reloadSettings()
	{
	}

	
	public void onEnable()
	{
		Company.instance = this;

		getCommand("company").setExecutor(CompanyCommandExecuter.instance());
		getCommand("shop").setExecutor(ShopCommandExecuter.instance());
		getCommand("shops").setExecutor(ShopCommandExecuter.instance());
		getCommand("job").setExecutor(JobCommandExecuter.instance());
		getCommand("jobs").setExecutor(JobCommandExecuter.instance());
		getCommand("land").setExecutor(LandCommandExecuter.instance());
		
		this.configuration = new SeriousBusinessConfiguration();
		this.permissionsManager = new PermissionsManager();
		this.companyManager = new CompanyManager();
		this.playerManager = new PlayerManager();
		this.chestManager = new ChestManager();
		this.landManager = new LandManager();
		
		PluginManager pm = getServer().getPluginManager();

		if (pm.getPlugin("Vault") != null)
		{
			log("Vault detected. Bounties and sign economy are enabled!");
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
			if (economyProvider != null)
			{
				economyManager = economyProvider.getProvider();
			}
			else
			{
				log("Vault not found.");
			}
		}
		else
		{
			log("Vault not found. Signs are disabled.");
		}		
		

		this.configuration.load();
		this.companyManager.load();
		this.playerManager.load();
		this.landManager.load();
		
		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getServer().getPluginManager().registerEvents(new SignManager(), this);

		Runnable updateTask = new Runnable()
		{
			public void run()
			{
				Company.this.companyManager.update();
				Company.this.landManager.update();
			}
		};
		
		getServer().getScheduler().runTaskTimer(this, updateTask, 200L, 20L);		
	}

	public void onDisable()
	{
		reloadSettings();

		this.companyManager.save();
		this.playerManager.save();
	}	
}