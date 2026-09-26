package dev.alphy90.minigames.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class CardHandCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("cardhand")
                    // Sub-command: /cardhand off
                    .then(ClientCommandManager.literal("off")
                            .executes(context -> {
                                CardClientState.setActive(false);
                                context.getSource().sendFeedback(Text.literal("§cCard hand disabled."));
                                return 1;
                            })
                    )
                    // Sub-command: /cardhand <count>
                    .then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1, 9))
                            .executes(context -> {
                                int count = IntegerArgumentType.getInteger(context, "count");
                                CardClientState.setActive(true);
                                CardClientState.setCardCount(count);
                                // Changed the color code from §c (red) to §a (green) for enabling!
                                context.getSource().sendFeedback(Text.literal("§aCard hand enabled with " + count + " cards."));
                                return 1;
                            })
                    )
            );
        });
    }
}