package com.dogonfire.seriousbusiness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;

import com.dogonfire.seriousbusiness.CompanyManager.JobPosition;
import com.dogonfire.seriousbusiness.commands.CourtCaseType;



public class CourtManager
{
	private static CourtManager			instance;
	private Random						random				= new Random();
	private Queue<CourtCase>			playerCases 		= new LinkedList<CourtCase>(); // TODO: Should be a queue
	private int courtCaseid = 1;
	private HashMap<UUID, UUID>			playerLawsuitCompanies = new HashMap<UUID, UUID>();
	
	final public class CourtCase
	{
		final public int Id;
		final public UUID playerId;
		final public UUID companyId;
		final public CourtCaseType caseType;
		final public String description;
		public int bribesGuilty;
		public int bribesNotGuilty;
		
		CourtCase(int id, CourtCaseType caseType, UUID playerId, UUID companyId, String description)		
		{
			this.Id = id;
			this.companyId = companyId;
			this.playerId = playerId;
			this.caseType = caseType;
			this.description = description;
			this.bribesGuilty = 0;
			this.bribesNotGuilty = 0;
		}		
	}
	
	CourtManager()
	{
		instance = this;		
	}

	static public CourtManager instance()
	{
		return instance;
	}
		
	public void setPlayerLawsuitCompany(UUID playerId, UUID companyId)
	{
		playerLawsuitCompanies.put(playerId, companyId);
	}

	
	public UUID getPlayerLawsuitCompany(UUID playerId)
	{		
		return playerLawsuitCompanies.get(playerId);
	}

	public Object[] getCases()
	{	
		return playerCases.toArray();
	}
	
	public CourtCase getCaseById(int caseId)
	{
		for(CourtCase courtCase : playerCases)
		{			
			if(courtCase.Id == caseId)
			{
				return courtCase;
			}
		}
		
		return null;
	}
		
	public String getCaseTypeDescription(CourtCaseType caseType)
	{
		switch(caseType)
		{
		case Spamming : return "Spamming";
		case SalesTaxFraud : return "Sales tax fraud";
		case StockManipulation : return "Stock manipulation";
		case LoanSharking : return "Loan sharking";
		case TaxAvoidance : return "Tax avoidance";
		case TradingIllegalItems : return "Trading illegal Items";	
		default : break;
		}
		
		return "UNKNOWN";
	}
	
	public void removePlayerLawsuitCompany(UUID playerId)
	{
		this.playerLawsuitCompanies.remove(playerId);		
	}
	
	public void bribeGuilty(int caseId, int amount)
	{
		for(CourtCase courtCase : playerCases)
		{
			if(courtCase.Id == caseId)
			{
				courtCase.bribesGuilty += amount;
			}
		}		
	}
	
	public void bribeNotGuilty(int caseId, int amount)
	{
		for(CourtCase courtCase : playerCases)
		{
			if(courtCase.Id == caseId)
			{
				courtCase.bribesNotGuilty += amount;
			}
		}		
	}

	// Players can randomly (without actual knowledge) fire court cases against companies and hope that they will actually hit criminal behaviour
	public int applyCase(CourtCaseType caseType, UUID playerId, UUID companyId, String description)
	{
		// Check whether player case exist, too many, irrelevant and other reasons to reject the case
		for(CourtCase courtCase : playerCases)
		{			
			if(courtCase.playerId.equals(playerId))
			{
				if(courtCase.caseType == caseType && courtCase.companyId.equals(companyId))
				{
					return 0;
				}				
			}
		}
		
		PlayerManager.instance().addXP(playerId, JobPosition.Law, 1);
		
		return createCase(caseType, playerId, companyId, description);
	}	
	
	public int createCase(CourtCaseType caseType, UUID playerId, UUID companyId, String description)
	{
		CourtCase courtCase = new CourtCase(courtCaseid++, caseType, playerId, companyId, description);
		playerCases.add(courtCase);
					
		String companyName = CompanyManager.instance().getCompanyName(companyId);
		Company.instance().getServer().broadcastMessage(ChatColor.GOLD + Company.instance().getServer().getPlayer(playerId).getDisplayName() + ChatColor.AQUA + " filed a lawsuit against " + ChatColor.GOLD + companyName + ChatColor.AQUA + " for " + ChatColor.GOLD + courtCase.description + "!");
		
		return courtCaseid - 1;
	}

