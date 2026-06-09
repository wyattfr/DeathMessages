# DeathMessages

A client-side Fabric mod that filters and suppresses repetitive death messages in Minecraft chat.

## Features

- **Death Message Filtering**: Suppress repetitive death messages based on configurable thresholds
- **Message Categorization**: Different filtering rules for different types of deaths (player kills, falls, drowning, etc.)
- **Whitelist Support**: Always show specific players' deaths or messages containing certain text
- **Blacklist Support**: Hide deaths of specific players or caused by specific entities
- **ModMenu Integration**: Full configuration support through ModMenu with Cloth Config
- **Chat Commands**: Administrative commands for quick configuration changes

## Configuration

This mod is configured through ModMenu. Open the mods menu and click the config button next to DeathMessages to access the configuration screen.

### Configuration Options

- **Global Settings**: Enable/disable the mod, set global rate limits
- **Category Settings**: Fine-tune filtering for specific death types
- **Filters**: Manage player/cause/source/text blacklists and whitelists

## Chat Commands

Use `/deathmessages` followed by:
- `reload` - Reload configuration from disk
- `save` - Save current configuration to disk
- `status` - Show current settings
- `set <max>` - Set max deaths per player
- `player mute <name>` - Hide deaths for a player
- `player unmute <name>` - Show deaths for a player
- `type mute <category>` - Hide a death category
- `type unmute <category>` - Show a death category

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up).

## License

This project is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
