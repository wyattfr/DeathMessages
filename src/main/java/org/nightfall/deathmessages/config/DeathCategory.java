package org.nightfall.deathmessages.config;

/**
 * Enumeration of death message categories.
 * Used to classify and manage death messages by type.
 */
public enum DeathCategory {
    /** Death caused by another player */
    PLAYER,
    /** Death caused by a hostile mob */
    HOSTILE,
    /** Death caused by a passive mob */
    PASSIVE,
    /** Death caused by environmental damage (suffocation, starvation, freezing, etc.) */
    ENVIRONMENT,
    /** Death caused by falling */
    FALL,
    /** Death caused by fire or lava */
    FIRE_LAVA,
    /** Death caused by drowning */
    DROWNING,
    /** Death caused by explosion */
    EXPLOSION,
    /** Death caused by falling into the void */
    VOID,
    /** All other types of death */
    OTHER
}

