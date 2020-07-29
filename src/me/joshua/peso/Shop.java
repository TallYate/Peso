package me.joshua.peso;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

@SerializableAs("Shop")
public class Shop implements ConfigurationSerializable {
	public String owner;
	public int price;
	
	public Shop(Player p, int price) {
		this.owner = p.getName();
		this.price = price;
	}
	
	public Shop(String pName, int price) {
		this.owner = pName;
		this.price = price;
	}
	
	public Shop(Map<String, Object> map) {
		this.owner = (String) map.get("owner");
		this.price = (int) map.get("price");
	}
	
	@Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("owner", owner);
        map.put("price", price);
        return map;
    }
	
	public static String getLoc(Location loc) {
		return loc.getBlockX() + ":" +  loc.getBlockY() + ":" + loc.getBlockZ() + "->" + loc.getWorld().getName();
	}
	
}
