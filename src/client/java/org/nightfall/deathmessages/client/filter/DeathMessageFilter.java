package org.nightfall.deathmessages.client.filter;

import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import org.nightfall.deathmessages.client.config.CategorySettings;
import org.nightfall.deathmessages.client.config.DeathCategory;
import org.nightfall.deathmessages.client.config.DeathMessagesConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeathMessageFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger("deathmessages");


	private static final List<PatternEntry> PATTERNS = new ArrayList<>();
	private static final Set<String> HOSTILE_MOBS = new HashSet<>(Arrays.asList(
			"zombie","skeleton","creeper","spider","enderman","witch","blaze","husk","stray","phantom","drowned","slime","magma_cube","piglin","hoglin","evoker","vindicator","pillager","illusioner","shulker"
	));

	private static final Map<String, List<Long>> playerDeaths = new HashMap<>();
	private static final Map<String, Long> lastSuppressionNotice = new HashMap<>();

	static {
		add("^(.+) was slain by (.+)$", DeathCategory.HOSTILE);
		add("^(.+) was shot by (.+)$", DeathCategory.HOSTILE);
		add("^(.+) was killed by (.+)$", DeathCategory.HOSTILE);
		add("^(.+) was pummeled by (.+)$", DeathCategory.HOSTILE);
		add("^(.+) hit the ground too hard$", DeathCategory.FALL);
		add("^(.+) fell from a high place$", DeathCategory.FALL);
		add("^(.+) fell off a ladder$", DeathCategory.FALL);
		add("^(.+) fell while fighting (.+)$", DeathCategory.FALL);
		add("^(.+) tried to swim in lava$", DeathCategory.FIRE_LAVA);
		add("^(.+) walked into a wall of fire while fighting (.+)$", DeathCategory.FIRE_LAVA);
		add("^(.+) went up in flames$", DeathCategory.FIRE_LAVA);
		add("^(.+) was burnt to a crisp while fighting (.+)$", DeathCategory.FIRE_LAVA);
		add("^(.+) burned to death$", DeathCategory.FIRE_LAVA);
		add("^(.+) drowned$", DeathCategory.DROWNING);
		add("^(.+) suffocated in a wall$", DeathCategory.ENVIRONMENT);
		add("^(.+) starved to death$", DeathCategory.ENVIRONMENT);
		add("^(.+) froze to death$", DeathCategory.ENVIRONMENT);
		add("^(.+) was blown up by (.+)$", DeathCategory.EXPLOSION);
		add("^(.+) blew up$", DeathCategory.EXPLOSION);
		add("^(.+) fell out of the world$", DeathCategory.VOID);
		add("^(.+) tried to swim in lava$", DeathCategory.FIRE_LAVA);
		add("^(.+) was killed trying to hurt (.+)$", DeathCategory.OTHER);
		add("^(.+) was killed by magic$", DeathCategory.OTHER);
		add("^(.+)\'s .* was destroyed$", DeathCategory.OTHER);
		// Fallback: any message that starts with a name then a space and a lowercase verb
		add("^([A-Za-z0-9_\\-]+) .+$", DeathCategory.OTHER);
	}

	private static void add(String regex, DeathCategory cat) {
		PATTERNS.add(new PatternEntry(Pattern.compile(regex), cat));
	}

	public static boolean shouldHide(Component message) {
		try {
			if (message == null) return false;
			String text = message.getString();
			DeathMessagesConfigManager.CONFIG = DeathMessagesConfigManager.CONFIG; // ensure loaded
			if (!DeathMessagesConfigManager.CONFIG.enabled) return false;

			// quick checks for hidden text
			if (DeathMessagesConfigManager.CONFIG.hiddenText != null) {
				for (String s : DeathMessagesConfigManager.CONFIG.hiddenText) {
					if (s != null && !s.isEmpty() && text.contains(s)) {
						LOGGER.debug("Death message suppressed (hidden text): {}", text);
						return true;
					}
				}
			}

			Matcher found = null;
			DeathCategory cat = DeathCategory.OTHER;
			String player = null;
			String source = null;

			for (PatternEntry e : PATTERNS) {
				Matcher m = e.pattern.matcher(text);
				if (m.matches()) {
					cat = e.category;
					if (m.groupCount() >= 1) player = m.group(1);
					if (m.groupCount() >= 2) {
						source = m.group(2);
					}
					found = m;
					break;
				}
			}

			if (player == null) return false; // not a death message we recognise
			String playerKey = player.toLowerCase(Locale.ROOT);

			// if player is in alwaysShowPlayers or the message contains alwaysShowText -> always show
			if (DeathMessagesConfigManager.CONFIG.alwaysShowPlayers != null) {
				for (String s : DeathMessagesConfigManager.CONFIG.alwaysShowPlayers) {
					if (s != null && s.equalsIgnoreCase(player)) return false;
				}
			}
			if (DeathMessagesConfigManager.CONFIG.alwaysShowText != null) {
				for (String s : DeathMessagesConfigManager.CONFIG.alwaysShowText) {
					if (s != null && !s.isEmpty() && text.contains(s)) return false;
				}
			}

			// category-specific overrides
			CategorySettings catSettings = null;
			if (DeathMessagesConfigManager.CONFIG.categorySettings != null) {
				catSettings = DeathMessagesConfigManager.CONFIG.categorySettings.get(cat.name());
			}

			if (catSettings != null && !catSettings.enabled) return false;
			if (catSettings == null) {
				// fallback to global
				catSettings = new CategorySettings();
				catSettings.enabled = true;
				catSettings.maxDeathsPerPlayer = DeathMessagesConfigManager.CONFIG.maxDeathsPerPlayer;
				catSettings.timeWindowSeconds = DeathMessagesConfigManager.CONFIG.timeWindowSeconds;
			}

			// special case: if alwaysShowPlayerKills and the death was a player kill (source looks like a player) then always show
			if (DeathMessagesConfigManager.CONFIG.alwaysShowPlayerKills && source != null) {
				String srcLower = source.toLowerCase(Locale.ROOT);
				if (!HOSTILE_MOBS.contains(srcLower) && !srcLower.contains("[")) {
					// assume player
					return false;
				}
			}

			// check hidden players
			if (DeathMessagesConfigManager.CONFIG.hiddenPlayers != null) {
				for (String s : DeathMessagesConfigManager.CONFIG.hiddenPlayers) {
					if (s != null && s.equalsIgnoreCase(player)) {
						LOGGER.debug("Death message suppressed (hidden player): {} - {}", player, text);
						return true;
					}
				}
			}

			// check hidden causes/sources
			if (source != null) {
				if (DeathMessagesConfigManager.CONFIG.hiddenCauses != null) {
					for (String s : DeathMessagesConfigManager.CONFIG.hiddenCauses) {
						if (s != null && !s.isEmpty() && source.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))) {
							LOGGER.debug("Death message suppressed (hidden cause): {} - {}", s, text);
							return true;
						}
					}
				}
				if (DeathMessagesConfigManager.CONFIG.hiddenSources != null) {
					for (String s : DeathMessagesConfigManager.CONFIG.hiddenSources) {
						if (s != null && !s.isEmpty() && source.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))) {
							LOGGER.debug("Death message suppressed (hidden source): {} - {}", s, text);
							return true;
						}
					}
				}
			}

			long now = System.currentTimeMillis();
			long windowMs = catSettings.timeWindowSeconds * 1000L;
			int max = catSettings.maxDeathsPerPlayer;

			synchronized (playerDeaths) {
				List<Long> list = playerDeaths.computeIfAbsent(playerKey, k -> new ArrayList<>());
				// remove old timestamps
				list.removeIf(t -> (now - t) > windowMs);
				if (list.size() >= max) {
					// suppressed
					boolean showNotice = DeathMessagesConfigManager.CONFIG.showSuppressionNotice;
					if (showNotice) {
						long last = lastSuppressionNotice.getOrDefault(playerKey, 0L);
						if ((now - last) / 1000L >= DeathMessagesConfigManager.CONFIG.suppressionNoticeCooldownSeconds) {
							lastSuppressionNotice.put(playerKey, now);
							showSuppressionChat(Component.literal("[DeathMessages] Suppressed repeated death messages for " + player));
						}
					}
					LOGGER.debug("Death message suppressed (rate limit): {} - {} (count: {}/{})", player, cat.name(), list.size(), max);
					return true;
				} else {
					list.add(now);
					return false;
				}
			}

		} catch (Throwable t) {
			LOGGER.error("Error processing death message", t);
		}
		return false;
	}

	private static void showSuppressionChat(Component message) {
		// simple notice to player
		try {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				mc.player.sendSystemMessage(message);
			}
		} catch (Throwable ignored) {}
	}

	private static class PatternEntry {
		final Pattern pattern;
		final DeathCategory category;
		PatternEntry(Pattern p, DeathCategory c) { this.pattern = p; this.category = c; }
	}
}

