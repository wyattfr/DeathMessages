package org.nightfall.deathmessages.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;
import org.nightfall.deathmessages.client.command.DeathMessagesCommand;
import org.nightfall.deathmessages.client.config.DeathMessagesConfigManager;
import org.nightfall.deathmessages.client.filter.DeathMessageFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathMessagesClient implements ClientModInitializer {
	public static final String MOD_ID = "deathmessages";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		try {
			DeathMessagesConfigManager.load();
			LOGGER.info("Configuration loaded successfully.");
		} catch (Exception e) {
			LOGGER.error("Failed to load configuration", e);
		}

		try {
			DeathMessagesCommand.register();
			LOGGER.info("Commands registered successfully.");
		} catch (Exception e) {
			LOGGER.error("Failed to register commands", e);
		}

		ClientReceiveMessageEvents.ALLOW_GAME.register((Component message, boolean overlay) -> {
			if (overlay) {
				return true;
			}

			return !DeathMessageFilter.shouldHide(message);
		});

		LOGGER.info("DeathMessages mod initialized.");
	}
}