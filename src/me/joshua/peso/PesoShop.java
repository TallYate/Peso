package me.joshua.peso;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class PesoShop implements Listener {
	private static Main plugin;

	public PesoShop(Main pluginIn) {
		plugin = pluginIn;

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public static void onClick(InventoryClickEvent e) {
		if (e.getSlot() < 0) {
			return;
		}
		String loc = Shop.getLoc(e.getClickedInventory().getLocation());
		if (e.getClickedInventory().getType() == InventoryType.CHEST) {
			if (Main.shopConfig.contains(loc)) {
				Player p = (Player) e.getWhoClicked();
				Shop shop = (Shop) Main.shopConfig.get(loc);
				int money = Main.bankConfig.getInt(p.getName());
				if (!p.getName().equals(shop.owner)) {
					if (e.getWhoClicked() instanceof Player) {
						boolean flag = true;
						int i = 0;
						if (money >= shop.price) {

							ItemStack cheque;
							if (shop.price > 64) {
								cheque = new ItemStack(Material.PAPER);
								ItemMeta meta = cheque.getItemMeta();
								meta.setDisplayName("§l" + Integer.toString(shop.price));
								cheque.setItemMeta(meta);
							} else {
								cheque = plugin.config.getItemStack("1");
								cheque.setAmount(shop.price);
							}

							PlayerInventory pInv = p.getInventory();
							while (i < pInv.getSize() && flag) {
								if (pInv.getItem(i) == null) {
									flag = false;
								}
								i++;
							}
							if (flag) {
								Bukkit.getScheduler().runTask(plugin, task -> {
									p.closeInventory();
									p.sendMessage("You do not have enough inventory space");
								});
							} else {
								pInv.addItem(e.getCurrentItem());
								ItemStack current = e.getCurrentItem();
								if (current.getType() == Material.AIR) {
									return;
								}
								e.setCurrentItem(cheque);
								Main.bankConfig.set(p.getName(), money - shop.price);
								Bukkit.getScheduler().runTask(plugin, task -> {
									p.closeInventory();
									if (current.hasItemMeta()) {
										p.sendMessage("You bought " + current.getItemMeta().getDisplayName() + " for "
												+ shop.price + "pesos");
									} else {
										p.sendMessage("You bought " + current.getType().toString() + " for "
												+ shop.price + "pesos");
									}

									p.sendMessage(
											"Your new balance is " + Main.bankConfig.getInt(p.getName()) + " pesos");
								});
							}
						} else {
							Bukkit.getScheduler().runTask(plugin, task -> {
								p.closeInventory();
								p.sendMessage("You do not have " + shop.price + " pesos");
							});
						}
					}
					e.setCancelled(true);
				}

			}

		} else if (Main.shopConfig.contains(Shop.getLoc(e.getInventory().getLocation()))) {
			if (!((Player) e.getWhoClicked()).getName()
					.equals(((Shop) Main.shopConfig.get(Shop.getLoc(e.getInventory().getLocation()))).owner)) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public static void onHopper(InventoryMoveItemEvent e) {
		if (Main.shopConfig.contains(Shop.getLoc(e.getSource().getLocation()))) {
			e.setCancelled(true);
		} else if (Main.shopConfig.contains(Shop.getLoc(e.getDestination().getLocation()))) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public static void onEntityExplode(EntityExplodeEvent e) {
		for (int i = 0; i < e.blockList().size(); i++) {
			if (Main.shopConfig.contains(Shop.getLoc(e.blockList().get(i).getLocation()))) {
				e.blockList().remove(i);
			}
		}
	}

	@EventHandler
	public static void onPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (p == null) {
			return;
		}
		if (e.getBlock().getType() != Material.CHEST) {
			return;
		}

		Location loc = e.getBlock().getLocation();
		ItemMeta meta = e.getItemInHand().getItemMeta();

		if (meta.hasDisplayName()) {
			if (meta.getDisplayName().substring(0, 8).equalsIgnoreCase("PesoShop")) {
				String toParse = meta.getDisplayName().substring(9);
				int n = 0;
				try {
					n = Integer.parseInt(toParse);
				} catch (NumberFormatException ex) {
					e.getPlayer().sendMessage(toParse + " is not a valid number");
				}
				if (n < 1) {
					e.getPlayer().sendMessage(n + " is not a valid number");
				} else {
					Main.shopConfig.set(Shop.getLoc(loc), new Shop(p, n));
					Main.saveShops();
				}
			}
		}
	}

	@EventHandler
	public static void onBreak(BlockBreakEvent e) {
		if (e.getPlayer() == null) {
			return;
		}
		if (e.getBlock().getType() != Material.CHEST) {
			return;
		}

		String loc = Shop.getLoc(e.getBlock().getLocation());
		if (Main.shopConfig.contains(loc)) {
			if (e.getPlayer().getName().equalsIgnoreCase(((Shop) Main.shopConfig.get(loc)).owner)) {
				Main.shopConfig.set(loc, null);
				Main.saveShops();
			} else {
				Bukkit.broadcastMessage(e.getPlayer().getName());
				e.setCancelled(true);
			}
		}
	}

}
