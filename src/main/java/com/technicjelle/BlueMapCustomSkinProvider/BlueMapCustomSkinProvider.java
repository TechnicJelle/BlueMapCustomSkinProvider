package com.technicjelle.BlueMapCustomSkinProvider;

import com.jayway.jsonpath.JsonPath;
import com.technicjelle.BMUtils.BMNative.BMNLogger;
import com.technicjelle.BMUtils.BMNative.BMNMetadata;
import com.technicjelle.UpdateChecker;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.plugin.SkinProvider;
import de.bluecolored.bluemap.common.api.BlueMapAPIImpl;
import de.bluecolored.bluemap.common.api.PluginImpl;
import de.bluecolored.bluemap.common.plugin.Plugin;
import de.bluecolored.bluemap.common.serverinterface.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class BlueMapCustomSkinProvider implements Runnable {
	private BMNLogger logger;
	private UpdateChecker updateChecker;
	private @Nullable Config config;

	private boolean isCLI = false;
	private final Set<HashedPlayer> allPlayers = new HashSet<>();

	@Override
	public void run() {
		String addonID;
		String addonVersion;
		try {
			addonID = BMNMetadata.getAddonID(this.getClass().getClassLoader());
			addonVersion = BMNMetadata.getKey(this.getClass().getClassLoader(), "version");
			logger = new BMNLogger(this.getClass().getClassLoader());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.logInfo("Starting " + addonID + " " + addonVersion);
		updateChecker = new UpdateChecker("TechnicJelle", addonID, addonVersion);
		updateChecker.checkAsync();
		BlueMapAPI.onEnable(blueMapOnEnableListener);
	}

	private final Consumer<BlueMapAPI> blueMapOnEnableListener = api -> {
		updateChecker.getUpdateMessage().ifPresent(logger::logWarning);

		if (((BlueMapAPIImpl) api).plugin() == null) {
			isCLI = true;
			throw new UnsupportedOperationException("Running on the CLI mode is not supported yet in BlueMap!");
			//TODO: Hopefully I can take this out in the future
			// Once the BlueMap CLI can take a SkinProvider...
			// Useful for integration with BMOPM
		}

		try {
			config = Config.load(api);
		} catch (IOException e) {
			config = null;
			throw new RuntimeException(e);
		}

		String url = config.getUrl();
		if (url == null) {
			throw new RuntimeException("No skin URL provided in the config file!");
		}

		SkinProvider customSkinProvider = playerUUID -> {
			if (!isCLI) {
				//BlueMap is attached to a server, so we remember all players from there
				Plugin plugin = ((PluginImpl) api.getPlugin()).getPlugin();
				Collection<Player> players = plugin.getServerInterface().getOnlinePlayers();
				for (Player player : players) allPlayers.add(new HashedPlayer(player));
			}

			String uuid = playerUUID.toString();
			String username = url.contains("{USERNAME}") ? getUsername(playerUUID) : uuid; //Do not make the potentially expensive API call if not needed
			String localUrl = url
					.replace("{UUID}", uuid)
					.replace("{USERNAME}", username)
					.replace("{UUID-}", uuid.replace("-", ""));
			logger.logDebug("Downloading skin for " + username + " from " + localUrl);
			BufferedImage img = downloadImage(localUrl);
			return Optional.ofNullable(img);
		};

		api.getPlugin().setSkinProvider(customSkinProvider);
	};

	private String getUsername(UUID uuid) {
		if (!isCLI) {
			//BlueMap is attached to a server, so we can get the username from the remembered players
			Optional<String> serverName = allPlayers.stream()
					.filter(player -> player.player().getUuid().equals(uuid))
					.findFirst()
					.map(hashedPlayer -> hashedPlayer.player().getName().toPlainString());
			if (serverName.isPresent()) return serverName.get();
		}

		if (config == null) throw new RuntimeException("Config is null in getUsername??? Please report this bug!");

		String playerUUID = uuid.toString();
		String usernameURL = config.getUsernameURL();
		String jsonPath = config.getJsonPath();
		if (usernameURL == null || usernameURL.isBlank() || jsonPath == null || jsonPath.isBlank()) return playerUUID;

		String localUrl = usernameURL
				.replace("{UUID}", playerUUID)
				.replace("{UUID-}", playerUUID.replace("-", ""));

		try {
			URL url = new URI(localUrl).toURL();
			try {
				URLConnection request = url.openConnection();
				request.connect();
				String jsonContent = new String(request.getInputStream().readAllBytes());
				return JsonPath.read(jsonContent, jsonPath);
			} catch (IOException e) {
				logger.logError("Failed to get username from " + localUrl + ": " + e.getMessage());
			}
		} catch (MalformedURLException | URISyntaxException e) {
			logger.logError("Invalid URL: " + localUrl);
		}

		return playerUUID;
	}

	/**
	 * Downloads an image from the given URL.
	 *
	 * @param link URL of the image
	 * @return The image, or <code>null</code> if it could not be found, or the link was invalid
	 */
	private static @Nullable BufferedImage downloadImage(@NotNull String link) {
		final @NotNull URL url;
		try {
			url = new URI(link).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			return null;
		}
		try (InputStream in = url.openStream()) {
			return ImageIO.read(in);
		} catch (IOException e) {
			return null;
		}
	}
}
