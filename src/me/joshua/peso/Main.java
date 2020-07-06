package me.joshua.peso;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public FileConfiguration config = this.getConfig();
	public static File bankFile;
	public static FileConfiguration bankConfig;
	
	public static File shopFile;
	public static FileConfiguration shopConfig;
	
	public void onEnable() {
		this.saveDefaultConfig();
		ConfigurationSerialization.registerClass(Shop.class);
		createBank();
		createShops();
		new Commands(this);
		new PesoShop(this);
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
	
	public static void saveShops() {
		try {
			shopConfig.save(shopFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
