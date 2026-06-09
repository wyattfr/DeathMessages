package org.nightfall.deathmessages.client.config;

/**
 * Settings specific to a death category.
 * Allows for fine-grained control over how different types of death messages are filtered.
 */
public class CategorySettings {
	/** Whether this category is enabled */
	public boolean enabled;
	/** Maximum deaths per player for this category */
	public int maxDeathsPerPlayer;
	/** Time window in seconds for this category's rate limiting */
	public int timeWindowSeconds;
}

