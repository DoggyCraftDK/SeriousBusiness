package com.dogonfire.seriousbusiness;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.dogonfire.seriousbusiness.LandManager.LandReport;
import com.dogonfire.seriousbusiness.PatentManager.Patent;


public class CompanyManager
{
	private static CompanyManager		instance;
	private FileConfiguration			companyConfig		= null;
	private File						companyConfigFile	= null;
	private Random						random				= new Random();
	private List<UUID>					onlineCompanies		= new ArrayList<UUID>();
	private long						lastSaveTime;
	private String						pattern				= "HH:mm:ss dd-MM-yyyy";
	DateFormat							formatter			= new SimpleDateFormat(this.pattern);

	public enum JobPosition
	{
		Production,
		Sales,
		Manager,
		Law,
		//Research
	}

	final public class Job
	{	
		final public UUID companyId;
		final public double wage;
		final public JobPosition position;
		
		Job(UUID companyId, double wage, JobPosition position)		
		{
			this.companyId = companyId;
			this.wage = wage;
			this.position = position;
		}
	}
	
	final public class FinancialReport
	{
		final public HashMap<Material, Integer> itemsSoldAmount;
		final public HashMap<Material, Double> itemsSoldValues;		
		final public HashMap<Material, Integer> itemsProducedAmount;
		final public HashMap<JobPosition, Double> wagesPaid;
		
		final public double patentIncome;
		final public double patentExpenses;

		final public double lawsuitIncome;
		final public double lawsuitExpenses;

		final public double companyTaxPercent;
		final public double salesTaxPercent;
		
		final public double income;
		final public double profit;
		
		final public double stockStartValue;
		final public double stockEndValue;
		final public double stockValueChange;		

		final public double loanRate;

		final public double balance;
		
		final public double issuedLoans;
		final public double maxLoanRate;
		final public int maxActivePatents;

		FinancialReport(double companyTaxPercent, double salesTaxPercent, double income, double profit, double stockStartValue, double stockEndValue, double stockValueChange, double balance, HashMap<Material, Integer> itemsSoldAmount, HashMap<Material, Double> itemsSoldValues, 
				HashMap<Material, Integer> itemsProducedAmount, HashMap<JobPosition, Double> wagesPaid, double patentIncome, double patentExpenses, double issuedLoans, double loanRate, double maxLoanRate, int maxActivePatents, double lawsuitIncome, double lawsuitExpenses)		
		{
			this.companyTaxPercent = companyTaxPercent;
			this.salesTaxPercent = salesTaxPercent;
			this.income = income;
			this.profit = profit;
			this.stockStartValue = stockStartValue;
			this.stockEndValue = stockEndValue;
			this.stockValueChange = stockValueChange;
			this.balance = balance;
			this.itemsSoldAmount = itemsSoldAmount;
			this.itemsSoldValues = itemsSoldValues;
			this.itemsProducedAmount = itemsProducedAmount;
			this.wagesPaid = wagesPaid;
			this.patentIncome = patentIncome;
			this.patentExpenses = patentExpenses;
			this.loanRate = loanRate;
			this.issuedLoans = issuedLoans;
			this.maxLoanRate = maxLoanRate;
			this.maxActivePatents = maxActivePatents;
			this.lawsuitIncome = lawsuitIncome;
			this.lawsuitExpenses = lawsuitExpenses;
		}
	}

	CompanyManager()
	{
		instance = this;
	}

	static public CompanyManager instance()
	{
		return instance;
	}
	
	public void load()
	{
		this.companyConfigFile = new File(Company.instance().getDataFolder(), "companies.yml");

		this.companyConfig = YamlConfiguration.loadConfiguration(this.companyConfigFile);

		Company.instance().log("Loaded " + this.companyConfig.getKeys(false).size() + " companies.");
	}

	public void save()
	{
		this.lastSaveTime = System.currentTimeMillis();
		if ((this.companyConfig == null) || (this.companyConfigFile == null))
		{
			return;
		}
		try
		{
			this.companyConfig.save(this.companyConfigFile);
		}
		catch (Exception ex)
		{
			Company.instance().log("Could not save config to " + this.companyConfigFile + ": " + ex.getMessage());
		}
	}

	public void saveTimed()
	{
		if (System.currentTimeMillis() - this.lastSaveTime < 180000L)
		{
			return;
		}
		save();
	}

	public List<UUID> getOfflineCompanies()
	{
		Set<String> allCompanies = this.companyConfig.getKeys(false);
		List<UUID> offlineCompanies = new ArrayList<UUID>();
		for (String companyIdString : allCompanies)
		{
			UUID companyId = UUID.fromString(companyIdString);
			
			if (!this.onlineCompanies.contains(companyId))
			{
				offlineCompanies.add(companyId);
			}
		}
		return offlineCompanies;
	}

	public List<UUID> getCompanies()
	{
		Set<String> allCompanies = this.companyConfig.getKeys(false);
		List<UUID> offlineCompanies = new ArrayList<UUID>();
		for (String companyIdString : allCompanies)
		{
			UUID companyId = UUID.fromString(companyIdString);
			
			if (!this.onlineCompanies.contains(companyId))
			{
				offlineCompanies.add(companyId);
			}
		}
		return offlineCompanies;
	}
	
	public List<UUID> getTopCompanies()
	{
		List<UUID> topCompanies = new ArrayList<UUID>();
		
		Set<String> list = this.companyConfig.getKeys(false);

		for(String key : list)
		{
			topCompanies.add(UUID.fromString(key));
		}
		
		return topCompanies;
	}

	public void updateOnlineCompanies()
	{
		this.onlineCompanies.clear();
		
		for (Player player : Company.instance().getServer().getOnlinePlayers())
		{
			UUID companyId = PlayerManager.instance().getCompanyForEmployee(player.getUniqueId());
			
			if (companyId != null)
			{
				if (!this.onlineCompanies.contains(companyId))
				{
					this.onlineCompanies.add(companyId);
				}
			}
		}
	}

	public List<UUID> getOnlineCompanies()
	{
		return this.onlineCompanies;
	}
	
	public double getBalance(UUID companyId)
	{
		return this.companyConfig.getDouble(companyId.toString() + ".Balance");		
	}

	public void depositCompanyBalance(UUID companyId, double amount)
	{
		double balance = this.companyConfig.getDouble(companyId.toString() + ".Balance");
		
		balance += amount;
				
		this.companyConfig.set(companyId.toString() + ".Balance", balance);

		save();				
	}

