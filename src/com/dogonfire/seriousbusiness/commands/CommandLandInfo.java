package com.dogonfire.seriousbusiness.commands;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dogonfire.seriousbusiness.Company;
import com.dogonfire.seriousbusiness.CompanyManager;
import com.dogonfire.seriousbusiness.LandManager;
import com.dogonfire.seriousbusiness.LandManager.LandReport;



public class CommandLandInfo extends SeriousBusinessCommand
{
	protected CommandLandInfo()
	{
		super("info");
		this.permission = "land.info";
	}

	@Override
	public void onCommand(CommandSender sender, String command, String... args)
	{
		Player player = (Player) sender;
		String landName = null;
		LandReport report = null;

		if (args.length > 1)
		{
			landName = args[1];
			
			for(int a=2; a<args.length; a++)
			{
				landName += " " + args[a];
			}
		}
		
		if(landName==null)
		{
			report = LandManager.instance().getLandReport(player.getLocation());
		}
		else
		{
			long landId = LandManager.instance().getLandIdByName(landName);			
			report = LandManager.instance().getLandReport(landId);
		}

		sender.sendMessage(ChatColor.YELLOW + "Land information:");
		sender.sendMessage(ChatColor.YELLOW + "  Name: " + ChatColor.AQUA + report.name);

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		if (report.companyTaxValueChange > 0)
		{
			sender.sendMessage(ChatColor.YELLOW + "  Company Tax: " + ChatColor.AQUA + df.format(report.companyTaxEndValue) + "%" + ChatColor.RED + "   +" + df.format(100 * (report.companyTaxValueChange / report.companyTaxStartValue)) + "%");
		}
		else if (report.companyTaxValueChange == 0)
		{
			sender.sendMessage(ChatColor.YELLOW + "  Company Tax: " + ChatColor.AQUA + df.format(report.companyTaxEndValue) + "%" + ChatColor.WHITE + "   " + df.format(100 * (report.companyTaxValueChange / report.companyTaxStartValue)) + "%");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + "  Company Tax: " + ChatColor.AQUA + df.format(report.companyTaxEndValue) + "%" + ChatColor.GREEN + "   " + df.format(100 * (report.companyTaxValueChange / report.companyTaxStartValue)) + "%");
		}

		if (report.salesTaxValueChange > 0)
		{
			sender.sendMessage(ChatColor.YELLOW + "  Sales Tax: " + ChatColor.AQUA + df.format(report.salesTaxEndValue) + "%" + ChatColor.RED + "   +" + df.format(100 * (report.salesTaxValueChange / report.salesTaxStartValue)) + "%");
		}
		else if (report.salesTaxValueChange == 0)
		{
			sender.sendMessage(ChatColor.YELLOW + "  Sales Tax: " + ChatColor.AQUA + df.format(report.salesTaxEndValue) + "%" + ChatColor.WHITE + "   " + df.format(100 * (report.salesTaxValueChange / report.salesTaxStartValue)) + "%");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + "  Sales Tax: " + ChatColor.AQUA + df.format(report.salesTaxEndValue) + "%" + ChatColor.YELLOW + "   " + ChatColor.GREEN + df.format(100 * (report.salesTaxValueChange / report.salesTaxStartValue)) + "%");
		}

		if (report.incomeTaxValueChange > 0)
		{
			sender.sendMessage(ChatColor.YELLOW + "  Income Tax: " + ChatColor.AQUA + df.format(report.incomeTaxEndValue) + "%" + ChatColor.RED + "   +" + df.format(100 * (report.incomeTaxValueChange / report.incomeTaxStartValue)) + "%");
		}
		else if (report.incomeTaxValueChange == 0)
		{
			sender.sendMessage(ChatColor.YELLOW + "  Income Tax: " + ChatColor.AQUA + df.format(report.incomeTaxEndValue) + "%" + ChatColor.WHITE + "   " + df.format(100 * (report.incomeTaxValueChange / report.incomeTaxStartValue)) + "%");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + "  Income Tax: " + ChatColor.AQUA + df.format(report.incomeTaxEndValue) + "%" + ChatColor.YELLOW + "   " + ChatColor.GREEN + df.format(100 * (report.incomeTaxValueChange / report.incomeTaxStartValue)) + "%");
		}

		sender.sendMessage(ChatColor.YELLOW + "  Max loan rate: " + ChatColor.AQUA + df.format(report.maxLoanRateValue) + "%");
		sender.sendMessage(ChatColor.YELLOW + "  Patent fee: " + ChatColor.AQUA + df.format(report.maxChatPrMinute) + "%");
		sender.sendMessage(ChatColor.YELLOW + "  Max chat pr minute: " + ChatColor.AQUA + df.format(report.maxChatPrMinute));

		List<UUID> companies = LandManager.instance().getCompanies(player.getLocation());

		if (companies != null && companies.size() > 0)
		{
			sender.sendMessage(ChatColor.YELLOW + "Companies:");

			for (UUID landCompanyId : companies)
			{
				sender.sendMessage(ChatColor.YELLOW + "  " + CompanyManager.instance().getCompanyName(landCompanyId));
			}
		}

		Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company people" + ChatColor.AQUA + " to view the people employed by a company", 40);
		Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Use " + ChatColor.WHITE + "/company report" + ChatColor.AQUA + " to view the latest financial report for the company", 80);
	}
}
