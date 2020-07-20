package me.joshua.peso;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin {
	public FileConfiguration config = this.getConfig();
	public File bankFile;
	public FileConfiguration bankConfig;

	public File shopFile;
	public FileConfiguration shopConfig;

	public List<String> ADMINS = new Vector<String>();

	public void onEnable() {
		this.saveDefaultConfig();
		ConfigurationSerialization.registerClass(Shop.class);
		createBank();
		createShops();
		adminBar();
		new Commands(this);
		new PesoShop(this);
	}

	public void adminBar() {
		Bukkit.getScheduler().runTaskTimer(this ,() -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
			    if (ADMINS.contains(p.getName())) {
			      p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§l§4Admin Override"));
			    }
			  }
		},0L, 20L);
	}

	public void onDisable() {
	}

	private void createBank() {
		bankFile = new File(getDataFolder(), "bank.yml");
		if (!bankFile.exists()) {
			bankFile.getParentFile().mkdirs();
			saveResource("bank.yml", false);
		}

		bankConfig = new YamlConfiguration();
		try {
			bankConfig.load(bankFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void saveBank() {
		try {
			bankConfig.save(bankFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createShops() {
		shopFile = new File(getDataFolder(), "shops.yml");
		if (!shopFile.exists()) {
			shopFile.getParentFile().mkdirs();
			saveResource("shops.yml", false);
		}

		shopConfig = new YamlConfiguration();
		try {
			shopConfig.load(shopFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void saveShops() {
		try {
			shopConfig.save(shopFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
