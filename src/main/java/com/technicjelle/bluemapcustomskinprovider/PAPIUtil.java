package com.technicjelle.bluemapcustomskinprovider;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PAPIUtil {
	private PAPIUtil() {}

	public static String processPAPI(Player player, String str) {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && player != null) {
			return PlaceholderAPI.setPlaceholders(player, str);
		}
		else return str;
	}
}
