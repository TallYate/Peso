package me.joshua.peso;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Msg {
	
	public static void noInventorySpace(Player p) {
		err(p, "You do not have enough inventory space");
	}
	public static void cantAfford(Player p, Shop shop) {
		err(p, "You do not have " + ChatColor.GREEN + shop.price + ChatColor.RED + " pesos");
	}
	public static void priceNotSpecified(Player p) {
		err(p, "You did not specify a price");
	}
	public static void noHopperSteal(Player p) {
		err(p, "You cannot place a container under a different player's hopper chest");
	}
	public static void noDoubleChest(Player p) {
		err(p, "PesoShops cannot be double chests");
	}
	
	
	
	//	green = money
	//	gold = playerName
	public static void purchasedStack(Player p, ItemStack stack, Shop shop) {
		String itemName;
		if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
			itemName = stack.getItemMeta().getDisplayName();
		}
		else {
			itemName = stack.getType().toString();
		}
		info(p, "You purchased " + itemName + ChatColor.BLUE + " x"
				+ stack.getAmount() + ChatColor.GRAY + " for " + ChatColor.GREEN + shop.price + ChatColor.GRAY
				+ " pesos from " + ChatColor.GOLD + shop.owner);
	}
	public static void newBalance(Player p, PesoPlugin plugin) {
		info(p, "Your new balance is " + ChatColor.GREEN + plugin.bankConfig.getInt(p.getName()) + ChatColor.GRAY
				+ " pesos");
	}
	public static void removedShop(Player p, Shop shop) {
		info(p, "You " + ChatColor.RED + "removed " + ChatColor.GRAY + "your "
				+ ChatColor.GREEN + shop.price + ChatColor.GRAY + " peso PesoShop");
	}
	public static void removedShopAdmin(Player p, String owner, Shop shop) {
		info(p, "You " + ChatColor.RED + "removed " + ChatColor.GOLD + owner + ChatColor.GRAY
			+ "'s " + ChatColor.GREEN + shop.price + ChatColor.GRAY
			+ " peso PesoShop using §4§lADMIN OVERRIDE");
	}
	public static void placedShop(Player p, int price) {
		info(p, "You placed a " + ChatColor.GOLD + "PesoShop" + ChatColor.GRAY
				+ " with a price of " + ChatColor.GREEN + price + ChatColor.GRAY + " pesos per slot");
	}
	public static void openedShop(Player p, Shop shop, PesoPlugin plugin) {
		String msg = "You opened ";
		if (shop.owner.equalsIgnoreCase(p.getName())) {
			msg += "your own shop";
		} else {
			msg += "§2§l" + shop.owner + "§r's shop";
		}

		if (plugin.ADMINS.contains(p.getName())) {
			msg += " in §4§lADMIN OVERRIDE§r mode";
		}
		info(p, msg);
	}
	public static void claimedShop(Player p, int price) {
		info(p, "You claimed a PesoShop with a price of " + ChatColor.GREEN + price + ChatColor.GRAY + " pesos per slot");
	}
	
	
	
	public static void err(Player p, String s) {
		msg(p, ChatColor.RED + s + "!");
	}
	public static void info(Player p, String s) {
		msg(p, ChatColor.GRAY + s);
	}
	public static void msg(Player p, String s) {
		p.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "Pesos" + ChatColor.DARK_GRAY + "] " + s);
	}
	
}
