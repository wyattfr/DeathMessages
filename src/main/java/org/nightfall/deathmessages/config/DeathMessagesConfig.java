package org.nightfall.deathmessages.config;

import java.util.Map;

/**
 * Configuration data class for the DeathMessages mod.
 * This class holds all configuration options that can be persisted to disk.
 */
public class DeathMessagesConfig {
    /** Whether the mod is enabled */
    public boolean enabled;

    /** Maximum number of death messages to show per player before suppression */
    public int maxDeathsPerPlayer;
    /** Time window in seconds for death message rate limiting */
    public int timeWindowSeconds;

    /** Whether to show a notice when death messages are suppressed */
    public boolean showSuppressionNotice;
    /** Cooldown in seconds between suppression notices for the same player */
    public int suppressionNoticeCooldownSeconds;

    /** Per-category settings */
    public Map<String, CategorySettings> categorySettings;

    /** Players whose death messages are always hidden */
    public String[] hiddenPlayers;
    /** Death causes to hide (e.g., "arrow", "fireball") */
    public String[] hiddenCauses;
    /** Death sources to hide (e.g., specific mob names) */
    public String[] hiddenSources;
    /** Text patterns to hide in death messages */
    public String[] hiddenText;

    /** Players whose death messages are always shown */
    public String[] alwaysShowPlayers;
    /** Text patterns that always show death messages */
    public String[] alwaysShowText;
    /** Whether to always show player-vs-player kills */
    public boolean alwaysShowPlayerKills;
}

