package org.nightfall.deathmessages.client.command;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.nightfall.deathmessages.client.config.CategorySettings;
import org.nightfall.deathmessages.client.config.DeathCategory;
import org.nightfall.deathmessages.client.config.DeathMessagesConfigManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DeathMessagesCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            com.mojang.brigadier.builder.LiteralArgumentBuilder<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> root = com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("deathmessages").requires(src -> true);

            // reload
            root.then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                    .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("reload").executes(ctx -> {
                        DeathMessagesConfigManager.load();
                        sendFeedbackSuccess("Config reloaded.");
                        return SINGLE_SUCCESS;
                    }));

            // save
            root.then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                    .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("save").executes(ctx -> {
                        DeathMessagesConfigManager.save();
                        sendFeedbackSuccess("Config saved.");
                        return SINGLE_SUCCESS;
                    }));

            // status
            root.then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                    .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("status").executes(ctx -> {
                        sendFeedbackInfo("Enabled: " + DeathMessagesConfigManager.CONFIG.enabled + ", maxPerPlayer: " + DeathMessagesConfigManager.CONFIG.maxDeathsPerPlayer + ", window: " + DeathMessagesConfigManager.CONFIG.timeWindowSeconds + "s");
                        return SINGLE_SUCCESS;
                    }));


            // set global values
            root.then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                    .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("set")
                    .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                            .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, Integer>argument("max", com.mojang.brigadier.arguments.IntegerArgumentType.integer()).executes(ctx -> {
                                int max = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "max");
                                DeathMessagesConfigManager.CONFIG.maxDeathsPerPlayer = Math.max(0, max);
                                DeathMessagesConfigManager.save();
                                sendFeedbackSuccess("Set global max deaths per player to " + DeathMessagesConfigManager.CONFIG.maxDeathsPerPlayer);
                                return SINGLE_SUCCESS;
                            }))
                    .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                            .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, Integer>argument("window", com.mojang.brigadier.arguments.IntegerArgumentType.integer()).executes(ctx -> {
                                int w = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "window");
                                DeathMessagesConfigManager.CONFIG.timeWindowSeconds = Math.max(0, w);
                                DeathMessagesConfigManager.save();
                                sendFeedbackSuccess("Set global time window to " + DeathMessagesConfigManager.CONFIG.timeWindowSeconds + "s");
                                return SINGLE_SUCCESS;
                            })));

            // player mute/unmute
            root.then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                    .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("player")
                    .then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                            .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("mute").then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                                    .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, String>argument("name", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
                                        String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                                        java.util.List<String> hidden = new java.util.ArrayList<>(java.util.Arrays.asList(DeathMessagesConfigManager.CONFIG.hiddenPlayers));
                                        if (!hidden.contains(name)) hidden.add(name);
                                        DeathMessagesConfigManager.CONFIG.hiddenPlayers = hidden.toArray(new String[0]);
                                        DeathMessagesConfigManager.save();
                                        sendFeedbackSuccess("Muted player: " + name);
                                        return SINGLE_SUCCESS;
                                    })))
                    .then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                            .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("unmute").then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                                    .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, String>argument("name", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
                                        String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                                        java.util.List<String> hidden = new java.util.ArrayList<>(java.util.Arrays.asList(DeathMessagesConfigManager.CONFIG.hiddenPlayers));
                                        hidden.removeIf(s -> s.equalsIgnoreCase(name));
                                        DeathMessagesConfigManager.CONFIG.hiddenPlayers = hidden.toArray(new String[0]);
                                        DeathMessagesConfigManager.save();
                                        sendFeedbackSuccess("Unmuted player: " + name);
                                        return SINGLE_SUCCESS;
                                    }))));

            // type mute/unmute (category)
            com.mojang.brigadier.builder.LiteralArgumentBuilder<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> typeRoot = com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("type");

            typeRoot.then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                    .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("mute").then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                            .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, String>argument("category", com.mojang.brigadier.arguments.StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (org.nightfall.deathmessages.client.config.DeathCategory cat : org.nightfall.deathmessages.client.config.DeathCategory.values()) {
                                    builder.suggest(cat.name().toLowerCase(java.util.Locale.ROOT));
                                }
                                return builder.buildFuture();
                            }).executes(ctx -> {
                                String c = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "category");
                                try {
                                    DeathCategory cat = DeathCategory.valueOf(c.toUpperCase(java.util.Locale.ROOT));
                                    CategorySettings s = DeathMessagesConfigManager.CONFIG.categorySettings.get(cat.name());
                                    if (s == null) s = new CategorySettings();
                                    s.enabled = false;
                                    DeathMessagesConfigManager.CONFIG.categorySettings.put(cat.name(), s);
                                    DeathMessagesConfigManager.save();
                                    sendFeedbackSuccess("Muted category: " + cat.name());
                                } catch (IllegalArgumentException ex) {
                                    sendFeedbackError("Unknown category: " + c);
                                }
                                return SINGLE_SUCCESS;
                            })));

            typeRoot.then(com.mojang.brigadier.builder.LiteralArgumentBuilder
                    .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("unmute").then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                            .<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource, String>argument("category", com.mojang.brigadier.arguments.StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (org.nightfall.deathmessages.client.config.DeathCategory cat : org.nightfall.deathmessages.client.config.DeathCategory.values()) {
                                    builder.suggest(cat.name().toLowerCase(java.util.Locale.ROOT));
                                }
                                return builder.buildFuture();
                            }).executes(ctx -> {
                                String c = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "category");
                                try {
                                    DeathCategory cat = DeathCategory.valueOf(c.toUpperCase(java.util.Locale.ROOT));
                                    CategorySettings s = DeathMessagesConfigManager.CONFIG.categorySettings.get(cat.name());
                                    if (s == null) s = new CategorySettings();
                                    s.enabled = true;
                                    DeathMessagesConfigManager.CONFIG.categorySettings.put(cat.name(), s);
                                    DeathMessagesConfigManager.save();
                                    sendFeedbackSuccess("Unmuted category: " + cat.name());
                                } catch (IllegalArgumentException ex) {
                                    sendFeedbackError("Unknown category: " + c);
                                }
                                return SINGLE_SUCCESS;
                            })));

            root.then(typeRoot);

            dispatcher.register(root);
        });
    }

    private static void sendFeedbackInfo(String s) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal("[DeathMessages] " + s).withStyle(ChatFormatting.AQUA));
        }
    }

	private static void sendFeedbackSuccess(String s) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			mc.player.sendSystemMessage(Component.literal("[DeathMessages] " + s).withStyle(ChatFormatting.GREEN));
		}
	}

	private static void sendFeedbackError(String s) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			mc.player.sendSystemMessage(Component.literal("[DeathMessages] " + s).withStyle(ChatFormatting.RED));
		}
	}
}



