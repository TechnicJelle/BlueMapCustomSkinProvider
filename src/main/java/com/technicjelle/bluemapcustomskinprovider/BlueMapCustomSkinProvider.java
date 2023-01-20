package com.technicjelle.bluemapcustomskinprovider;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.SkinProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;

@SuppressWarnings("SameParameterValue")
public final class BlueMapCustomSkinProvider extends JavaPlugin {

	@Override
	public void onEnable() {
		BlueMapAPI.onEnable(blueMapOnEnableListener);

		getLogger().info("BlueMapCustomSkinProvider plugin enabled!");
	}

	private final Consumer<BlueMapAPI> blueMapOnEnableListener = blueMapAPI -> {

		// Setup config
		if(getDataFolder().mkdirs()) getLogger().info("Created plugin config directory");
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			try {
				getLogger().info("Creating config file");
				Files.copy(Objects.requireNonNull(getResource("config.yml")), configFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//Load config from disk
		reloadConfig();

		//Load config values into variables
		String url = getConfig().getString("url");

		SkinProvider floodgateSkinProvider = playerUUID -> {
			String username = getServer().getOfflinePlayer(playerUUID).getName();
			String localUrl = url.replace("{UUID}", playerUUID.toString()).replace("{USERNAME}", username);
			getLogger().info("Downloading skin for " + username + " from " + localUrl);
			BufferedImage img = imageFromURL(localUrl);
			return Optional.ofNullable(img);
		};

		blueMapAPI.getPlugin().setSkinProvider(floodgateSkinProvider);
	};


	/**
	 * @param url URL of the image
	 * @return the image, or null if it could not be found
	 */
	private @Nullable BufferedImage imageFromURL(@NotNull String url) {
		BufferedImage result;
		try {
			URL imageUrl = new URL(url);
			try {
				InputStream in = imageUrl.openStream();
				result = ImageIO.read(in);
				in.close();
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Failed to get the image from " + url, e);
				return null;
			}
		} catch (MalformedURLException e) {
			getLogger().log(Level.SEVERE, "URL is malformed: " + url, e);
			return null;
		}
		return result;
	}
}
