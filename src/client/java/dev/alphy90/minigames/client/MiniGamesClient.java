package dev.alphy90.minigames.client;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.client.render.EmptyEntityRenderer;
import dev.alphy90.minigames.client.render.GamblerEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MiniGamesClient implements ClientModInitializer {

	private static KeyBinding toggleCardsKey;

	@Override
	public void onInitializeClient() {

		CardHandCommand.register();

		toggleCardsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"Toggle 3D Cards",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_O,
				"Minigames"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleCardsKey.wasPressed()) {
				CardClientState.toggle(5);
			}
		});

		EntityRendererRegistry.register(MiniGames.GAMBLER, GamblerEntityRenderer::new);
		EntityRendererRegistry.register(MiniGames.SEAT_ENTITY, EmptyEntityRenderer::new);
	}
}