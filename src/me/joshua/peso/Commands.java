package me.joshua.peso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

public class Commands implements CommandExecutor, TabCompleter {

	private PesoPlugin plugin;
	private static final List<String> PESO = Arrays.asList("withdraw", "deposit", "balance", "helpbook");

	public Commands(PesoPlugin plugin) {
		this.plugin = plugin;
		plugin.getCommand("peso").setExecutor(this);
		plugin.getCommand("withdraw").setExecutor(this);
		plugin.getCommand("deposit").setExecutor(this);
		plugin.getCommand("balance").setExecutor(this);

		plugin.getCommand("peso").setTabCompleter(this);
		plugin.getCommand("withdraw").setTabCompleter(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players may execute this command!");
			return true;
		}
		Player p = (Player) sender;

		if (command.getName().equalsIgnoreCase("peso")) {
			if (args.length < 1) {
				p.sendMessage(ChatColor.RED + "The peso command requires arguments");
				return false;
			} else if (args[0].equalsIgnoreCase("helpbook") && sender.isOp()) {
				p.getInventory().addItem((ItemStack) plugin.config.get("helpbook"));
			} else if (args[0].equalsIgnoreCase("AdminSetDenomination") && p.isOp()) {
				boolean jklol = false;
				if (args.length == 3) {
					if (args[2].equals("jklol")) {
						jklol = true;
					}
				}

				p.sendMessage(ChatColor.AQUA + args[1] + ChatColor.RESET + " has been set to " + ChatColor.AQUA
						+ p.getInventory().getItemInMainHand().toString());
				Bukkit.broadcastMessage(ChatColor.RED + "§l[Alert] " + p.getName() + " has changed the "
						+ ChatColor.WHITE + args[1] + " denomination");

				if (!jklol) {
					saveStack(p.getInventory().getItemInMainHand(), args[1]);
				} else {
					p.sendMessage(ChatColor.AQUA + "don't worry you typed jklol");
				}

			} else if (args[0].equalsIgnoreCase("AdminOverride") && p.isOp()) {
				if (plugin.ADMINS.contains(p.getName())) {
					plugin.ADMINS.remove(p.getName());
					p.sendMessage("§4§lAdminOverride turned §8§loff");
				} else {
					plugin.ADMINS.add(p.getName());
					p.sendMessage("§4§lAdminOverride turned §2§lon");
				}
			} else if (args[0].charAt(0) == 'w' || args[0].charAt(0) == 'W') {
				if (args.length == 2) {
					Withdraw(p, args[1], "1");
				} else if (args.length > 2) {
					Withdraw(p, args[1], args[2]);
				}
			} else if (args[0].charAt(0) == 'd' || args[0].charAt(0) == 'D') {
				Deposit(p);
			}

			else if (args[0].charAt(0) == 'b' || args[0].charAt(0) == 'B') {
				p.sendMessage("You have " + ChatColor.GREEN + plugin.bankConfig.getInt(p.getName()) + ChatColor.RESET
						+ " pesos");
			}
		} else if (command.getName().equalsIgnoreCase("withdraw")) {
			if (args.length == 1) {
				Withdraw(p, args[0], "1");
			} else if (args.length > 1) {
				Withdraw(p, args[0], args[1]);
			}
		} else if (command.getName().equalsIgnoreCase("deposit")) {
			Deposit(p);
		} else if (command.getName().equalsIgnoreCase("balance")) {
			p.sendMessage(
					"You have " + ChatColor.GREEN + plugin.bankConfig.getInt(p.getName()) + ChatColor.RESET + " pesos");
		} else {
			Bukkit.broadcastMessage("This is an error. Please report this to TallYate");
		}
		return false;
	}

	public void Withdraw(Player p, String amount, String denomination) {
		int x;
		try {
			x = Integer.parseInt(amount);
		} catch (NumberFormatException e) {
			p.sendMessage(ChatColor.GREEN.toString() + amount + ChatColor.RED + " is not a valid number");
			return;
		}
		if (x <= 0) {
			p.sendMessage(ChatColor.GREEN.toString() + x + ChatColor.RED + " is not a valid amount");
			return;
		}
		int bal = plugin.bankConfig.getInt(p.getName());

		ItemStack stack;
		int m = 1;
		if (denomination == null) {
			stack = (ItemStack) plugin.config.get("1");
		} else if (denomination.contentEquals("10")) {
			m = 10;
			stack = (ItemStack) plugin.config.get("10");
		} else if (denomination.equals("20")) {
			m = 20;
			stack = (ItemStack) plugin.config.get("20");
		} else if (denomination.charAt(0) == 'd' || denomination.charAt(0) == 'D') {
			m = 20;
			stack = new ItemStack(Material.DIAMOND);
		} else if (denomination.equals("50")) {
			m = 50;
			stack = (ItemStack) plugin.config.get("50");
		} else {
			stack = (ItemStack) plugin.config.get("1");
		}
		m *= x;
		if (bal >= m) {
			int space = 0;
			ItemStack[] inv = p.getInventory().getStorageContents();

			for (int i = 0; i < inv.length; i++) {
				if (inv[i] == null) {
					space += 64;
				}
			}
			if (x <= space) {
				plugin.bankConfig.set(p.getName(), bal - m);
				stack.setAmount(x);
				p.getInventory().addItem(stack);
				p.sendMessage("Withdrew " + ChatColor.GREEN + m + ChatColor.RESET + " pesos. Your new balance is "
						+ ChatColor.GREEN + (bal - m));
				this.saveBank();
			} else {
				p.sendMessage(ChatColor.RED + "You do not have enough inventory space");
			}
		} else {
			p.sendMessage(ChatColor.RED + "You cannot withdraw " + ChatColor.GREEN + m + ChatColor.RED
					+ " pesos. Your balance is " + ChatColor.GREEN + bal);
		}
	}

