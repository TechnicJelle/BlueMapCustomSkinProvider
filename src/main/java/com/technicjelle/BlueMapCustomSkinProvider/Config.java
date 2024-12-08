package com.technicjelle.BlueMapCustomSkinProvider;

import com.technicjelle.BMUtils.BMNative.BMNConfigDirectory;
import de.bluecolored.bluemap.api.BlueMapAPI;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.io.IOException;
import java.nio.file.Path;

@ConfigSerializable
public class Config {
	private static final String fileName = "settings.conf";

	@Comment("The skin URL")
	private @Nullable String url;

	@Comment("The username URL")
	private @Nullable String usernameUrl;

	@Comment("Username API json path")
	private @Nullable String jsonPath;

	public static Config load(BlueMapAPI api) throws IOException {
		BMNConfigDirectory.BMNCopy.fromJarResource(api, Config.class.getClassLoader(), fileName, fileName, false);
		Path configDirectory = BMNConfigDirectory.getAllocatedDirectory(api, Config.class.getClassLoader());
		Path configFile = configDirectory.resolve(fileName);

		HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
				.defaultOptions(options -> options.implicitInitialization(false))
				.path(configFile).build();

		Config config = loader.load().get(Config.class);
		if (config == null) {
			throw new IOException("Failed to load config");
		}
		return config;
	}

	public @Nullable String getUrl() {
		return url;
	}

	public @Nullable String getUsernameURL() {
		return usernameUrl;
	}

	public @Nullable String getJsonPath() {
		return jsonPath;
	}
}
