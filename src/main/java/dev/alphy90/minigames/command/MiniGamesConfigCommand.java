package dev.alphy90.minigames.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import dev.alphy90.minigames.config.ModConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MiniGamesConfigCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("minigames")
                // Require OP level 2+
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("config")
                        .then(CommandManager.literal("get").executes(context -> sendConfigStatus(context.getSource())))
                        .then(CommandManager.literal("reload").executes(context -> {
                            ModConfig.load();
                            context.getSource().sendFeedback(() -> Text.literal("[MiniGames] Config reloaded from disk!").formatted(Formatting.GREEN), true);
                            return 1;
                        }))
                        .then(CommandManager.literal("reset").executes(context -> {
                            ModConfig.reset();
                            context.getSource().sendFeedback(() -> Text.literal("[MiniGames] Config reset to default values!").formatted(Formatting.YELLOW), true);
                            return 1;
                        }))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.literal("despawn")
                                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(-1, 86400))
                                                .executes(context -> {
                                                    int val = IntegerArgumentType.getInteger(context, "seconds");
                                                    ModConfig.get().cardDespawnSeconds = val;
                                                    ModConfig.save();
                                                    context.getSource().sendFeedback(() -> Text.literal("[MiniGames] Card despawn set to: " + (val < 0 ? "Never" : val + "s")).formatted(Formatting.GREEN), true);
                                                    return 1;
                                                })))
                                .then(CommandManager.literal("damage")
                                        .then(CommandManager.argument("amount", FloatArgumentType.floatArg(0.0F, 100.0F))
                                                .executes(context -> {
                                                    float val = FloatArgumentType.getFloat(context, "amount");
                                                    ModConfig.get().cardDamage = val;
                                                    ModConfig.save();
                                                    context.getSource().sendFeedback(() -> Text.literal("[MiniGames] Card hit damage set to: " + val).formatted(Formatting.GREEN), true);
                                                    return 1;
                                                })))
                                .then(CommandManager.literal("pickup")
                                        .then(CommandManager.argument("allowed", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean val = BoolArgumentType.getBool(context, "allowed");
                                                    ModConfig.get().allowCardPickup = val;
                                                    ModConfig.save();
                                                    context.getSource().sendFeedback(() -> Text.literal("[MiniGames] Card pickup allowed: " + val).formatted(Formatting.GREEN), true);
                                                    return 1;
                                                })))
                                .then(CommandManager.literal("gambler")
                                        .then(CommandManager.argument("allowed", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean val = BoolArgumentType.getBool(context, "allowed");
                                                    ModConfig.get().allowGambler = val;
                                                    ModConfig.save();
                                                    context.getSource().sendFeedback(() -> Text.literal("[MiniGames] Gambler NPC allowed: " + val).formatted(Formatting.GREEN), true);
                                                    return 1;
                                                })))
                                .then(CommandManager.literal("crafting")
                                        .then(CommandManager.argument("allowed", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean val = BoolArgumentType.getBool(context, "allowed");
                                                    ModConfig.get().allowCrafting = val;
                                                    ModConfig.save();
                                                    context.getSource().sendFeedback(() -> Text.literal("[MiniGames] Table crafting allowed: " + val).formatted(Formatting.GREEN), true);
                                                    return 1;
                                                })))
                                .then(CommandManager.literal("maxcards")
                                        .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 9))
                                                .executes(context -> {
                                                    int val = IntegerArgumentType.getInteger(context, "count");
                                                    ModConfig.get().maxCardsInHand = val;
                                                    ModConfig.save();
                                                    context.getSource().sendFeedback(() -> Text.literal("[MiniGames] Max cards in hand & stack size set to: " + val).formatted(Formatting.GREEN), true);
                                                    return 1;
                                                })))
                        )
                )
        );
    }

    private static int sendConfigStatus(ServerCommandSource source) {
        ModConfig c = ModConfig.get();
        source.sendMessage(Text.literal("=== MiniGames Server Config ===").formatted(Formatting.GOLD, Formatting.BOLD));
        source.sendMessage(Text.literal("Card Despawn: ").formatted(Formatting.YELLOW)
                .append(Text.literal(c.cardDespawnSeconds < 0 ? "Never" : c.cardDespawnSeconds + "s").formatted(Formatting.AQUA)));
        source.sendMessage(Text.literal("Card Pickup Allowed: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(c.allowCardPickup)).formatted(Formatting.AQUA)));
        source.sendMessage(Text.literal("Card Hit Damage: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(c.cardDamage)).formatted(Formatting.AQUA)));
        source.sendMessage(Text.literal("Gambler NPC Allowed: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(c.allowGambler)).formatted(Formatting.AQUA)));
        source.sendMessage(Text.literal("Table Crafting Allowed: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(c.allowCrafting)).formatted(Formatting.AQUA)));
        source.sendMessage(Text.literal("Max Cards in Hand & Stack: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(c.maxCardsInHand)).formatted(Formatting.AQUA)));
        return 1;
    }
}