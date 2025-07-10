package com.skibidicausa.subtitlemanager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class SubtitleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("subtitleowleaf")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", StringArgumentType.string())
                        .then(Commands.argument("duration", IntegerArgumentType.integer(1, 300))
                                .then(Commands.argument("text", StringArgumentType.greedyString())
                                        .executes(SubtitleCommand::executeCommand)
                                )
                        )
                )
        );

        dispatcher.register(Commands.literal("clearsubtitles")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                        SubtitleNetworking.sendClearPacket(player);
                    }
                    context.getSource().sendSuccess(() -> Component.literal("Subtítulos limpiados"), true);
                    return 1;
                })
        );
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        int duration = IntegerArgumentType.getInteger(context, "duration");
        String text = StringArgumentType.getString(context, "text");

        int durationTicks = duration * 20;

        try {
            if (playerName.equals("@a") || playerName.equals("@all")) {
                for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                    SubtitleNetworking.sendSubtitlePacket(player, text, durationTicks);
                }
                context.getSource().sendSuccess(() -> Component.literal("Subtítulo enviado a todos los jugadores: \"" + text + "\" (" + duration + "s)"), true);
            } else if (playerName.equals("@s")) {
                if (context.getSource().getEntity() instanceof ServerPlayer player) {
                    SubtitleNetworking.sendSubtitlePacket(player, text, durationTicks);
                    context.getSource().sendSuccess(() -> Component.literal("Subtítulo enviado a ti: \"" + text + "\" (" + duration + "s)"), true);
                } else {
                    context.getSource().sendFailure(Component.literal("Solo jugadores pueden usar @s"));
                    return 0;
                }
            } else {
                ServerPlayer targetPlayer = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);
                if (targetPlayer != null) {
                    SubtitleNetworking.sendSubtitlePacket(targetPlayer, text, durationTicks);
                    context.getSource().sendSuccess(() -> Component.literal("Subtítulo enviado a " + playerName + ": \"" + text + "\" (" + duration + "s)"), true);
                } else {
                    context.getSource().sendFailure(Component.literal("Jugador no encontrado: " + playerName));
                    return 0;
                }
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error al enviar subtítulo: " + e.getMessage()));
            SubtitleManagerMod.LOGGER.error("Error sending subtitle", e);
            return 0;
        }

        return 1;
    }
}