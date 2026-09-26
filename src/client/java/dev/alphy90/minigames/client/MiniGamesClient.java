package dev.alphy90.minigames.client;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.client.render.EmptyEntityRenderer;
import dev.alphy90.minigames.client.render.GamblerEntityRenderer;
import dev.alphy90.minigames.network.PlaceCardPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MiniGamesClient implements ClientModInitializer {

	private static KeyBinding toggleCardsKey;

	@Override
	public void onInitializeClient() {

		BlockRenderLayerMap.INSTANCE.putBlock(MiniGames.PLACED_CARD, RenderLayer.getCutout());

		UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
			if(world.isClient() && hand == Hand.MAIN_HAND && CardClientState.isActive() && CardClientState.getCardCount() > 0){
				ClientPlayNetworking.send(new PlaceCardPayload(hitResult.getBlockPos(), hitResult.getSide()));
				CardClientState.removeOneCard(); // minus 1 and re centre
				player.swingHand(hand);

				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		}));

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