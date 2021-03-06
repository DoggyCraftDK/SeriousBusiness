package com.dogonfire.seriousbusiness;


import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;


public class SeriousBusinessConfiguration
{
	private static SeriousBusinessConfiguration instance;

	public static SeriousBusinessConfiguration instance()
	{	
		return instance;
	}

	private FileConfiguration config;
	private boolean debug = false;
	private String serverName = "Your Server";
	private int turnTimeInSeconds = 10*60;
	private int roundTimeInSeconds = 60*60;
	private int maxCEOOfflineTimeInMinutes = 10;
	private int maxEmployeeOfflineTimeInDays = 14;
	private int maxCompanyPatents = 5;
	private int newCompanyCost = 100;
	private int newPatentCost = 100;
	private int patentChargePercentage = 1;
	private int patentMinutes = 10;
	public int renameCompanyCost = 10;
	public int courtCaseCost = 100;
	public int lawsuitFinePercentage = 5;
	public int lawsuitBribePrPercentage = 100;

	public SeriousBusinessConfiguration()
	{
		instance = this;
	}

	public int getLawsuitBribePrPercentage()
	{
		return lawsuitBribePrPercentage;
	}
	
	public int getCourtCaseCost()
	{
		return courtCaseCost;
	}
	
	public final int getTurnTimeInSeconds()
	{
		return turnTimeInSeconds;
	}

	public final int getRoundTimeInSeconds()
	{
		return roundTimeInSeconds;
	}

	public final int getNewCompanyCost()
	{
		return newCompanyCost;
	}
	
	public final int getRenameCompanyCost()
	{
		return renameCompanyCost;		
	}
	
	public int getPatentTime()
	{
		return patentMinutes;
	}

	public int getPatentChargePercentage()
	{
		return patentChargePercentage;
	}
	
	public int getLawsuitFinePercentage()
	{
		return lawsuitFinePercentage;
	}

	public final int getMaxEmployeeOfflineTimeInDays()
	{
		return maxEmployeeOfflineTimeInDays;
	}

	public final int getMaxCEOOfflineTimeInMinutes()
	{
		return maxCEOOfflineTimeInMinutes;
	}

	public final String getServerName()
	{
		return serverName;
	}

	public final int getPatentCost()
	{
		
		return newPatentCost;
	}
	
	public final boolean isDebug()
	{
		return debug;
	}
	
	public final boolean isEnabledInWorld(World world)
	{
		return true;
	}
	
	public final int getMaxCompanyPatents()
	{
		return maxCompanyPatents;
	}
	
	public void load()
	{
		config = Company.instance().getConfig();	
		
		this.debug = config.getBoolean("Settings.Debug", false);
	}

	public void saveSettings()
	{
		FileConfiguration config = Company.instance().getConfig();
		config.set("Settings.Debug", Boolean.valueOf(this.debug));
		
		Company.instance().saveConfig();
	}
}