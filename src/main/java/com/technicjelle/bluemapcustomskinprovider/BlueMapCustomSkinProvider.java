package com.technicjelle.bluemapcustomskinprovider;

import com.technicjelle.MCUtils;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.SkinProvider;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import com.technicjelle.UpdateChecker;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class BlueMapCustomSkinProvider extends JavaPlugin {
	UpdateChecker updateChecker;

	@Override
	public void onEnable() {
		new Metrics(this, 18368);

		updateChecker = new UpdateChecker("TechnicJelle", "BlueMapCustomSkinProvider", getDescription().getVersion());
		updateChecker.checkAsync();

		BlueMapAPI.onEnable(blueMapOnEnableListener);

		getLogger().info("BlueMapCustomSkinProvider plugin enabled!");
	}

	private final Consumer<BlueMapAPI> blueMapOnEnableListener = blueMapAPI -> {
		updateChecker.logUpdateMessage(getLogger());

		//Copy config.yml from jar to config folder
		try {
			MCUtils.copyPluginResourceToConfigDir(this, "config.yml", "config.yml", false);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to copy config.yml from jar to config folder!", e);
		}

		//Load config from disk
		reloadConfig();

		//Load config values into variables
		String url = getConfig().getString("url");

		SkinProvider customSkinProvider = playerUUID -> {
			@Nullable String bukkitName = getServer().getOfflinePlayer(playerUUID).getName();
			@NotNull String username = bukkitName != null ? bukkitName : playerUUID.toString();
			String localUrl = url.replace("{UUID}", playerUUID.toString()).replace("{USERNAME}", username);
			getLogger().info("Downloading skin for " + username + " from " + localUrl);
			BufferedImage img = MCUtils.downloadImage(localUrl);
			return Optional.ofNullable(img);
		};

		blueMapAPI.getPlugin().setSkinProvider(customSkinProvider);
	};
}