	public void Deposit(Player p) {
		ItemStack hand = p.getInventory().getItemInMainHand();
		if (hand.getType() == Material.AIR) {
			p.sendMessage(ChatColor.RED + "You are not holding currency");
			return;
		} else if (hand.getType() == Material.PAPER) {
			String name = hand.getItemMeta().getDisplayName();
			if (name.substring(0, 2).equals("§l")) {
				int value = Integer.parseInt(name.substring(2));
				int n = hand.getAmount() * value;
				int balance = plugin.bankConfig.getInt(p.getName()) + n;
				plugin.bankConfig.set(p.getName(), balance);
				hand.setAmount(0);
				this.saveBank();
				if (n == 1) {
					p.sendMessage("§a1§r peso has been added to your balance. Your new balance is " + balance);
				} else {
					p.sendMessage(ChatColor.GREEN + Integer.toString(n) + ChatColor.RESET
							+ " pesos have been added to your balance. Your new balance is " + balance);
				}
			}
		}
		int n = hand.getAmount();
		if (hand.getType() == Material.DIAMOND) {
			n *= 20;
		} else if (!hand.hasItemMeta()) {
			p.sendMessage(ChatColor.RED + "You are not holding currency");
		} else if (hand.getItemMeta().getDisplayName()
				.equals(plugin.config.getItemStack("10").getItemMeta().getDisplayName())) {
			n *= 10;
		} else if (hand.getItemMeta().getDisplayName()
				.equals(plugin.config.getItemStack("20").getItemMeta().getDisplayName())) {
			n *= 20;
		} else if (hand.getItemMeta().getDisplayName()
				.equals(plugin.config.getItemStack("50").getItemMeta().getDisplayName())) {
			n *= 20;
		} else if (!hand.getItemMeta().getDisplayName()
				.equals(plugin.config.getItemStack("1").getItemMeta().getDisplayName())) {
			p.sendMessage(ChatColor.RED + "You are not holding currency");
			return;
		}
		int oldBalance = plugin.bankConfig.getInt(p.getName());
		if (oldBalance < 0) {
			oldBalance *= -1;
			plugin.bankConfig.set(p.getName(), oldBalance);
		}
		int newBalance = oldBalance + n;
		if (newBalance < 0) {
			p.sendMessage(ChatColor.RED + "That will cause your bank to overflow! (max is 2,147,483,647)");
			return;
		}
		plugin.bankConfig.set(p.getName(), newBalance);
		hand.setAmount(0);
		this.saveBank();
		p.sendMessage(ChatColor.GREEN.toString() + n + ChatColor.RESET + (n == 1 ? " peso" : " pesos")
				+ " has been added to your balance. Your new balance is " + ChatColor.GREEN + newBalance);
	}

	public void saveStack(ItemStack stack, String s) {
		plugin.config.set(s, stack);
		plugin.saveConfig();
	}

	public void saveBank() {
		try {
			plugin.bankConfig.save(plugin.bankFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		final List<String> completions = new ArrayList<>();
		if (command.getName().equalsIgnoreCase("peso")) {
			if (args.length == 1) {
				StringUtil.copyPartialMatches(args[0], PESO, completions);
			} else if (args.length == 2) {
				if (args[0].charAt(0) == 'w' || args[0].charAt(0) == 'W') {
					completions.add(Integer.toString(plugin.bankConfig.getInt(sender.getName())));
				}
			} else if (args.length == 3) {
				if (args[0].charAt(0) == 'w' || args[0].charAt(0) == 'W') {
					StringUtil.copyPartialMatches(args[2], getDenom(sender, args[1]), completions);
				}
			}
		} else if (command.getName().equalsIgnoreCase("withdraw")) {
			if (args.length == 1) {
				completions.add(Integer.toString(plugin.bankConfig.getInt(sender.getName())));
			} else if (args.length == 2) {
				StringUtil.copyPartialMatches(args[1], getDenom(sender, args[0]), completions);
			}
		}
		Collections.sort(completions);
		return completions;
	}

	private List<String> getDenom(CommandSender sender, int den) {

		final List<String> temp = new ArrayList<>();
		int bal = plugin.bankConfig.getInt(sender.getName());
		int big = bal / den;
		int s = 0;
		if (big >= 50) {
			s = 1;
		} else if (big >= 20) {
			s = 2;
		} else if (big >= 10) {
			s = 3;
		} else if (big >= 1) {
			s = 4;
		}

		switch (s) {
		case 1:
			temp.add("50");
		case 2:
			temp.add("20");
			temp.add("diamond");
		case 3:
			temp.add("10");
		case 4:
			temp.add("1");
			break;
		default:
			return temp;
		}
		return temp;
	}

	private List<String> getDenom(CommandSender sender, String den) {
		try {
			return getDenom(sender, Integer.parseInt(den));
		} catch (NumberFormatException e) {
			return new ArrayList<String>();
		}

	}
}
