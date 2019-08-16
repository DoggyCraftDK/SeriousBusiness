package com.dogonfire.seriousbusiness.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dogonfire.seriousbusiness.Company;
import com.dogonfire.seriousbusiness.CompanyManager;


public class CommandShopSearch extends SeriousBusinessCommand
{
	protected CommandShopSearch()
	{
		super("search");
		this.permission = "shop.search";
	}

	@Override
	public void onCommand(CommandSender sender, String command, String... args)
	{
		Player player = (Player)sender;

		if (!sender.isOp() && !Company.instance().getPermissionsManager().hasPermission((Player) sender, "company.shop.search"))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return;
		}
				
		List<UUID> topCompanies = CompanyManager.instance().getTopCompanies();
		
		if (topCompanies.size() == 0)
		{
			Company.instance().log(ChatColor.RED + "There are no companies in " + Company.instance().serverName + "!");
			return;
		}
				
		try
		{
			Material selectedMaterial = Material.getMaterial(args[1]);
		
			int n = 1;
		
			for (UUID companyId : topCompanies)
			{		
				int stock = Company.instance().getCompanyManager().getCompanyItemStockAmount(companyId, selectedMaterial);
				//if (stock > 0)
				//{
					player.sendMessage(ChatColor.YELLOW + "" + n + ") " + Company.instance().getCompanyManager().getCompanyName(companyId) + " For sale: " + stock);
					n++;				
				//}
			}

			if(n==1)
			{
				Company.instance().log(ChatColor.RED + "No shop is selling" + selectedMaterial.name());
			}
		}
		catch(Exception ex)
		{
			Company.instance().log(ChatColor.RED + "That is not a valid item type");			
			Company.instance().sendInfo(player.getUniqueId(), "", 3*20);
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Example item name: '" + ChatColor.WHITE + Material.DIAMOND_SWORD + ChatColor.AQUA + "'", 3*20);
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Example item name '" + ChatColor.WHITE + Material.CAKE + ChatColor.AQUA + "'", 3*20);
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Example item name '" + ChatColor.WHITE + Material.PUMPKIN + ChatColor.AQUA + "'", 3*20);
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "Example item name '" + ChatColor.WHITE + Material.OAK_LOG + ChatColor.AQUA + "'", 3*20);
			Company.instance().sendInfo(player.getUniqueId(), "", 3*20);
			Company.instance().sendInfo(player.getUniqueId(), ChatColor.AQUA + "See full list of item/material names at " + ChatColor.WHITE + "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html" + ChatColor.AQUA + "", 3*20);
		}
	}
}