	public FinancialReport getFinancialReport(UUID companyId, int round)
	{
		HashMap<Material, Integer> itemsSoldAmount = new HashMap<Material, Integer>();
		HashMap<Material, Double> itemsSoldValues = new HashMap<Material, Double>();		
		HashMap<Material, Integer> itemsProducedAmount = new HashMap<Material, Integer>();
		HashMap<JobPosition, Double> wagesPaid = new HashMap<JobPosition, Double>();
		double totalSoldValue = 0;
		
		for(Material soldItem : this.getItemsSoldThisRound(companyId, round))
		{
			double soldValue = this.getItemSoldValueThisRound(companyId, soldItem, round);
			
			itemsSoldAmount.put(soldItem, this.getItemSoldAmountThisRound(companyId, soldItem, round));
			itemsSoldValues.put(soldItem, soldValue);
			
			totalSoldValue += soldValue;
		}

		double patentIncome = this.getPatentIncomeByRound(companyId, round);
		double patentExpenses = this.getPatentExpensesByRound(companyId, round);
		double lawsuitIncome = this.getLawsuitIncomeByRound(companyId, round);
		double lawsuitExpenses = this.getLawsuitExpensesByRound(companyId, round);

		double income = totalSoldValue + patentIncome;
		
		for(Material producedItem : this.getItemsProducedThisRound(companyId, round))
		{
			itemsProducedAmount.put(producedItem, this.getItemProducedAmountThisRound(companyId, producedItem, round));
		}		
		
		double wagesPaidProduction = this.getProductionWagesPaidThisRound(companyId, round);
		double wagesPaidSales = this.getSalesWagesPaidThisRound(companyId, round);
		double wagesPaidManagers = this.getManagerWagesPaidThisRound(companyId, round);
		
		wagesPaid.put(JobPosition.Production, wagesPaidProduction);
		wagesPaid.put(JobPosition.Sales, wagesPaidSales);
		wagesPaid.put(JobPosition.Manager, wagesPaidManagers);

		long headquartersLandHash = this.companyConfig.getLong(companyId.toString() + ".Home.Headquarters.Land");
		LandReport landReport = LandManager.instance().getLandReport(headquartersLandHash);
		
		double companyTaxPercent = landReport.companyTaxEndValue;
		double salesTaxPercent = landReport.salesTaxEndValue;
			
		double taxesPaid = (landReport.companyTaxEndValue + landReport.salesTaxEndValue) * income / 100;
				
		double balance = this.companyConfig.getDouble(companyId.toString() + ".Balance");
		
		double loanRate = this.getLoanRateForRound(companyId, round);
		double maxLoanRate = this.getMaxLoanRateForRound(companyId, round);
		int maxActivePatents = this.getMaxPatentsForRound(companyId, round);
		double totalAvailableLoans = this.getAvailableLoanForRound(companyId, round);
		
		double profit = patentIncome + lawsuitIncome + totalSoldValue - wagesPaidProduction - wagesPaidSales - taxesPaid - patentExpenses - lawsuitExpenses;
	
		double stockStartValue = this.companyConfig.getDouble(companyId.toString() + ".Round." + round + ".Stock.StartValue");
		double stockEndValue = this.companyConfig.getDouble(companyId.toString() + ".Round." + round + ".Stock.EndValue");
		double stockValueChange = profit / stockStartValue;
		
		if(stockStartValue + stockValueChange < 1)
		{
			stockValueChange = 1 - stockStartValue;
		}

		if(stockEndValue == 0)
		{
			stockEndValue = stockStartValue + stockValueChange;			
		}
			
		FinancialReport report = new FinancialReport(companyTaxPercent, salesTaxPercent, income, profit, stockStartValue, stockEndValue, stockValueChange, balance, itemsSoldAmount, itemsSoldValues, itemsProducedAmount, wagesPaid, patentIncome, patentExpenses, totalAvailableLoans, loanRate, maxLoanRate, maxActivePatents, lawsuitIncome, lawsuitExpenses);
		
		return report;		
	}

	public int getCurrentRound(UUID companyId)
	{		
		int roundId = this.companyConfig.getInt(companyId.toString() + ".CurrentRound");
		
		return roundId;
	}

	public void increaseCurrentRound(UUID companyId)
	{		
		int roundId = this.companyConfig.getInt(companyId.toString() + ".CurrentRound");
		
		roundId++;
		
		this.companyConfig.set(companyId.toString() + ".CurrentRound", roundId);
		
		saveTimed();
	}
	
	public void setHeadquartersHomeForCompany(UUID companyId, Location location, Location previousLocation)
	{
		long hash = LandManager.instance().registerCompanyLocation(companyId, location, previousLocation);
		
		this.companyConfig.set(companyId.toString() + ".Home.Headquarters.X", location.getX());
		this.companyConfig.set(companyId.toString() + ".Home.Headquarters.Y", location.getY());
		this.companyConfig.set(companyId.toString() + ".Home.Headquarters.Z", location.getZ());
		this.companyConfig.set(companyId.toString() + ".Home.Headquarters.World", location.getWorld().getName());
		this.companyConfig.set(companyId.toString() + ".Home.Headquarters.Land", hash);

		saveTimed();
	}

	public void setSalesHomeForCompany(UUID companyId, Location location, Location previousLocation)
	{
		long hash = LandManager.instance().registerCompanyLocation(companyId, location, previousLocation);

		this.companyConfig.set(companyId.toString() + ".Home.Sales.X", location.getX());
		this.companyConfig.set(companyId.toString() + ".Home.Sales.Y", location.getY());
		this.companyConfig.set(companyId.toString() + ".Home.Sales.Z", location.getZ());
		this.companyConfig.set(companyId.toString() + ".Home.Sales.World", location.getWorld().getName());
		this.companyConfig.set(companyId.toString() + ".Home.Sales.Land", hash);

		saveTimed();
	}

	public Location getHeadquartersForCompany(UUID companyId)
	{
		Location location = new Location(null, 0.0D, 0.0D, 0.0D);

		String worldName = this.companyConfig.getString(companyId + ".Home.Headquarters.World");
		if (worldName == null)
		{
			return null;
		}
		
		location.setWorld(Company.instance().getServer().getWorld(worldName));

		location.setX(this.companyConfig.getDouble(companyId + ".Home.Headquarters.X"));
		location.setY(this.companyConfig.getDouble(companyId + ".Home.Headquarters.Y"));
		location.setZ(this.companyConfig.getDouble(companyId + ".Home.Headquarters.Z"));

		return location;
	}
	
	
	public Location getSalesHomeForCompany(UUID companyId)
	{
		Location location = new Location(null, 0.0D, 0.0D, 0.0D);

		String worldName = this.companyConfig.getString(companyId + ".Home.Sales.World");
		if (worldName == null)
		{
			return null;
		}
		location.setWorld(Company.instance().getServer().getWorld(worldName));

		location.setX(this.companyConfig.getDouble(companyId + ".Home.Sales.X"));
		location.setY(this.companyConfig.getDouble(companyId + ".Home.Sales.Y"));
		location.setZ(this.companyConfig.getDouble(companyId + ".Home.Sales.Z"));

		return location;
	}
	