	private void decideNotGuilty(CourtCase courtCase, int amount)
	{
		String companyName = CompanyManager.instance().getCompanyName(courtCase.companyId);
		String playerName = Company.instance().getServer().getOfflinePlayer(courtCase.playerId).getName();
		
		Company.instance().getServer().broadcastMessage(ChatColor.AQUA + "In the case #" + courtCase.Id + ": " + playerName + " vs " + companyName + ":");		
		Company.instance().getServer().broadcastMessage(ChatColor.AQUA + "The Court ruled " + companyName + ChatColor.GREEN + " NOT GUILTY" + ChatColor.AQUA + " of " + courtCase.description + "!");		
		Company.instance().getServer().broadcastMessage(ChatColor.AQUA + companyName + " was given " + ChatColor.GOLD + amount + " wanks " + ChatColor.AQUA + "as compensation for emotional damage!");		

		int repuationChange = 1;

		CompanyManager.instance().increaseCompanyReputation(courtCase.companyId, repuationChange);
		CompanyManager.instance().sendInfoToEmployees(courtCase.companyId, "Your company reputation was increased by " + repuationChange, ChatColor.GREEN, 1);
		
		//CompanyManager.instance().depositCompanyBalance(courtCase.companyId, amount);
	}
	
	private void decideGuilty(CourtCase courtCase, int amount)
	{
		if(amount > CompanyManager.instance().getBalance(courtCase.companyId))
		{
			amount = (int)CompanyManager.instance().getBalance(courtCase.companyId);
		}

		String companyName = CompanyManager.instance().getCompanyName(courtCase.companyId);
		String playerName = Company.instance().getServer().getOfflinePlayer(courtCase.playerId).getName();
		
		Company.instance().getServer().broadcastMessage(ChatColor.AQUA + "In the case #" + courtCase.Id + ": " + playerName + " vs " + companyName + ":");		
		Company.instance().getServer().broadcastMessage(ChatColor.AQUA + "The Court ruled " + companyName + ChatColor.RED + "" + "	GUILTY" + ChatColor.AQUA + " of " + courtCase.description + "!");		
		Company.instance().getServer().broadcastMessage(ChatColor.AQUA + companyName + " was fined " + ChatColor.GOLD + amount + " wanks!");
	
		int repuationChange = -1;
		
		CompanyManager.instance().increaseCompanyReputation(courtCase.companyId, repuationChange);
		CompanyManager.instance().sendInfoToEmployees(courtCase.companyId, "Your company reputation was decreased by " + (-repuationChange), ChatColor.RED, 1);

		//CompanyManager.instance().depositCompanyBalance(courtCase.companyId, -amount);
		//Company.instance().getEconomyManager().depositPlayer(player, amount);
	}

	public int getGuiltyProbability(CourtCase courtCase)
	{
		double lawsuitBribePrPercentage = SeriousBusinessConfiguration.instance().getLawsuitBribePrPercentage();
		double guiltyProbability = 40.0 + courtCase.bribesGuilty / lawsuitBribePrPercentage - courtCase.bribesNotGuilty / lawsuitBribePrPercentage;
		
		if(guiltyProbability > 100)
		{
			guiltyProbability = 100;
		}
		
		if(guiltyProbability < 0)
		{
			guiltyProbability = 0;
		}

		return (int)guiltyProbability;
	}
	
	public void update()
	{
		if (this.random.nextInt(500) == 0)
		{
			Company.instance().logDebug("Processing court cases...");

			// Decide on 1 court case at a time. Let players wait for court decisions
			// Evaluate company actions during the last 5 turns/rounds
			CourtCase courtCase = playerCases.poll();
					
			if(courtCase!=null)
			{						
				int guiltyProbability = getGuiltyProbability(courtCase); 
				
				if((1 + random.nextInt(100)) > guiltyProbability)
				{
					int amount = SeriousBusinessConfiguration.instance().getCourtCaseCost();
					decideNotGuilty(courtCase, amount);
				}
				else
				{
					int amount = 10 + (int)(CompanyManager.instance().getBalance(courtCase.companyId) * SeriousBusinessConfiguration.instance().getLawsuitFinePercentage() / 100.0);
														
					decideGuilty(courtCase, amount);					
				}			
			}
		}	
	}
}