package me.joshua.peso;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Events implements Listener {
	private Main plugin;

	public Events(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		// return if fake slot
		if (e.getSlot() < 0) {
			return;
		}

		// return if admin override
		if (plugin.ADMINS.contains(e.getWhoClicked().getName())) {
			return;
		}

		// return if fake inventory
		// essentials' /echest's inventory's holder is null so this extracheck fixes an
		// error
		if (e.getInventory().getHolder() == null || e.getInventory().getLocation() == null) {
			return;
		}
		// return if inventory is not a shop
		String loc = Shop.getLoc(e.getInventory().getLocation());
		if (!plugin.shopConfig.contains(loc)) {
			return;
		}

		Player p = (Player) e.getWhoClicked();
		Shop shop = (Shop) plugin.shopConfig.get(loc);
		int money = plugin.bankConfig.getInt(p.getName());

		// if the player is the owner, return
		if (p.getName().equalsIgnoreCase(shop.owner)) {
			return;
		}

		// prevent accidental shift clicking from player inventory to shop
		if (e.getClickedInventory().getType() == InventoryType.PLAYER && e.isShiftClick()) {
			if (plugin.shopConfig.contains(Shop.getLoc(e.getInventory().getLocation()))) {
				e.setCancelled(true);
			}
		}

		// if player is not clicking their own inventory do shop things
		if (e.getInventory() == e.getClickedInventory()) {
			if (money < shop.price) {
				noMoneyMsgIfRealClick(e.getCurrentItem().getType(), p, shop.price);
			} else {
				ItemStack cheque = createCheque(shop.price);
				boolean flag = isFull(p.getInventory().getStorageContents());
				if (flag) {
					Bukkit.getScheduler().runTask(plugin, task -> {
						p.closeInventory();
						p.sendMessage(ChatColor.RED + "You do not have enough inventory space");
					});
				} else {
					ItemStack current = e.getCurrentItem();
					if (current.getType() != Material.AIR) {
						p.getInventory().addItem(current);
						e.setCurrentItem(cheque);
						plugin.bankConfig.set(p.getName(), money - shop.price);
						purchaseMessage(p, current, shop);
					}
				}
			}
			e.setCancelled(true);
		}
	}

	private boolean isFull(ItemStack[] inv) {
		for (ItemStack stack : inv) {
			if (stack == null) {
				return false;
			}
		}
		return true;
	}

	private ItemStack createCheque(int price) {
		ItemStack cheque;
		if (price > 64) {
			cheque = new ItemStack(Material.PAPER);
			ItemMeta meta = cheque.getItemMeta();
			meta.setDisplayName("�l" + Integer.toString(price));
			cheque.setItemMeta(meta);
		} else {
			cheque = plugin.config.getItemStack("1");
			cheque.setAmount(price);
		}
		return cheque;
	}

	private void noMoneyMsgIfRealClick(Material material, Player p, int money) {

		if (material != Material.AIR) {

			p.sendMessage(ChatColor.RED + "You do not have " + ChatColor.GREEN + money + ChatColor.RED + " pesos");
			Bukkit.getScheduler().runTask(plugin, task -> {
				p.closeInventory();
			});
		}
	}

	private void purchaseMessage(Player p, ItemStack stack, Shop shop) {
		Bukkit.getScheduler().runTask(plugin, task -> {
			p.closeInventory();
		});
		if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
			p.sendMessage("You purchased " + stack.getItemMeta().getDisplayName() + ChatColor.BLUE + " x"
					+ stack.getAmount() + ChatColor.RESET + " for " + ChatColor.GREEN + shop.price + ChatColor.RESET
					+ " pesos from " + ChatColor.GOLD + shop.owner);
		} else {
			p.sendMessage("You purchased " + stack.getType().toString() + ChatColor.BLUE + " x" + stack.getAmount()
					+ ChatColor.RESET + " for " + ChatColor.GREEN + shop.price + ChatColor.RESET + " pesos from "
					+ ChatColor.GOLD + shop.owner);
		}

		p.sendMessage("Your new balance is " + ChatColor.GREEN + plugin.bankConfig.getInt(p.getName()) + ChatColor.RESET
				+ " pesos");
		plugin.saveBank();
	}

	@EventHandler
	public void onInventoryDrag(final InventoryDragEvent e) {
		if (plugin.ADMINS.contains(e.getWhoClicked().getName())) {
			return;
		}
		if (e.getInventory().getLocation() == null) {
			return;
		}

		String loc = Shop.getLoc(e.getInventory().getLocation());
		if (plugin.shopConfig.contains(loc)) {
			if (((Shop) plugin.shopConfig.get(loc)).owner.equalsIgnoreCase(e.getWhoClicked().getName())) {
				return;
			}
			for (Integer slot : e.getRawSlots()) {
				if (slot < e.getInventory().getSize()) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onOpen(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getPlayer().isSneaking()
				|| !(e.getClickedBlock().getState() instanceof Container)) {
			return;
		}

		Container container = (Container) e.getClickedBlock().getState();
		Location invLoc = container.getLocation();

		if (invLoc == null || container.getCustomName() == null) {
			return;
		}

		if (container.getCustomName().length() < 8) {
			return;
		}

		if (plugin.shopConfig.contains(Shop.getLoc(invLoc))) {
			Shop shop = (Shop) plugin.shopConfig.get(Shop.getLoc(invLoc));

			// colorify real pesoshop as green
			if (container.getCustomName().substring(0, 8).equalsIgnoreCase("PesoShop")) {
				markColor(Integer.toString(shop.price), container, e.getPlayer(), ChatColor.DARK_GREEN.toString());
				return;
			}

			// open colorified shop window if price doesn't match or price is not number
			boolean flag = false;
			try {
				int containerPrice = Integer.parseInt(container.getCustomName().substring(11));
				if (containerPrice != shop.price) {
					flag = true;
				}
			} catch (NumberFormatException exception) {
				flag = true;
			}

			if (flag) {
				plugin.getLogger().log(Level.INFO, "Config and Chest desync fixed at " + Shop.getLoc(invLoc));
				e.setCancelled(true);
				markColor(Integer.toString(shop.price), container, e.getPlayer(), ChatColor.DARK_GREEN.toString());
			}
			String msg = "You opened ";
			HumanEntity h = e.getPlayer();
			if (shop.owner.equalsIgnoreCase(h.getName())) {
				msg += "your own shop";
			} else {
				msg += "�2�l" + shop.owner + "�r's shop";
			}

			if (plugin.ADMINS.contains(h.getName())) {
				msg += " in �4�lADMIN OVERRIDE�r mode";
			}
			h.sendMessage(msg);
		}
//		fix colorified shops that are not in config
		else if (container.getCustomName().length() > 10
				&& container.getCustomName().substring(0, 10).equalsIgnoreCase(ChatColor.DARK_GREEN + "PesoShop")) {

			// this is just an extra check in case the number somehow became invalid
			String toParse = container.getCustomName().substring(11);
			int n = 0;
			try {
				n = Integer.parseInt(toParse);
			} catch (NumberFormatException ex) {
				markColor("INVALID", container, e.getPlayer(), ChatColor.DARK_RED.toString());
				return;
			}
			if (n < 1) {
				markColor("INVALID", container, e.getPlayer(), ChatColor.DARK_RED.toString());

				//
			} else {
				e.getPlayer().sendMessage("You claimed a " + ChatColor.GOLD + "PesoShop" + ChatColor.RESET
						+ " with a price of " + ChatColor.GREEN + n + ChatColor.RESET + " pesos per slot");
				plugin.shopConfig.set(Shop.getLoc(container.getLocation()), new Shop(e.getPlayer().getName(), n));
				plugin.saveShops();
			}
		}
//		mark fake shops as red (shops not in config)
		else if (container.getCustomName().substring(0, 8).equalsIgnoreCase("PesoShop")) {
			markColor("INVALID", container, e.getPlayer(), ChatColor.DARK_RED.toString());
		}
	}

	public void markColor(String price, Container container, HumanEntity human, String prepend) {
		container.setCustomName(prepend + "PesoShop " + price);
		container.update();
	}

	public static boolean startsWth(String string, String with) {
		return string.length() >= with.length() && string.substring(0, with.length()).equalsIgnoreCase(with);
	}

	@EventHandler
	public void onHopper(InventoryMoveItemEvent e) {
		if (plugin.shopConfig.contains(Shop.getLoc(e.getSource().getLocation()))) {
			e.setCancelled(true);
		} else if (plugin.shopConfig.contains(Shop.getLoc(e.getDestination().getLocation()))) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		for (int i = 0; i < e.blockList().size(); i++) {
			if (plugin.shopConfig.contains(Shop.getLoc(e.blockList().get(i).getLocation()))) {
				e.blockList().remove(i);
			}
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (p == null) {
			return;
		}
		Block block = e.getBlock();

		ItemMeta meta = e.getItemInHand().getItemMeta();
		if (!(block.getState() instanceof Container)) {
			String name = meta.getDisplayName();
			if (name.equals(plugin.config.getItemStack("1").getItemMeta().getDisplayName())
					|| name.equals(plugin.config.getItemStack("10").getItemMeta().getDisplayName())
					|| name.equals(plugin.config.getItemStack("20").getItemMeta().getDisplayName())
					|| name.equals(plugin.config.getItemStack("50").getItemMeta().getDisplayName())) {
				e.setCancelled(true);
			}
			return;
		}

		Location loc = e.getBlock().getLocation();

		if (e.getBlock().getType() == Material.CHEST || e.getBlock().getType() == Material.TRAPPED_CHEST) {
			Material type = e.getBlock().getType();
			if (isNextTo(type, loc)
					&& ((meta.hasDisplayName() && meta.getDisplayName().substring(0, 8).equalsIgnoreCase("PesoShop"))
							|| isNextToShopThatIs(type, loc))) {
				e.setCancelled(true);
				p.sendMessage(ChatColor.RED + "PesoShops cannot be double chests!");
				return;
			}
		}

		if (!(block.getState() instanceof Container)) {
			return;
		}

		String name = ((Container) e.getBlock().getState()).getCustomName();
		Bukkit.broadcastMessage(name);
		name = ((Container) block.getState()).getCustomName();
		
		if(name.length()<5) {
			return;
		}
		if(name.substring(0,2).equals(ChatColor.DARK_GREEN.toString())) {
			name = name.substring(2);
		}
		
		if (name != null && name.substring(0, 8).equalsIgnoreCase("PesoShop")) {
			if (name.length() <= 9) {
				e.getPlayer().sendMessage(ChatColor.RED + "You did not specify a price!");
				return;
			}
			String toParse = name.substring(9);
			int n = 0;
			try {
				n = Integer.parseInt(toParse);
			} catch (NumberFormatException ex) {
				e.getPlayer().sendMessage(ChatColor.RED + toParse + " is not a valid integer number");
				return;
			}
			if (n < 1) {
				e.getPlayer().sendMessage(ChatColor.RED + (n + " is not a valid amount"));
			} else {
				e.getPlayer().sendMessage("You placed a " + ChatColor.GOLD + "PesoShop" + ChatColor.RESET
						+ " with a price of " + ChatColor.GREEN + n + ChatColor.RESET + " pesos per slot");
				plugin.shopConfig.set(Shop.getLoc(loc), new Shop(p, n));
				plugin.saveShops();
			}
		}
	}

	private boolean isNextToShopThatIs(Material type, Location loc) {
		if ((loc.clone().add(1, 0, 0).getBlock().getType() == type && isShop(loc.clone().add(1, 0, 0)))
				|| (loc.clone().add(-1, 0, 0).getBlock().getType() == type && isShop(loc.clone().add(-1, 0, 0)))
				|| (loc.clone().add(0, 0, 1).getBlock().getType() == type && isShop(loc.clone().add(0, 0, 1)))
				|| (loc.clone().add(0, 0, -1).getBlock().getType() == type && isShop(loc.clone().add(0, 0, -1)))) {
			return true;
		}
		return false;

	}

	public boolean isNextTo(Material type, Location loc) {
		if (loc.clone().add(1, 0, 0).getBlock().getType() == type
				|| loc.clone().add(-1, 0, 0).getBlock().getType() == type
				|| loc.clone().add(0, 0, 1).getBlock().getType() == type
				|| loc.clone().add(0, 0, -1).getBlock().getType() == type) {
			return true;
		}
		return false;
	}

	private boolean isShop(Location loc) {
		if (plugin.shopConfig.contains(Shop.getLoc(loc))) {
			return true;
		}
		return false;
	}

	@EventHandler
	public void onAnvil(PrepareAnvilEvent event) {
		ItemStack stack = event.getInventory().getContents()[0];
		if (stack == null) {
			return;
		}
		if (!stack.hasItemMeta()) {
			return;
		}
		ItemMeta meta = stack.getItemMeta();
		if (!meta.hasDisplayName()) {
			return;
		}
		String name = meta.getDisplayName();
		if ((stack.getType() == Material.PAPER && name.substring(0, 2).equals("�l"))
				|| name.equals(plugin.config.getItemStack("1").getItemMeta().getDisplayName())
				|| name.equals(plugin.config.getItemStack("10").getItemMeta().getDisplayName())
				|| name.equals(plugin.config.getItemStack("20").getItemMeta().getDisplayName())
				|| name.equals(plugin.config.getItemStack("50").getItemMeta().getDisplayName())) {
			event.setResult(null);
		}
	}

	@EventHandler
	public void onPiston(BlockPistonExtendEvent event) {
		event.getBlocks().forEach(block -> {
			String loc = Shop.getLoc(block.getLocation());
			if (plugin.shopConfig.contains(loc)) {
				event.setCancelled(true);
				return;
			}
		});
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (e.getPlayer() == null) {
			return;
		}

		if (!(e.getBlock().getState() instanceof Container)) {
			String loc = Shop.getLoc(e.getBlock().getLocation());
			if (plugin.shopConfig.contains(loc)) {
				plugin.shopConfig.set(loc, null);
			}
			return;
		}

		String loc = Shop.getLoc(e.getBlock().getLocation());
		if (plugin.shopConfig.contains(loc)) {
			Shop shop = (Shop) plugin.shopConfig.get(loc);
			String owner = shop.owner;
			String player = e.getPlayer().getName();
			if (player.equalsIgnoreCase(owner)) {
				e.getPlayer().sendMessage("You " + ChatColor.RED + "removed " + ChatColor.RESET + "your "
						+ ChatColor.GREEN + shop.price + ChatColor.RESET + " peso PesoShop");
				plugin.shopConfig.set(loc, null);
				plugin.saveShops();
			} else if (plugin.ADMINS.contains(player)) {
				e.getPlayer()
						.sendMessage("You " + ChatColor.RED + "removed " + ChatColor.GOLD + owner + ChatColor.RESET
								+ "'s " + ChatColor.GREEN + shop.price + ChatColor.RESET
								+ " peso PesoShop using �4�lADMIN OVERRIDE");
			} else {
				e.setCancelled(true);
			}
		}
	}
}
