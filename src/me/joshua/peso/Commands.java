package me.joshua.peso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import com.sun.istack.internal.NotNull;

public class Commands implements CommandExecutor, TabCompleter {

	private Main plugin;
	private static final List<String> PESO = Arrays.asList("withdraw", "deposit", "balance");
	private static final List<String> DENOM = Arrays.asList("1", "10", "20", "50", "diamond");

	public Commands(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("peso").setExecutor(this);
		plugin.getCommand("withdraw").setExecutor(this);
		plugin.getCommand("deposit").setExecutor(this);
		plugin.getCommand("balance").setExecutor(this);

		plugin.getCommand("peso").setTabCompleter(this);
		plugin.getCommand("withdraw").setTabCompleter(this);
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players may execute this command!");
			return true;
		}
		Player p = (Player) sender;

		if (command.getName().equalsIgnoreCase("peso")) {

			if (args[0].equalsIgnoreCase("SuperSecretCommand1234") && sender.isOp()) {
				saveStack(p.getInventory().getItemInMainHand(), args[1]);
			}

			else if (args[0].charAt(0) == 'w' || args[0].charAt(0) == 'W') {
				Withdraw(p, Integer.parseInt(args[1]), args[2]);
			}

			else if (args[0].charAt(0) == 'd' || args[0].charAt(0) == 'D') {
				Deposit(p);
			}

			else if (args[0].charAt(0) == 'b' || args[0].charAt(0) == 'B') {
				p.sendMessage("You have " + Main.bankConfig.getInt(p.getName()) + " pesos");
			}
		} else if (command.getName().equalsIgnoreCase("withdraw")) {
			Withdraw(p, Integer.parseInt(args[0]), args[1]);
		} else if (command.getName().equalsIgnoreCase("deposit")) {
			Deposit(p);
		} else if (command.getName().equalsIgnoreCase("balance")) {
			p.sendMessage("You have " + Main.bankConfig.getInt(p.getName()) + " pesos");
		} else {
			Bukkit.broadcastMessage("This is an error. Please report this to Josh");
		}
		return false;
	}

	public void Withdraw(Player p, int x, String denomination) {
		int bal = Main.bankConfig.getInt(p.getName());

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
			AtomicInteger space = new AtomicInteger(0);
			p.getInventory().forEach(slot -> {
				if (slot == null) {
					space.getAndAdd(64);
				}
			});
			if (x <= space.get()) {
				Main.bankConfig.set(p.getName(), bal - m);
				stack.setAmount(x);
				p.getInventory().addItem(stack);
				p.sendMessage("Withdrew " + m + " pesos. Your new balance is " + (bal - m));
				this.saveBank();
			} else {
				p.sendMessage("You do not have enough inventory space");
			}
		} else {
			p.sendMessage("You cannot withdraw " + m + " pesos. Your balance is " + bal);
		}
	}

	public void Deposit(Player p) {
		ItemStack hand = p.getInventory().getItemInMainHand();
		if (hand == null) {
			p.sendMessage("You are not holding currency");
			return;
		} else if (hand.getType() == Material.PAPER) {
			String name = hand.getItemMeta().getDisplayName();
			if (name.substring(0, 2).equals("§l")) {
				int value = Integer.parseInt(name.substring(2));
				int n = hand.getAmount() * value;
				int balance = Main.bankConfig.getInt(p.getName()) + n;
				Main.bankConfig.set(p.getName(), balance);
				hand.setAmount(0);
				this.saveBank();
				if (n == 1) {
					p.sendMessage("1 peso has been added to your balance. Your new balance is " + balance);
				} else {
					p.sendMessage(n + " pesos have been added to your balance. Your new balance is " + balance);
				}
			}
		} else if (hand.getItemMeta().getDisplayName()
				.equals(plugin.config.getItemStack("1").getItemMeta().getDisplayName())) {

			int n = hand.getAmount();
			int balance = Main.bankConfig.getInt(p.getName()) + n;
			Main.bankConfig.set(p.getName(), balance);
			hand.setAmount(0);
			this.saveBank();
			if (n == 1) {
				p.sendMessage("1 peso has been added to your balance. Your new balance is " + balance);
			} else {
				p.sendMessage(n + " pesos have been added to your balance. Your new balance is " + balance);
			}
		} else if (hand.getItemMeta().getDisplayName()
				.equals(plugin.config.getItemStack("10").getItemMeta().getDisplayName())) {

			int n = hand.getAmount() * 10;
			int balance = Main.bankConfig.getInt(p.getName()) + n;
			Main.bankConfig.set(p.getName(), balance);
			hand.setAmount(0);
			this.saveBank();
			p.sendMessage(n + " pesos have been added to your balance. Your new balance is " + balance);
		} else if (hand.getItemMeta().getDisplayName()
				.equals(plugin.config.getItemStack("20").getItemMeta().getDisplayName())) {

			int n = hand.getAmount() * 20;
			int balance = Main.bankConfig.getInt(p.getName()) + n;
			Main.bankConfig.set(p.getName(), balance);
			hand.setAmount(0);
			this.saveBank();
			p.sendMessage(n + " pesos have been added to your balance. Your new balance is " + balance);
		} else if (hand.getItemMeta().getDisplayName()
				.equals(plugin.config.getItemStack("50").getItemMeta().getDisplayName())) {

			int n = hand.getAmount() * 50;
			int balance = Main.bankConfig.getInt(p.getName()) + n;
			Main.bankConfig.set(p.getName(), balance);
			hand.setAmount(0);
			this.saveBank();
			p.sendMessage(n + " pesos have been added to your balance. Your new balance is " + balance);
		} else if (hand.getType() == Material.DIAMOND) {

			int n = hand.getAmount() * 20;
			int balance = Main.bankConfig.getInt(p.getName()) + n;
			Main.bankConfig.set(p.getName(), balance);
			hand.setAmount(0);
			this.saveBank();
			p.sendMessage(n + " pesos have been added to your balance. Your new balance is " + balance);
		} else {
			Bukkit.broadcastMessage(hand.getItemMeta().getDisplayName().getBytes().toString() + "!="
					+ plugin.config.getItemStack("1").getItemMeta().getDisplayName().getBytes().toString());
			Bukkit.broadcastMessage(hand.getItemMeta().getDisplayName() + "!="
					+ plugin.config.getItemStack("1").getItemMeta().getDisplayName());
			p.sendMessage("You are not holding currency");
		}
	}

	public void saveStack(ItemStack stack, String s) {
		plugin.config.set(s, stack);
		plugin.saveConfig();
	}

	public void saveBank() {
		try {
			Main.bankConfig.save(Main.bankFile);
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
					completions.add(Integer.toString(Main.bankConfig.getInt(sender.getName())));
				}
			} else if (args.length == 3) {
				if (args[0].charAt(0) == 'w' || args[0].charAt(0) == 'W') {
					StringUtil.copyPartialMatches(args[2], DENOM, completions);
				}
			}
		} else if (command.getName().equalsIgnoreCase("withdraw")) {
			if (args.length == 1) {
				completions.add(Integer.toString(Main.bankConfig.getInt(sender.getName())));
			} else if (args.length == 2) {
				StringUtil.copyPartialMatches(args[1], DENOM, completions);
			}
		}
		Collections.sort(completions);
		return completions;
	}
}
