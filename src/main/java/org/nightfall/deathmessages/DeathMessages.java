package org.nightfall.deathmessages;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathMessages implements ModInitializer {
	public static final String MOD_ID = "deathmessages";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("DeathMessages mod is being loaded.");
	}
}