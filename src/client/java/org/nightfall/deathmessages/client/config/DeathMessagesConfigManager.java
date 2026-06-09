package org.nightfall.deathmessages.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DeathMessagesConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("deathmessages");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("deathmessages.json");

	public static DeathMessagesConfig CONFIG = defaultConfig();

	public static void load() {
		try {
			if (Files.exists(CONFIG_PATH)) {
				try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
					Type t = new TypeToken<DeathMessagesConfig>() {}.getType();
					CONFIG = GSON.fromJson(r, t);
					if (CONFIG == null) {
						LOGGER.warn("Config file was empty, using default config");
						CONFIG = defaultConfig();
					} else {
						LOGGER.info("Configuration loaded successfully from {}", CONFIG_PATH);
					}
				}
			} else {
				LOGGER.info("Config file not found, creating default config at {}", CONFIG_PATH);
				save();
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load configuration from {}", CONFIG_PATH, e);
			CONFIG = defaultConfig();
		}
	}

	public static void save() {
		try {
			if (!Files.exists(CONFIG_PATH.getParent())) {
				Files.createDirectories(CONFIG_PATH.getParent());
				LOGGER.debug("Created config directory: {}", CONFIG_PATH.getParent());
			}
			try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(CONFIG, w);
				LOGGER.info("Configuration saved successfully to {}", CONFIG_PATH);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to save configuration to {}", CONFIG_PATH, e);
		}
	}

	private static DeathMessagesConfig defaultConfig() {
		LOGGER.debug("Creating default configuration");
		DeathMessagesConfig c = new DeathMessagesConfig();
		c.enabled = true;
		c.maxDeathsPerPlayer = 3;
		c.timeWindowSeconds = 300;
		c.showSuppressionNotice = true;
		c.suppressionNoticeCooldownSeconds = 60;

		c.categorySettings = new HashMap<>();
		for (DeathCategory cat : DeathCategory.values()) {
			CategorySettings s = new CategorySettings();
			s.enabled = true;
			s.maxDeathsPerPlayer = switch(cat) {
				case PLAYER -> 5;
				case PASSIVE -> 2;
				case FALL -> 2;
				case FIRE_LAVA -> 2;
				case DROWNING -> 2;
				case VOID -> 2;
				default -> 3;
			};
			s.timeWindowSeconds = 300;
			c.categorySettings.put(cat.name(), s);
		}

		c.hiddenPlayers = new String[0];
		c.hiddenCauses = new String[0];
		c.hiddenSources = new String[0];
		c.hiddenText = new String[0];

		c.alwaysShowPlayers = new String[0];
		c.alwaysShowText = new String[0];
		c.alwaysShowPlayerKills = true;

		return c;
	}
}

