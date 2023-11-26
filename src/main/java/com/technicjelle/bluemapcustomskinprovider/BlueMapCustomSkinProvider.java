package com.technicjelle.bluemapcustomskinprovider;

import org.jetbrains.annotations.NotNull;

// Core Java Misc
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.MalformedURLException;
import java.net.URL;

// Java Utilities
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Base64;
import java.util.UUID;
import java.util.Base64;

// Core Bukkit
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

// technicjelle Tools
import com.technicjelle.MCUtils;
import com.technicjelle.UpdateChecker;

// BlueMapAPI
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.SkinProvider;

// SkinsRestorerAPI
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.VersionProvider;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.property.SkinProperty;

// JSON Support
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;

public final class BlueMapCustomSkinProvider extends JavaPlugin {
	// Globals
	private final Logger logger = getLogger();
	private JSONParser jsonParser;
	private UpdateChecker updateChecker;
	private SkinsRestorer skinsRestorerAPI;

	@Override
	public void onEnable() {
		// Initialization
		logger.info("BlueMapCustomSkinProvider");
		logger.info("Version " + this.getDescription().getVersion());
		logger.info("https://github.com/TechnicJelle/BlueMapCustomSkinProvider");
		// Metrics ID
		new Metrics(this, 18368);
		// Update Checker
		updateChecker = new UpdateChecker("TechnicJelle", "BlueMapCustomSkinProvider", getDescription().getVersion());
		updateChecker.checkAsync();
		// Components
		jsonParser = new JSONParser();
		// Bluemap (Required)
		BlueMapAPI.onEnable(blueMapOnEnableListener);
		// SkinsRestorerAPI (Optional)
		try {
			// Retrieve the SkinsRestorer API for applying the skin
			skinsRestorerAPI = SkinsRestorerProvider.get();
			if (!VersionProvider.isCompatibleWith("15")) {
				logger.info("This plugin was made for SkinsRestorer v15, but " + VersionProvider.getVersionInfo() + " is installed. There may be errors!");
			}			
			System.out.println("SkinsRestorer support is enabled!");
		}
		catch(Exception e) {
			System.out.println("SkinsRestorer not found.");
		}
	}
	
	// Adapted from " https://github.com/webbukkit/dynmap/blob/ca80758605f6bdbe6d243e071d0d2b582b759bf0/spigot/src/main/java/org/dynmap/bukkit/SkinsRestorerSkinUrlProvider.java "
    private URL getSkinUrl(UUID playerUUID) {
		final PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
		
        final Optional<SkinProperty> playerSkinPropertyOptional = playerStorage.getSkinOfPlayer(playerUUID);
		
		if (!playerSkinPropertyOptional.isPresent()) {
			return null;
		}
		
		final SkinProperty playerSkinProperty = playerSkinPropertyOptional.get();

        String skinDataPropertyValue = playerSkinProperty.getValue();

        byte[] skinDataBytes = Base64.getDecoder().decode(skinDataPropertyValue);

        JSONObject skinData;

        try {
            skinData = (JSONObject) jsonParser.parse(new String(skinDataBytes, StandardCharsets.UTF_8));
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }

        try {
            return new URL((String) ((JSONObject) ((JSONObject) skinData.get("textures")).get("SKIN")).get("url"));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

	private final Consumer<BlueMapAPI> blueMapOnEnableListener = blueMapAPI -> {
		// Perform Update Check
		updateChecker.logUpdateMessage(logger);

		//Copy config.yml from jar to config folder
		try {
			MCUtils.copyPluginResourceToConfigDir(this, "config.yml", "config.yml", false);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to copy config.yml from jar to config folder!", e);
		}

		//Load config from disk
		reloadConfig();

		//Load config values into variables
		String url = getConfig().getString("url");
		
		// Prepare the Skin Provider
		SkinProvider customSkinProvider;
		
		if (url.trim().equalsIgnoreCase("skinsrestorer")) {
			logger.info("Using skinsRestorerAPI as the source!");
			
			customSkinProvider = playerUUID -> {
				@Nullable String bukkitName = getServer().getOfflinePlayer(playerUUID).getName();
				@NotNull String username = bukkitName != null ? bukkitName : playerUUID.toString();
				// Get Player Skin
				final URL playerSkinURL = getSkinUrl(playerUUID);
				logger.info("SkinRestorer returned URL: " + playerSkinURL);
				BufferedImage img = MCUtils.downloadImage(playerSkinURL);
				return Optional.ofNullable(img);
			};
		} else {
			logger.info("URL defined as the source: '" + url + "'");
			
			customSkinProvider = playerUUID -> {
				@Nullable String bukkitName = getServer().getOfflinePlayer(playerUUID).getName();
				@NotNull String username = bukkitName != null ? bukkitName : playerUUID.toString();
				String localUrl = url.replace("{UUID}", playerUUID.toString()).replace("{USERNAME}", username);
				logger.info("Downloading skin for " + username + " from " + localUrl);
				BufferedImage img = MCUtils.downloadImage(localUrl);
				return Optional.ofNullable(img);
			};
		}
		
		blueMapAPI.getPlugin().setSkinProvider(customSkinProvider);
	};
}
