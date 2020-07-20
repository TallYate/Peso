package me.joshua.peso;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PesoShop implements Listener {
	private Main plugin;

	public PesoShop(Main pluginIn) {
		plugin = pluginIn;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getSlot() < 0) {
			return;
		}

		if (plugin.ADMINS.contains(e.getWhoClicked().getName())) {
			return;
		}

		if (e.getInventory().getLocation() == null) {
			return;
		}

		String loc = Shop.getLoc(e.getClickedInventory().getLocation());
		if (e.getClickedInventory().getType() == InventoryType.CHEST) {
			if (plugin.shopConfig.contains(loc)) {
				Player p = (Player) e.getWhoClicked();
				Shop shop = (Shop) plugin.shopConfig.get(loc);
				int money = plugin.bankConfig.getInt(p.getName());
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

							ItemStack[] pInv = p.getInventory().getStorageContents();
							while (i < pInv.length && flag) {
								if (pInv[i] == null) {
									flag = false;
								}
								i++;
							}
							if (flag) {
								Bukkit.getScheduler().runTask(plugin, task -> {
									p.closeInventory();
									p.sendMessage(ChatColor.RED + "You do not have enough inventory space");
								});
							} else {
								p.getInventory().addItem(e.getCurrentItem());
								ItemStack current = e.getCurrentItem();
								if (current.getType() == Material.AIR) {
									return;
								}
								e.setCurrentItem(cheque);
								plugin.bankConfig.set(p.getName(), money - shop.price);
								Bukkit.getScheduler().runTask(plugin, task -> {
									p.closeInventory();
									if (current.hasItemMeta()) {
										p.sendMessage("You purchased " + current.getItemMeta().getDisplayName()
												+ ChatColor.BLUE + " x" + current.getAmount() + " for "
												+ ChatColor.GREEN + shop.price + ChatColor.RESET + " pesos from "
												+ ChatColor.GOLD + shop.owner);
									} else {
										p.sendMessage("You purchased " + current.getType().toString() + ChatColor.BLUE
												+ " x" + current.getAmount() + ChatColor.RESET + " for "
												+ ChatColor.GREEN + shop.price + ChatColor.RESET + " pesos from "
												+ ChatColor.GOLD + shop.owner);
									}

									p.sendMessage("Your new balance is " + ChatColor.GREEN
											+ plugin.bankConfig.getInt(p.getName()) + ChatColor.RESET + " pesos");
									plugin.saveBank();
								});
							}
						} else {
							Bukkit.getScheduler().runTask(plugin, task -> {
								if (e.getCurrentItem().getType() != Material.AIR) {
									p.closeInventory();
									p.sendMessage(ChatColor.RED + "You do not have " + ChatColor.GREEN + shop.price
											+ ChatColor.RED + " pesos");
								}
							});
						}
					}
					e.setCancelled(true);
				}

			}

		} else if (e.getInventory().getLocation() != null) {
			if (e.getInventory().getType() == InventoryType.CHEST) {
				if (plugin.shopConfig.contains(Shop.getLoc(e.getInventory().getLocation()))) {
					if (!((Player) e.getWhoClicked()).getName().equals(
							((Shop) plugin.shopConfig.get(Shop.getLoc(e.getInventory().getLocation()))).owner)) {
						e.setCancelled(true);
						return;
					}
				}
			}
		}
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
		if (e.getInventory().getType() == InventoryType.CHEST) {
			if (plugin.shopConfig.contains(loc)) {
				if (((Shop) plugin.shopConfig.get(loc)).owner != e.getWhoClicked().getName()) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onOpen(InventoryOpenEvent e) {
		Location invLoc = e.getInventory().getLocation();
		if (invLoc == null) {
			return;
		}
		if (plugin.shopConfig.contains(Shop.getLoc(invLoc))) {
			if (e.getInventory().getType() == InventoryType.CHEST) {
				Shop shop = (Shop) plugin.shopConfig.get(Shop.getLoc(invLoc));
				String msg = "You opened ";
				HumanEntity h = e.getPlayer();
				if (shop.owner.equalsIgnoreCase(h.getName())) {
					msg += "your own shop";
				} else {
					msg += "§2§l" + shop.owner + "§r's shop";
				}

				if (plugin.ADMINS.contains(h.getName())) {
					msg += " in §4§lADMIN OVERRIDE§r mode";
				}
				h.sendMessage(msg);
			}
		}
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
					e.getPlayer().sendMessage(ChatColor.RED + toParse + " is not a valid number");
					return;
				}
				if (n < 1) {
					e.getPlayer().sendMessage(ChatColor.RED + (n + " is not a valid number"));
				} else {
					e.getPlayer().sendMessage("You placed a " + ChatColor.GOLD + "PesoShop" + ChatColor.RESET
							+ " with a price of " + ChatColor.GREEN + n + ChatColor.RESET + " pesos per slot");
					plugin.shopConfig.set(Shop.getLoc(loc), new Shop(p, n));
					plugin.saveShops();
				}
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (e.getPlayer() == null) {
			return;
		}
		if (e.getBlock().getType() != Material.CHEST) {
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
								+ " peso PesoShop using §4§lADMIN OVERRIDE");
			} else {
				e.setCancelled(true);
			}
		}
	}

}