	public int getManagerWagesPaidThisRound(UUID companyId, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + ".ManagerWages");
	}	

	public int getSalesWagesPaidThisRound(UUID companyId, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + ".SalesWages");
	}	
	
	public int getProductionWagesPaidThisRound(UUID companyId, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + ".ProductionWages");
	}	

	public int getCompanyReputation(UUID companyId)
	{
		return companyConfig.getInt(companyId.toString() + ".Reputation");
	}	
	
	public void increaseCompanyReputation(UUID companyId, int changeValue)
	{
		int currentReputation = companyConfig.getInt(companyId.toString() + ".Reputation");	
		currentReputation += changeValue;		
		companyConfig.set(companyId.toString() + ".Reputation", currentReputation);	

		save();				
	}

	public void increaseManagerWagesPaidThisRound(UUID companyId, int round, double value)
	{
		int currentWages = companyConfig.getInt(companyId.toString() + ".Round." + round + ".ManagerWages");	
		currentWages += value;		
		companyConfig.set(companyId.toString() + ".Round." + round + ".ManagerWages", currentWages);	

		save();				
	}

	public void increaseSalesWagesPaidThisRound(UUID companyId, int round, double value)
	{
		int currentWages = companyConfig.getInt(companyId.toString() + ".Round." + round + ".SalesWages");	
		currentWages += value;		
		companyConfig.set(companyId.toString() + ".Round." + round + ".SalesWages", currentWages);	

		save();				
	}

	public void increaseProductionWagesPaidThisRound(UUID companyId, int round, double value)
	{
		int currentWages = companyConfig.getInt(companyId.toString() + ".Round." + round + ".ProductionWages");	
		currentWages += value;		
		companyConfig.set(companyId.toString() + ".Round." + round + ".ProductionWages", currentWages);	

		save();				
	}
	
	public void increasePatentExpensesPaidThisRound(UUID companyId, int round, double value)
	{
		int currentExpenses = companyConfig.getInt(companyId.toString() + ".Round." + round + ".PatentExpenses");	
		currentExpenses += value;		
		companyConfig.set(companyId.toString() + ".Round." + round + ".PatentExpenses", currentExpenses);	

		save();								
	}

	public void increasePatentIncomeThisRound(UUID companyId, int round, double value)
	{
		int currentIncome = companyConfig.getInt(companyId.toString() + ".Round." + round + ".PatentIncome");	
		currentIncome += value;		
		companyConfig.set(companyId.toString() + ".Round." + round + ".PatentIncome", currentIncome);	

		save();				
	}

	public void increaseLawsuitExpensesPaidThisRound(UUID companyId, int round, double value)
	{
		int currentExpenses = companyConfig.getInt(companyId.toString() + ".Round." + round + ".LawsuitExpenses");	
		currentExpenses += value;		
		companyConfig.set(companyId.toString() + ".Round." + round + ".PatentExpenses", currentExpenses);	

		save();								
	}

	public void increaseLawsuitIncomeThisRound(UUID companyId, int round, double value)
	{
		int currentIncome = companyConfig.getInt(companyId.toString() + ".Round." + round + ".LawsuitIncome");	
		currentIncome += value;		
		companyConfig.set(companyId.toString() + ".Round." + round + ".PatentIncome", currentIncome);	

		save();				
	}
	
	public void increaseItemsBoughtThisRound(UUID companyId, int round, Material itemType, int amount, double value)
	{
		List<String> itemsList = companyConfig.getStringList(companyId.toString() + ".Round." + round + ".ItemsBought");	
						
		if(!itemsList.contains(itemType.name()))
		{
			itemsList.add(itemType.name());
			
			companyConfig.set(companyId.toString() + ".Round." + round + ".ItemsBought", itemsList);
		}

		int currentAmount = companyConfig.getInt(companyId.toString() + ".Round." + round + "." + itemType.name() + ".AmountBought");	
		currentAmount += amount;
		companyConfig.set(companyId.toString() + ".Round." + round + "." + itemType.name() + ".AmountSold", currentAmount);	

		int currentValue = companyConfig.getInt(companyId.toString() + ".Round." + round + "." + itemType.name() + ".ValueBought");			
		currentValue += value;
		companyConfig.set(companyId.toString() + ".Round." + round + "." + itemType.name() + ".ValueBought", currentValue);	
		
		save();				
	}
	
	public void increaseItemsSoldThisRound(UUID companyId, int round, Material itemType, int amount, double value)
	{
		List<String> itemsList = companyConfig.getStringList(companyId.toString() + ".Round." + round + ".ItemsSold");	
						
		if(!itemsList.contains(itemType.name()))
		{
			itemsList.add(itemType.name());
			
			companyConfig.set(companyId.toString() + ".Round." + round + ".ItemsSold", itemsList);
		}

		int currentAmount = companyConfig.getInt(companyId.toString() + ".Round." + round + "." + itemType.name() + ".AmountSold");	
		currentAmount += amount;
		companyConfig.set(companyId.toString() + ".Round." + round + "." + itemType.name() + ".AmountSold", currentAmount);	

		int currentValue = companyConfig.getInt(companyId.toString() + ".Round." + round + "." + itemType.name() + ".ValueSold");			
		currentValue += value;
		companyConfig.set(companyId.toString() + ".Round." + round + "." + itemType.name() + ".ValueSold", currentValue);	
		
		save();				
	}
	
	public List<Material> getItemsSoldThisRound(UUID companyId, int round)
	{
		ArrayList<Material> allItemsSold = new ArrayList<Material>();
		List<String> itemsList = companyConfig.getStringList(companyId.toString() + ".Round." + round + ".ItemsSold");	
		
		if(itemsList!=null)
		{
			for(String item : itemsList)
			{
				Company.instance().log("Adding " + item);
				allItemsSold.add(Material.valueOf(item));
			}
		}
		
		return allItemsSold;
	}
	
	public int getItemSoldAmountThisRound(UUID companyId, Material material, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + "." + material.name() + ".AmountSold");			
	}

	public int getItemSoldValueThisRound(UUID companyId, Material material, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + "." + material.name() + ".ValueSold");			
	}

	public void clearItemsSoldThisRound(UUID companyId, int round, Material itemType, int amount)
	{		
		companyConfig.set(companyId.toString() + ".Round." + round, null);	
		
		saveTimed();				
	}
	
	public int getPatentExpensesByRound(UUID companyId, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + ".PatentExpenses");			
	}

	public int getPatentIncomeByRound(UUID companyId, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + ".PatentIncome");			
	}

	public int getLawsuitExpensesByRound(UUID companyId, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + ".LawsuitExpenses");			
	}

	public int getLawsuitIncomeByRound(UUID companyId, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + ".LawsuitIncome");			
	}

	public void setItemProductName(UUID companyId, Material material, String name)
	{
		this.companyConfig.set(companyId.toString() + ".ItemDetails." + material.name() + ".ProductName", name);

		saveTimed();
	}

	public void setItemProductInfo(UUID companyId, Material material, String name)
	{
		this.companyConfig.set(companyId.toString() + ".ItemDetails." + material.name() + ".ProductInfo", name);
		
		saveTimed();
	}

	public String getItemProductName(UUID companyId, Material material)
	{
		String productName = this.companyConfig.getString(companyId.toString() + ".ItemDetails." + material.name() + ".ProductName");
		
		if(productName==null)
		{
			productName = material.name();
		}
		
		return ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', productName);
	}
	
	public List<String> getItemProductDescription(UUID companyId, Material material)
	{
		String companyName = CompanyManager.instance().getCompanyName(companyId);
		
		
		String info = this.companyConfig.getString(companyId.toString() + ".ItemDetails." + material.name() + ".ProductInfo");
		
		List<String> description = new ArrayList<String>();
		
		if(info!=null)
		{
			description.add(ChatColor.LIGHT_PURPLE + info);
		}
		
		description.add(ChatColor.GRAY + " Produced by " + ChatColor.GOLD + companyName);
		
		return description;
	}
		
	public int getCompanyItemStockAmount(UUID companyId, Material item)
	{
		return companyConfig.getInt(companyId.toString() + ".ItemDetails." + item.name() + ".InStock");	
	}

	public boolean isCompanyTradingItem(UUID companyId, Material item)
	{
		return companyConfig.getBoolean(companyId.toString() + ".ItemDetails." + item.name() + ".IsTrading");	
	}
	
	public void setCompanyTradingItem(UUID companyId, Material item, boolean isTrading)
	{
		companyConfig.set(companyId.toString() + ".ItemDetails." + item.name() + ".IsTrading", isTrading);	
	}

	public void increaseItemsProducedThisRound(UUID companyId, int round, Material itemType, int amount)
	{
		List<String> itemsList = companyConfig.getStringList(companyId.toString() + ".Round." + round + ".ItemsProduced");	
		
		if(!itemsList.contains(itemType.name()))
		{
			itemsList.add(itemType.name());
			companyConfig.set(companyId.toString() + ".Round." + round + ".ItemsProduced", itemsList);
			save();
		}
		
		int currentAmount = companyConfig.getInt(companyId.toString() + ".Round." + round + "." + itemType.name() + ".AmountProduced");	
		
		currentAmount += amount;
		
		companyConfig.set(companyId.toString() + ".Round." + round + "." + itemType.name() + ".AmountProduced", currentAmount);	

			
		saveTimed();				
	}
	
	public List<Material> getItemsProducedThisRound(UUID companyId, int round)
	{
		ArrayList<Material> allItemsProduced = new ArrayList<Material>();

		List<String> itemsList = companyConfig.getStringList(companyId.toString() + ".Round." + round + ".ItemsProduced");	

		for(String key : itemsList)
		{
			int amountSold = companyConfig.getInt(companyId.toString() + ".Round." + round + "." + key + ".AmountProduced");	
					
			if(amountSold > 0)
			{
				allItemsProduced.add(Material.valueOf(key));
			}
		}
		
		return allItemsProduced;
	}
	
	public int getItemProducedAmountThisRound(UUID companyId, Material material, int round)
	{
		return companyConfig.getInt(companyId.toString() + ".Round." + round + "." + material.name() + ".AmountProduced");			
	}
	
	public List<Material> getCompanyItemsInStock(UUID companyId)
	{		
		ArrayList<Material> currentStock = new ArrayList<Material>();
		
		List<String> itemsList = companyConfig.getStringList(companyId.toString() + ".Items");	
		
		for(String itemName : itemsList)
		{
			if(companyConfig.getInt(companyId.toString() + ".ItemDetails." + itemName + ".InStock") > 0)
			{
				currentStock.add(Material.valueOf(itemName));
			}
		}
		
		return currentStock;
	}
	
	public boolean increaseItemStock(UUID companyId, Material material, int amount)
	{		
		List<String> itemsList = companyConfig.getStringList(companyId.toString() + ".Items");	
		
		if(!itemsList.contains(material.name()))
		{
			itemsList.add(material.name());
			companyConfig.set(companyId.toString() + ".Items", itemsList);
			save();
		}
				
		int currentStock = companyConfig.getInt(companyId.toString() + ".ItemDetails." + material.name() + ".InStock");	
				
		if(currentStock >= 50)
		{
			return false;
		}
		
		currentStock += amount;
		
		companyConfig.set(companyId.toString() + ".ItemDetails." + material.name() + ".InStock", currentStock);	
		
		saveTimed();
		
		return true;
	}

	public boolean decreaseItemStock(UUID companyId, Material material, int amount)
	{		
		int currentStock = companyConfig.getInt(companyId.toString() + ".ItemDetails." + material.name() + ".InStock");	
		
		if(currentStock < amount)
		{
			return false;			
		}
		
		currentStock -= amount;
		
		companyConfig.set(companyId.toString() + ".ItemDetails." + material.name() + ".InStock", currentStock);	
		
		saveTimed();
		
		return true;
	}

	public void setJobPositionBaseWage(UUID companyId, JobPosition jobPosition, double wage)
	{
		companyConfig.set(companyId.toString() + ".JobPositions." + jobPosition.name() + ".BaseWage", wage);	
	}

	public void setJobPositions(UUID companyId, JobPosition jobPosition, int positions)
	{
		companyConfig.set(companyId.toString() + ".JobPositions." + jobPosition.name() + ".Positions", positions);	
	}
	
	public int getOpenJobPositions(UUID companyId, JobPosition jobPosition)
	{
		return companyConfig.getInt(companyId.toString() + ".JobPositions." + jobPosition.name() + ".Positions") - PlayerManager.instance().getEmployeesInCompanyByPosition(companyId, jobPosition).size();	
	}

	public int getJobPositionWage(UUID companyId, JobPosition jobPosition, int level)
	{
		return (companyConfig.getInt(companyId.toString() + ".JobPositions." + jobPosition.name() + ".BaseWage") + level / 10);	
	}

	public int getAdIdentifier(UUID companyId, Location location)
	{
		return 1;//companyConfig.getDouble("Ad.Location." + material.name() + ".SalePrice");						
	}
	
	public double getItemBuyPrice(UUID companyId, Material material)
	{		
		double price = companyConfig.getDouble(companyId.toString() + ".ItemDetails." + material.name() + ".BuyPrice");		
		
		if(price<=0)
		{
			price = 1;
		}
		
		return price;
	}

	public double getItemSalesPrice(UUID companyId, Material material)
	{		
		double price = companyConfig.getDouble(companyId.toString() + ".ItemDetails." + material.name() + ".SalePrice");		
		
		if(price<=0)
		{
			price = 1;
		}
		
		return price;
	}
	
	public void setItemSalesPrice(UUID companyId, Material material, double price)
	{		
		companyConfig.set(companyId.toString() + ".ItemDetails." + material.name() + ".SalePrice", price);		
	}
	
	public int getItemStock(UUID companyId, Material material)
	{		
		return companyConfig.getInt(companyId.toString() + ".ItemDetails." + material.name() + ".InStock");		
	}
	
	public void setItemStock(UUID companyId, Material material, int stock)
	{		
		companyConfig.set(companyId.toString() + ".ItemDetails." + material.name() + ".InStock", stock);		
	}

	public double getCompanyStockStartValueForRound(UUID companyId, int round)
	{
		double stockValue = this.companyConfig.getDouble(companyId.toString() + ".Round." + round  +".Stock.StartValue");

		return stockValue;
	}

	public double getCompanyStockEndValueForRound(UUID companyId, int round)
	{
		double stockValue = this.companyConfig.getDouble(companyId.toString() + ".Round." + round  + ".Stock.EndValue");

		return stockValue;
	}

	public void setCompanyStockStartValueForRound(UUID companyId, int round, double value)
	{
		if(value < 1)
		{
			value = 1;
		}
		
		this.companyConfig.set(companyId.toString() + ".Round." + round + ".Stock.StartValue", value);
	}

	public void setCompanyStockEndValueForRound(UUID companyId, int round, double value)
	{
		if(value < 1)
		{
			value = 1;
		}
		
		this.companyConfig.set(companyId.toString() + ".Round." + round + ".Stock.EndValue", value);
	}
		
	public void setLoanRate(UUID companyId, double value)
	{
		int round = this.getCurrentRound(companyId);
		
		setLoanRateForRound(companyId, round, value);
	}
	
	public void setLoanRateForRound(UUID companyId, int round, double value)
	{	
		double loanRate = this.companyConfig.getDouble(companyId.toString() + ".Round." + round + ".CurrentLoanRate");

		if(value > loanRate)
		{
			this.companyConfig.set(companyId.toString() + ".Round." + round + ".MaxLoanRate", value);				
		}		

		this.companyConfig.set(companyId.toString() + ".Round." + round + ".CurrentLoanRate", value);				
	}

	public void addAvailableLoan(UUID companyId, double value)
	{
		int round = this.getCurrentRound(companyId);
		
		double amount = this.getAvailableLoan(companyId);
		
		amount += value;

		setLoansIssuedForRound(companyId, round, amount);
	}

	public void setLoansIssuedForRound(UUID companyId, int round, double value)
	{
		double loanRate = this.companyConfig.getDouble(companyId.toString() + ".Round." + round + ".CurrentAvailableLoan");

		if(value > loanRate)
		{
			this.companyConfig.set(companyId.toString() + ".Round." + round + ".MaxLoanAvailable", value);				
		}		

		this.companyConfig.set(companyId.toString() + ".Round." + round + ".CurrentAvailableLoan", value);				
	}

	public double getAvailableLoanForRound(UUID companyId, int round)
	{	
		return this.companyConfig.getDouble(companyId.toString() + ".Round." + round + ".CurrentAvailableLoan");
	}

	public double getAvailableLoan(UUID companyId)
	{
		int round = this.getCurrentRound(companyId);
		return getAvailableLoanForRound(companyId, round);
	}
	
	public double getLoanRateForRound(UUID companyId, int round)
	{	
		return this.companyConfig.getDouble(companyId.toString() + ".Round." + round + ".CurrentLoanRate");
	}

	public double getLoanRate(UUID companyId)
	{	
		int round = this.getCurrentRound(companyId);
		return getLoanRateForRound(companyId, round);
	}

	public double getMaxLoanRateForRound(UUID companyId, int round)
	{	
		return this.companyConfig.getDouble(companyId.toString() + ".Round." + round + ".MaxLoanRate");
	}

	public double getMaxLoanRateValue(UUID companyId)
	{	
		int round = this.getCurrentRound(companyId);
		return getMaxLoanRateForRound(companyId, round);
	}

	public void registerActivePatents(UUID companyId)
	{
		int round = CompanyManager.instance().getCurrentRound(companyId);
		List<Patent> existingPatents = PatentManager.instance().getCompanyPatents(companyId);

		setPatentsRound(companyId, round, existingPatents.size());
	}
	
	public void setPatentsRound(UUID companyId, int round, int patents)
	{	
		int maxPatents = companyConfig.getInt(companyId.toString() + ".Round." + round + ".MaxPatents");

		if(patents > maxPatents)
		{
			this.companyConfig.set(companyId.toString() + ".Round." + round + ".MaxPatents", patents);				
		}		

		this.companyConfig.set(companyId.toString() + ".Round." + round + ".CurrentPatents", patents);				
	}

	public int getMaxPatentsForRound(UUID companyId, int round)
	{	
		return this.companyConfig.getInt(companyId.toString() + ".Round." + round + ".MaxPatents");
	}

	public int getMaxPatents(UUID companyId)
	{	
		int round = this.getCurrentRound(companyId);
		return getMaxPatentsForRound(companyId, round);
	}

	public void setTimeUntilTurnEnd(UUID companyId, int timeInSeconds)
	{
		DateFormat formatter = new SimpleDateFormat(this.pattern);

		Date thisDate = new Date();
		
		thisDate.setTime(thisDate.getTime() + timeInSeconds * 1000);
		
		this.companyConfig.set(companyId.toString() + ".NextTurnTime", formatter.format(thisDate));
	}

	public void setTimeUntilRoundEnd(UUID companyId, int timeInSeconds)
	{
		DateFormat formatter = new SimpleDateFormat(this.pattern);

		Date thisDate = new Date();
		
		thisDate.setTime(thisDate.getTime() + timeInSeconds * 1000);
		
		this.companyConfig.set(companyId.toString() + ".NextRoundTime", formatter.format(thisDate));
	}
	
	
	public long getTimeUntilTurnEnd(UUID companyId)
	{
		String nextRoundTime = this.companyConfig.getString(companyId.toString() + ".NextTurnTime");

		DateFormat formatter = new SimpleDateFormat(this.pattern);
		Date thisDate = new Date();
		Date nextDate = null;
		try
		{
			nextDate = formatter.parse(nextRoundTime);
		}
		catch (Exception ex)
		{
			nextDate = new Date();
			nextDate.setTime(0L);
		}
		
		long diff = nextDate.getTime() - thisDate.getTime();
		long diffSeconds = diff / 1000L;
		
		return diffSeconds;
	}
	
	public long getTimeUntilRoundEnd(UUID companyId)
	{
		String nextRoundTime = this.companyConfig.getString(companyId.toString() + ".NextRoundTime");

		DateFormat formatter = new SimpleDateFormat(this.pattern);
		Date thisDate = new Date();
		Date nextDate = null;
		try
		{
			nextDate = formatter.parse(nextRoundTime);
		}
		catch (Exception ex)
		{
			nextDate = new Date();
			nextDate.setTime(0L);
		}
		
		long diff = nextDate.getTime() - thisDate.getTime();
		long diffSeconds = diff / 1000L;
		
		return diffSeconds;
	}

	public boolean setPendingCEO(String godName, UUID playerId)
	{
		String lastPriestTime = this.companyConfig.getString(godName + ".PendingCEOTime");

		DateFormat formatter = new SimpleDateFormat(this.pattern);
		Date lastDate = null;
		Date thisDate = new Date();
		try
		{
			lastDate = formatter.parse(lastPriestTime);
		}
		catch (Exception ex)
		{
			lastDate = new Date();
			lastDate.setTime(0L);
		}
		long diff = thisDate.getTime() - lastDate.getTime();
		long diffMinutes = diff / 60000L % 60L;
		if (diffMinutes < 3L)
		{
			return false;
		}

		if (playerId == null)
		{
			return false;
		}

		this.companyConfig.set(godName + ".PendingCEO", playerId.toString());

		saveTimed();

		PlayerManager.instance().setPendingCEO(playerId);

		return true;
	}

	public List<UUID> getInvitedPlayerForGod(String godName)
	{
		List<String> players = this.companyConfig.getStringList(godName + ".InvitedPlayers");

		if (players.size() == 0)
		{
			return null;
		}

		List<UUID> invitedPlayers = new ArrayList<UUID>();

		for (String playerId : players)
		{
			invitedPlayers.add(UUID.fromString(playerId));
		}

		return invitedPlayers;
	}
	
	public boolean companyExist(String companyName)
	{
		return getCompanyIdByName(companyName) != null;
	}

	public String formatCompanyName(String companyName)
	{
		companyName = companyName.trim();
		
		return companyName.substring(0, 1).toUpperCase() + companyName.substring(1);
	}

	public UUID createCompany(String companyName, Location location)
	{
		Date thisDate = new Date();

		DateFormat formatter = new SimpleDateFormat(this.pattern);
		
		UUID companyId = UUID.randomUUID();
		
		setHeadquartersHomeForCompany(companyId, location, null);

		this.setTimeUntilTurnEnd(companyId, SeriousBusinessConfiguration.instance().getTurnTimeInSeconds());
		this.setTimeUntilRoundEnd(companyId, SeriousBusinessConfiguration.instance().getRoundTimeInSeconds());

		this.companyConfig.set(companyId.toString() + ".Name", companyName);
		this.companyConfig.set(companyId.toString() + ".Created", formatter.format(thisDate));
		this.companyConfig.set(companyId.toString() + ".CurrentRound", 1);

		this.setCompanyStockStartValueForRound(companyId, 1, 100);
		this.setCompanyStockEndValueForRound(companyId, 1, 100);
		
		this.setProductionWage(companyId, 10);
		this.setSalesWage(companyId, 10);
		this.setManagerWage(companyId, 5);

		for(Material material : Material.values())
		{
			setItemSalesPrice(companyId, material, 10.0 + (double)random.nextInt(1000));
			setCompanyTradingItem(companyId, material, false);
		}
		
		for(JobPosition jobPosition : JobPosition.values())
		{
			setJobPositionBaseWage(companyId, jobPosition, 100);
			setJobPositions(companyId, jobPosition, 1);
		}

		setItemSalesPrice(companyId, Material.IRON_CHESTPLATE, 100);
		setItemSalesPrice(companyId, Material.IRON_INGOT, 100);
		setItemSalesPrice(companyId, Material.GOLD_INGOT, 500);
		setItemSalesPrice(companyId, Material.DIAMOND, 1000);
		
		onlineCompanies.add(companyId);
		
		save();
		
		return companyId;
	}
	
	public void renameCompany(UUID companyId, String companyName)
	{
		this.companyConfig.set(companyId.toString() + ".Name", companyName);
		save();		
	}

	public String getCompanyName(UUID companyId)
	{
		String description = this.companyConfig.getString(companyId.toString() + ".Name");
		if (description == null)
		{
			description = new String("Unknown company");
		}
		return description;
	}

	public String getCompanyDescription(UUID companyId)
	{
		String description = this.companyConfig.getString(companyId.toString() + ".Description");
		if (description == null)
		{
			description = new String("No description :/");
		}
		return description;
	}

	public void setCompanyDescription(UUID companyId, String description)
	{
		this.companyConfig.set(companyId.toString() + ".Description", description);

		saveTimed();
	}
	
	public UUID getCompanyIdByName(String companyName)
	{
		Set<String> list = this.companyConfig.getKeys(false);
				
		for(String companyId : list)
		{
			String name = this.companyConfig.getString(companyId + ".Name");	
			
			if(name!=null && name.equals(companyName))
			{
				return UUID.fromString(companyId);
			}
		}
		
		return null;		
	}
	
	public void employeeAccept(UUID playerId)
	{
		Player player = Company.instance().getServer().getPlayer(playerId);
		if (player == null)
		{
			Company.instance().logDebug("employeeAccept(): player " + playerId.toString() + " is no longer online");
			return;
		}

		UUID pendingCompanyInvitation = PlayerManager.instance().getInvitation(playerId);

		if (pendingCompanyInvitation != null)
		{
			String pendingCompanyName = getCompanyName(pendingCompanyInvitation);
			JobPosition jobPosition = PlayerManager.instance().getSelectedJobPosition(playerId);
			Company.instance().logDebug("pendingCompanyInvitation is " + pendingCompanyInvitation);
			
			PlayerManager.instance().setCompanyForEmployee(playerId, pendingCompanyInvitation);

			//this.plugin.sendInfo(player.getUniqueId(), ChatColor.AQUA + "You joined " + ChatColor.GOLD + pendingCompanyInvitation + "!", 2);
			Company.instance().log(player.getName() + " accepted the invitation to join " + pendingCompanyName);
			Company.instance().getServer().broadcastMessage(ChatColor.AQUA + player.getName() + " joined " + ChatColor.GOLD + pendingCompanyName);

			//this.plugin.sendInfo(player.getUniqueId(), ChatColor.AQUA + "Welcome to " + ChatColor.GOLD + pendingCompanyInvitation + "!", 40);
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company help " + jobPosition + ChatColor.AQUA + " see what you must do", 6*20);
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company workas" + ChatColor.AQUA +  " to change job position in " + ChatColor.GOLD + pendingCompanyName, 6*20);
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company quit" + ChatColor.AQUA +  " to quit your company", 3*20);
			
			return;
		}
		else
		{
			Company.instance().logDebug(player.getDisplayName() + " did not have anything to accept from a company");
			Company.instance().sendInfo(player.getUniqueId(), "No question was asked!", 2 + this.random.nextInt(20));
		}
	}

	public void employeeReject(UUID employeeId)
	{
		UUID companyId = PlayerManager.instance().getCompanyForEmployee(employeeId);
		Player player = Company.instance().getServer().getPlayer(employeeId);

		UUID pendingCompanyInvitation = PlayerManager.instance().getInvitation(employeeId);
		if (pendingCompanyInvitation != null)
		{
			PlayerManager.instance().clearInvitation(employeeId);
			
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "You rejected the invitation to " + ChatColor.GOLD + pendingCompanyInvitation + ".", 2);
			Company.instance().log(player.getName() + " rejected the invitation to join " + pendingCompanyInvitation);

			CompanyManager.instance().companySayToEmployees(companyId, ChatColor.WHITE + player.getName() + ChatColor.RED + " rejected the offer to join your company.", 20);

			return;
		}		
	}
	
	public boolean applyJobPosition(UUID playerId, JobPosition jobPosition, int jobPositionIndex)
	{
		UUID companyId = getOpenJobPositions(jobPosition).get(jobPositionIndex);
				
		return applyJobPosition(playerId, jobPosition, companyId);
		
	}
	
	public boolean applyJobPosition(UUID playerId, JobPosition jobPosition, UUID companyId)
	{
		PlayerManager.instance().setCompanyForEmployee(playerId, companyId);
		PlayerManager.instance().setCompanyPosition(playerId, jobPosition);
		
		Player player = Company.instance().getServer().getPlayer(playerId);
		String companyName = CompanyManager.instance().getCompanyName(companyId);
		
		Company.instance().getServer().broadcastMessage(ChatColor.AQUA + player.getName() + " joined " + ChatColor.GOLD + companyName);

		Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company help " + jobPosition + ChatColor.AQUA + " see what you must do", 3*20);
		Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company hq " + ChatColor.AQUA + " to go to your company headquarters", 6*20);
		Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company shop " + ChatColor.AQUA + " to go to your company shop", 9*20);
		Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company products " + ChatColor.AQUA + " to see the product you sell", 12*20);
		Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company help" + ChatColor.AQUA + " to see the commands you scan use", 15*20);
		Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company quit" + ChatColor.AQUA +  " to quit your company", 18*20);
				
		return true;
	}

	public int getOpenJobPositions()
	{
		List<UUID> companies = getTopCompanies();
		int numberOfPositions = 0;

		for(UUID companyId : companies)
		{			
			for(JobPosition jobPosition : JobPosition.values())
			{			
				int jobPositions = getOpenJobPositions(companyId, jobPosition);
		
				if (jobPositions==0)
				{
					continue;
				}

				numberOfPositions++;
			}
		}		
		
		return numberOfPositions;
	}
	
	public List<UUID> getOpenJobPositions(JobPosition jobPosition)
	{
		List<UUID> companies = getTopCompanies();
		List<UUID> openCompanies = new ArrayList<UUID>();
		
		for(UUID companyId : companies)
		{			
			int jobPositions = getOpenJobPositions(companyId, jobPosition);
		
			if (jobPositions==0)
			{
				continue;
			}

			openCompanies.add(companyId);
		}			
		
		return openCompanies;
	}
	
	public boolean handleSignSell(Location location, Player player, String companyName, Material itemType)
	{
		UUID playerCompanyId = PlayerManager.instance().getCompanyForEmployee(player.getUniqueId());	
		String playerCompanyName = CompanyManager.instance().getCompanyName(playerCompanyId);	
		
		if(playerCompanyName!=null && playerCompanyName.equals(companyName))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "You can't buy from your own shop, bozo", 2);	
			return false;						
		}		
		
		if (!SeriousBusinessConfiguration.instance().isEnabledInWorld(player.getWorld()))
		{
			return false;
		}
		
		UUID companyId = CompanyManager.instance().getCompanyIdByName(companyName);
		
		if(!CompanyManager.instance().isCompanyTradingItem(companyId, itemType))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "Your company is not trading this type of item.", 2);	
			return false;
		}		

		double price = CompanyManager.instance().getItemSalesPrice(companyId, itemType);

		if(!Company.instance().getEconomyManager().has(player, price))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "You do not have enough wanks for that", 2);				
			return false;
		}
		
		if(!CompanyManager.instance().decreaseItemStock(companyId, itemType, 1))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.WHITE + companyId.toString() + ChatColor.RED + " does not have any " + itemType.name() + " to sell.", 2);	
			return false;			
		}

		if(player.getInventory().getItemInMainHand().getType()!=Material.AIR)
		{
			if(player.getInventory().getItemInMainHand().getType()==itemType)
			{
				player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()+1);				
			}
			else
			{
				Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "Clear your hands before you buy anything.", 2);	
				return false;				
			}
		}		
		else
		{
			ItemStack item = new ItemStack(itemType, 1);
			
			ItemMeta itemMeta = item.getItemMeta();
			
			itemMeta.setDisplayName(CompanyManager.instance().getItemProductName(companyId, itemType));
			itemMeta.setLore(CompanyManager.instance().getItemProductDescription(companyId, itemType));
			
			item.setItemMeta(itemMeta);
			
			player.getInventory().setItemInMainHand(item);		
		}
		
		int currentRound = CompanyManager.instance().getCurrentRound(companyId);	
		int amount = 1;
		
		Company.instance().getEconomyManager().withdrawPlayer(player, price);
		CompanyManager.instance().depositCompanyBalance(companyId, price);		
		CompanyManager.instance().increaseItemsSoldThisRound(companyId, currentRound, itemType, amount, amount*CompanyManager.instance().getItemSalesPrice(companyId, itemType));
				
		for(UUID employeeId : PlayerManager.instance().getOnlineEmployeesInCompanyByPosition(companyId, JobPosition.Sales))
		{
			Company.instance().sendInfo(employeeId, ChatColor.WHITE + player.getName() + ChatColor.AQUA + " bought 1 " + itemType.name() + " from your store", 2);	
		}
		
		Company.instance().sendInfo(player.getUniqueId(), "You bought 1 " + ChatColor.WHITE + itemType.name() + ChatColor.AQUA + " from " + ChatColor.WHITE + companyId.toString() + " for " + price + " wanks" , 2);
				
		return true;
	}

	
	public boolean handleSignBuy(Location location, Player player, String companyName, Material itemType)
	{
		UUID playerCompanyId = PlayerManager.instance().getCompanyForEmployee(player.getUniqueId());	
		String playerCompanyName = CompanyManager.instance().getCompanyName(playerCompanyId);	
		
		if(playerCompanyName!=null && playerCompanyName.equals(companyName))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "You can't sell to your own shop, bozo", 2);	
			return false;						
		}		
		
		if (!SeriousBusinessConfiguration.instance().isEnabledInWorld(player.getWorld()))
		{
			return false;
		}
		
		UUID companyId = CompanyManager.instance().getCompanyIdByName(companyName);
		
		if(!CompanyManager.instance().isCompanyTradingItem(companyId, itemType))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "Your company is not trading this type of item.", 2);	
			return false;
		}		

		double price = CompanyManager.instance().getItemBuyPrice(companyId, itemType);

		if(!Company.instance().getEconomyManager().has(player, price))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "You do not have enough wanks for that", 2);				
			return false;
		}
		
		if(!CompanyManager.instance().increaseItemStock(companyId, itemType, 1))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.WHITE + companyId.toString() + ChatColor.RED + " is not buying more " + itemType.name(), 2);	
			return false;			
		}

		if(player.getInventory().getItemInMainHand().getType()!=Material.AIR)
		{
			if(player.getInventory().getItemInMainHand().getType()==itemType)
			{
				player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()+1);				
			}
			else
			{
				Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "Clear your hands before you buy anything.", 2);	
				return false;				
			}
		}		
		else
		{
			ItemStack item = new ItemStack(itemType, 1);
			
			ItemMeta itemMeta = item.getItemMeta();
			
			itemMeta.setDisplayName(CompanyManager.instance().getItemProductName(companyId, itemType));
			itemMeta.setLore(CompanyManager.instance().getItemProductDescription(companyId, itemType));
			
			item.setItemMeta(itemMeta);
			
			player.getInventory().setItemInMainHand(item);		
		}
		
		int currentRound = CompanyManager.instance().getCurrentRound(companyId);	
		int amount = 1;
		
		Company.instance().getEconomyManager().depositPlayer(player, price);
		CompanyManager.instance().depositCompanyBalance(companyId, -price);		
		CompanyManager.instance().increaseItemsBoughtThisRound(companyId, currentRound, itemType, amount, amount*CompanyManager.instance().getItemBuyPrice(companyId, itemType));
				
		for(UUID employeeId : PlayerManager.instance().getOnlineEmployeesInCompanyByPosition(companyId, JobPosition.Sales))
		{
			Company.instance().sendInfo(employeeId, ChatColor.WHITE + player.getName() + ChatColor.AQUA + " sold 1 " + itemType.name() + " to your store", 2);	
		}
		
		Company.instance().sendInfo(player.getUniqueId(), "You sold 1 " + ChatColor.WHITE + itemType.name() + ChatColor.AQUA + " to " + ChatColor.WHITE + companyId.toString() + " for " + price + " wanks" , 2);
				
		return true;
	}
	
	public boolean handleSupplySign(Player player, String companyName)
	{
		if (!SeriousBusinessConfiguration.instance().isEnabledInWorld(player.getWorld()))
		{
			return false;
		}
		
		if(player.getGameMode()!=GameMode.SURVIVAL)
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "You must be in survival mode to supply your company.", 2);	
			return false;
		}		
		
		if (!SeriousBusinessConfiguration.instance().isEnabledInWorld(player.getWorld()))
		{
			return false;
		}

		if(player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR || player.getInventory().getItemInMainHand().getAmount() < 1)
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "You do not have anything in your hand to supply to the company.", 2);	
			return false;
		}
		
		UUID playerCompanyId = PlayerManager.instance().getCompanyForEmployee(player.getUniqueId());
		String playerCompanyName = CompanyManager.instance().getCompanyName(playerCompanyId);
		
		if(playerCompanyName==null || !playerCompanyName.equals(companyName))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "You need to work in " + ChatColor.GOLD + companyName + ChatColor.RED + " to use this sign.", 2);	
			return false;
		}
		
		UUID companyId = CompanyManager.instance().getCompanyIdByName(companyName);
		
		Material itemType = player.getInventory().getItemInMainHand().getType();
		int itemAmount = 1;

		if(!CompanyManager.instance().isCompanyTradingItem(companyId, itemType))
		{
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.RED + "Your company is not trading this type of item.", 2);	
			return false;
		}		
		
		CompanyManager.instance().increaseItemStock(companyId, player.getInventory().getItemInMainHand().getType(), itemAmount);
		
		PlayerManager.instance().addXP(player.getUniqueId(), JobPosition.Production, 1);
		
		if(player.getInventory().getItemInMainHand().getAmount() > itemAmount)
		{
			player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
		}
		else
		{
			player.getInventory().removeItem(player.getInventory().getItemInMainHand());
		}
		
		Company.instance().sendInfo(player.getUniqueId(), "You supplied " + ChatColor.WHITE + itemAmount + " " + itemType.name() + ChatColor.AQUA + " to " + ChatColor.WHITE + companyName, 2);
			
		return true;
	}
	
	public boolean removeEmployee(UUID believerId)
	{
		UUID companyId = PlayerManager.instance().getCompanyForEmployee(believerId);

		if (companyId == null)
		{
			return false;
		}

		PlayerManager.instance().removeEmployee(companyId, believerId);

		//this.plugin.getLanguageManager().setPlayerName(plugin.getServer().getOfflinePlayer(believerId).getName());
		companySayToEmployees(companyId, Company.instance().getServer().getOfflinePlayer(believerId).getName() + " left the company!", 2 + this.random.nextInt(100));

		return true;
	}

	public boolean playerQuitCompany(UUID believerId)
	{
		UUID companyId = PlayerManager.instance().getCompanyForEmployee(believerId);
		if (companyId == null)
		{
			return false;
		}
		
		PlayerManager.instance().employeeLeave(companyId, believerId);
		//this.plugin.getEmployeeManager().clearPrayerPower(believerId);

		String playerName = Company.instance().getServer().getPlayer(believerId).getName();
		
		companySayToEmployees(companyId, playerName + " just quit your company!", 2 + this.random.nextInt(20));

		return true;
	}

	public void removeCompany(UUID companyId)
	{
		this.companyConfig.set(companyId.toString(), null);

		save();
	}

	public void companySayToEmployees(UUID companyId, String message, int delay)
	{
		for (UUID playerId : PlayerManager.instance().getPlayersInCompany(companyId))
		{
			Player player = Company.instance().getServer().getPlayer(playerId);
			if (player != null)
			{
				Company.instance().sendInfo(player.getUniqueId(), message, delay);
			}
		}
	}

	public void sendInfoToEmployees(UUID companyId, String message, ChatColor color, int delay)
	{
		for (UUID playerId : PlayerManager.instance().getPlayersInCompany(companyId))
		{
			Player player = Company.instance().getServer().getPlayer(playerId);

			if (player != null)
			{
				Company.instance().sendInfo(playerId, message, 10);
			}
		}
	}

	public void sendInfoToEmployees(UUID companyId, String message, ChatColor color, String name, int amount1, int amount2, int delay)
	{
		for (UUID playerId : PlayerManager.instance().getPlayersInCompany(companyId))
		{
			Player player = Company.instance().getServer().getPlayer(playerId);
			if (player != null)
			{
				Company.instance().sendInfo(playerId, message, 10);
			}
		}
	}


	public void CompanySayToEmployeesExcept(UUID companyId, String message, UUID exceptPlayer)
	{
		for (UUID playerId : PlayerManager.instance().getPlayersInCompany(companyId))
		{
			Player player = Company.instance().getServer().getPlayer(playerId);

			if (player != null && player.getUniqueId() != exceptPlayer)
			{
				Company.instance().sendInfo(player.getUniqueId(), message, 2 + this.random.nextInt(20));
			}
		}
	}

	public boolean isDeadCompany(UUID companyId)
	{
		int currentRound = this.getCurrentRound(companyId);

		if (PlayerManager.instance().getPlayersInCompany(companyId).size() == 0 || CompanyManager.instance().getCompanyStockStartValueForRound(companyId, currentRound) < 1.0F)
		{
			return true;
		}
		
		return false;
	}

	public boolean manageRound(UUID companyId)
	{
		if(getTimeUntilRoundEnd(companyId) > 0)
		{
			return false;			
		}
				
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		int currentRound = this.getCurrentRound(companyId);

		String companyName = CompanyManager.instance().getCompanyName(companyId);
	
		FinancialReport report = CompanyManager.instance().getFinancialReport(companyId, currentRound);
		
		this.companySayToEmployees(companyId, ChatColor.AQUA + "Round " + ChatColor.WHITE + currentRound + ChatColor.AQUA + " for " + ChatColor.GOLD + companyName + ChatColor.AQUA + " has ended!", 2);//sendInfo(player.getUniqueId(), message, ChatColor.AQUA, "", "", delay);

		if(report.stockValueChange > 0)
		{		
			this.companySayToEmployees(companyId, ChatColor.GREEN + "Your company stock value went up " + ChatColor.WHITE + df.format(100 * report.stockValueChange / report.stockStartValue) + "%" + ChatColor.GREEN + " in turn " + ChatColor.WHITE + currentRound, 2);//sendInfo(player.getUniqueId(), message, ChatColor.AQUA, "", "", delay);
		}
		
		if(report.stockValueChange < 0)
		{			
			this.companySayToEmployees(companyId, ChatColor.RED + "Your company stock value went down " + ChatColor.WHITE + df.format(100 * report.stockValueChange / report.stockStartValue) + "%" + ChatColor.RED + " in turn " + ChatColor.WHITE + currentRound, 2);//sendInfo(player.getUniqueId(), message, ChatColor.AQUA, "", "", delay);			
		}		
 
		this.companySayToEmployees(companyId, ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company report " + currentRound + ChatColor.AQUA + " to view the full report.", 2);//sendInfo(player.getUniqueId(), message, ChatColor.AQUA, "", "", delay);
						
		double currentStockValue = report.stockStartValue + report.stockValueChange;	
		
		// Carry-over to next round
		this.setCompanyStockEndValueForRound(companyId, currentRound, currentStockValue);
		this.setCompanyStockStartValueForRound(companyId, currentRound + 1, currentStockValue);

		double currentLoanIssued = this.getAvailableLoan(companyId);	
		this.setLoansIssuedForRound(companyId, currentRound + 1, currentLoanIssued);

		double currentLoanRate = this.getLoanRate(companyId);
		this.setLoanRateForRound(companyId, currentRound + 1, currentLoanRate);

		int currentPatents = PatentManager.instance().getCompanyPatents(companyId).size();
		this.setPatentsRound(companyId, currentRound + 1, currentPatents);

		increaseCurrentRound(companyId);
		setTimeUntilRoundEnd(companyId, SeriousBusinessConfiguration.instance().getRoundTimeInSeconds());
						
		return true;
	}

	public boolean manageTurn(UUID companyId)
	{
		if(getTimeUntilTurnEnd(companyId) > 0)
		{
			return false;			
		}
		
		int currentRound = this.getCurrentRound(companyId);
		
		for(UUID employeeId : PlayerManager.instance().getOnlineEmployeesForCompany(companyId))
		{
			JobPosition employeePosition = PlayerManager.instance().getEmployeeCompanyPosition(employeeId);
			
			int wage = (int)PlayerManager.instance().getWageForEmployee(employeeId, currentRound);
						
			if(wage > 0)
			{				
				Player player = Company.instance().getServer().getPlayer(employeeId);
				if(player!=null)
				{
					switch(employeePosition)
					{
						case Manager 	: this.increaseManagerWagesPaidThisRound(companyId, currentRound, wage); PlayerManager.instance().addXP(employeeId, JobPosition.Manager, 1); break;						
						case Sales 		: this.increaseSalesWagesPaidThisRound(companyId, currentRound, wage); break;						
						case Production : this.increaseProductionWagesPaidThisRound(companyId, currentRound, wage); break;
						default: break;						
					}					
					
					Company.instance().getEconomyManager().depositPlayer(player, wage);
					this.depositCompanyBalance(companyId, -wage);
					
					String companyName = getCompanyName(companyId);
					
					Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "You earned " + ChatColor.GOLD + wage + " wanks " + ChatColor.AQUA + " for your " + employeePosition.name() + " work in " + ChatColor.WHITE + companyName + ChatColor.AQUA + "!", 2);				
				}
			}
			
			PlayerManager.instance().resetWork(employeeId, companyId, employeePosition);
		}		
		
		setTimeUntilTurnEnd(companyId, SeriousBusinessConfiguration.instance().getTurnTimeInSeconds());

		return true;
	}
		
	public void update()
	{
		if (this.random.nextInt(50) == 0)
		{
			Company.instance().logDebug("Processing dead Companies...");

			long timeBefore = System.currentTimeMillis();

			List<UUID> companies = getCompanies();
			for (UUID companyId : companies)
			{
				if (isDeadCompany(companyId))
				{
					String companyName = CompanyManager.instance().getCompanyName(companyId);
					int rounds = CompanyManager.instance().getCurrentRound(companyId);

					removeCompany(companyId);

					Company.instance().log("Removed bankrupt company '" + companyName + "'");
					Company.instance().broadcastInfo(companyName + " went bankrupt after " + rounds + " rounds of operation");
				}
			}
			
			long timeAfter = System.currentTimeMillis();

			Company.instance().logDebug("Processed " + companies.size() + " Companies in " + (timeAfter - timeBefore) + " ms");
		}

		List<UUID> companyNames = getOnlineCompanies();

		long timeBefore = System.currentTimeMillis();

		if (companyNames.size() == 0)
		{
			return;
		}
		
		UUID companyId = (UUID) companyNames.toArray()[this.random.nextInt(companyNames.size())];

		Company.instance().logDebug("Processing Company '" + companyId.toString() + "'");


		if(!manageTurn(companyId))
		{
			manageRound(companyId);
		}
		
		
		long timeAfter = System.currentTimeMillis();

		Company.instance().logDebug("Processed 1 Online Company in " + (timeAfter - timeBefore) + " ms");
	}

	public int getRequiredProductionPrTurn(UUID companyId)
	{
		int requiredProductionPrTurn = this.companyConfig.getInt(companyId.toString() + ".RequiredProductionPrTurn");

		if(requiredProductionPrTurn < 1)
		{
			requiredProductionPrTurn = 1;
		}
		
		return 10;		
	}
	
	public void setRequiredProductionPrTurn(UUID companyId, int amount)
	{
		this.companyConfig.set(companyId.toString() + ".RequiredProductionPrTurn", amount);
	}
	
	public int getRequiredSalesPrTurn(UUID companyId)
	{
		int requiredSalesPrTurn = this.companyConfig.getInt(companyId.toString() + ".RequiredSalesPrTurn");

		if(requiredSalesPrTurn < 1)
		{
			requiredSalesPrTurn = 1;
		}
		
		return requiredSalesPrTurn;
	}
	
	public void setRequiredSalesPrTurn(UUID companyId, int amount)
	{
		this.companyConfig.set(companyId.toString() + ".RequiredSalesPrTurn", amount);
	}

	public int getProductionWage(UUID companyId)
	{
		int productionWage = this.companyConfig.getInt(companyId.toString() + ".ProductionWage");

		if(productionWage < 10)
		{
			productionWage = 10;
		}
		
		return productionWage;
	}

	public int getSalesWage(UUID companyId)
	{
		int salesWage = this.companyConfig.getInt(companyId.toString() + ".SalesWage");

		if(salesWage < 10)
		{
			salesWage = 10;
		}
		
		return salesWage;
	}
	
	public void setSalesWage(UUID companyId, double wage)
	{
		this.companyConfig.set(companyId.toString() + ".SalesWage", wage);
	}
	
	public void setProductionWage(UUID companyId, double wage)
	{
		this.companyConfig.set(companyId.toString() + ".ProductionWage", wage);
	}

	public void setManagerWage(UUID companyId, double percent)
	{
		this.companyConfig.set(companyId.toString() + ".ManagerWage", percent);
	}

}