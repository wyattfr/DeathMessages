package org.nightfall.deathmessages.config;

public class CategorySettings {
    /** Whether this category is enabled */
    public boolean enabled;
    /** Maximum deaths per player for this category */
    public int maxDeathsPerPlayer;
    /** Time window in seconds for this category's rate limiting */
    public int timeWindowSeconds;
}

