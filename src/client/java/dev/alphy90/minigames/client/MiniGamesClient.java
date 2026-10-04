package dev.alphy90.minigames.client;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.client.render.CardProjectileEntityRenderer;
import dev.alphy90.minigames.client.render.EmptyEntityRenderer;
import dev.alphy90.minigames.client.render.GamblerEntityRenderer;
import dev.alphy90.minigames.network.PlaceCardPayload;
import dev.alphy90.minigames.network.ThrowCardPayLoad;
import dev.alphy90.minigames.network.CardStateS2CPayload;
import dev.alphy90.minigames.network.CardStateC2SPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.session.telemetry.ThreadedLogWriter;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MiniGamesClient implements ClientModInitializer {

	private static KeyBinding toggleCardsKey;
	private static int actionCooldown = 0;
	private static boolean shiftRightClickHandled = false;
	private static boolean lastSentActive = false;
	private static int lastSentCount = 0;
	private static boolean lastSentCharging = false;

	@Override
	public void onInitializeClient() {

		BlockRenderLayerMap.INSTANCE.putBlock(MiniGames.PLACED_CARD, RenderLayer.getCutout());

		ClientPlayNetworking.registerGlobalReceiver(CardStateS2CPayload.ID, ((payload, context) -> {
			context.client().execute(() -> {
				PlayerCardSyncState.update(payload.entityId(), payload.active(), payload.count(), payload.charging());
			});
		}));

		CardHandCommand.register();

		// Shift + Right Click to enter or exit card state
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (hand == Hand.MAIN_HAND && player.isSneaking()) {
				ItemStack stack = player.getStackInHand(hand);
				if (stack.isOf(MiniGames.TAVERN_CARD) || CardClientState.isActive()) {
					if (world.isClient()) {
						if (!shiftRightClickHandled && actionCooldown == 0) {
							shiftRightClickHandled = true;
							actionCooldown = 10;
							if (CardClientState.isActive()) {
								CardClientState.setActive(false);
								CardClientState.resetHold();
								if (CardClientState.getOriginSlot() >= 0 && CardClientState.getOriginSlot() < 9) {
									player.getInventory().selectedSlot = CardClientState.getOriginSlot();
								}
							} else if (stack.isOf(MiniGames.TAVERN_CARD)) {
								int count = Math.min(9, Math.max(1, stack.getCount()));
								CardClientState.setOriginSlot(player.getInventory().selectedSlot);
								CardClientState.setCardCount(count);
								CardClientState.setActive(true);
							}
						}
					}
					return TypedActionResult.success(stack);
				}
			}
			return TypedActionResult.pass(player.getStackInHand(hand));
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (hand == Hand.MAIN_HAND && player.isSneaking()) {
				ItemStack stack = player.getStackInHand(hand);
				if (stack.isOf(MiniGames.TAVERN_CARD) || CardClientState.isActive()) {
					if (world.isClient()) {
						if (!shiftRightClickHandled && actionCooldown == 0) {
							shiftRightClickHandled = true;
							actionCooldown = 10;
							if (CardClientState.isActive()) {
								CardClientState.setActive(false);
								CardClientState.resetHold();
								if (CardClientState.getOriginSlot() >= 0 && CardClientState.getOriginSlot() < 9) {
									player.getInventory().selectedSlot = CardClientState.getOriginSlot();
								}
							} else if (stack.isOf(MiniGames.TAVERN_CARD)) {
								int count = Math.min(9, Math.max(1, stack.getCount()));
								CardClientState.setOriginSlot(player.getInventory().selectedSlot);
								CardClientState.setCardCount(count);
								CardClientState.setActive(true);
							}
						}
					}
					return ActionResult.SUCCESS;
				}
			}
			return ActionResult.PASS;
		});

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
		EntityRendererRegistry.register(MiniGames.CARD_PROJECTILE, CardProjectileEntityRenderer::new);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.player == null || client.world == null) return;

			if(actionCooldown > 0){
				actionCooldown--;
			}

			if(!client.options.useKey.isPressed()){
				shiftRightClickHandled = false;
			}

			boolean currentActive = CardClientState.isActive();
			int currentCount = CardClientState.getCardCount();
			boolean currentCharging = CardClientState.isThrowCharging();

			if(currentActive != lastSentActive || currentCount != lastSentCount || currentCharging != lastSentCharging){
				lastSentActive = currentActive;
				lastSentCount = currentCount;
				lastSentCharging = currentCharging;

				ClientPlayNetworking.send(new CardStateC2SPayload(currentActive, currentCount, currentCharging));
			}

			if (CardClientState.isFlicking()){
				CardClientState.tickFlick();

				if(CardClientState.getFlickTicks() == 0 && CardClientState.hasPendingThrow()){
					float power = CardClientState.getPendingPower();
					ClientPlayNetworking.send(new ThrowCardPayLoad(power));
					CardClientState.removeOneCard();
					client.player.swingHand(Hand.MAIN_HAND);
					CardClientState.completeThrow();
					actionCooldown = 5;
					if(!CardClientState.isActive() && CardClientState.getOriginSlot() >= 0 && CardClientState.getOriginSlot() < 9){
						client.player.getInventory().selectedSlot = CardClientState.getOriginSlot();
					}
				}
			}

			if(CardClientState.isActive() && CardClientState.getCardCount() > 0){
				if(client.player.isSneaking() && client.options.useKey.isPressed()){
					if(!shiftRightClickHandled && actionCooldown == 0){
						shiftRightClickHandled = true;
						actionCooldown = 10;
						CardClientState.setActive(false);
						CardClientState.resetHold();
						if(CardClientState.getOriginSlot() >= 0 && CardClientState.getOriginSlot() < 9){
							client.player.getInventory().selectedSlot = CardClientState.getOriginSlot();
						}
					}
					return;
				}
				int selectedSlot = client.player.getInventory().selectedSlot;
				boolean cardSelected = CardClientState.isCardSlot(selectedSlot);

				if(client.options.useKey.isPressed() && cardSelected && !CardClientState.isFlicking()){
					CardClientState.incrementHold();
				} else {
					int held = CardClientState.getHoldTicks();

					if(held > 0 && actionCooldown == 0) {
						// quick tap(4.5 blocks)
						if (held < 20) {
							HitResult crosshair = client.crosshairTarget;
							if (crosshair != null && crosshair.getType() == HitResult.Type.BLOCK && crosshair instanceof BlockHitResult) {
								BlockHitResult blockHit = (BlockHitResult) crosshair;
								double distSq = client.player.getEyePos().squaredDistanceTo(blockHit.getPos());

								BlockPos hitPos = blockHit.getBlockPos();
								BlockPos placePos = hitPos.offset(blockHit.getSide());

								boolean isCard = client.world.getBlockState(hitPos).isOf(MiniGames.PLACED_CARD) ||
								                 client.world.getBlockState(placePos).isOf(MiniGames.PLACED_CARD);

								if (distSq <= 20.25 && !isCard) {
									ClientPlayNetworking.send(new PlaceCardPayload(hitPos, blockHit.getSide()));
									CardClientState.removeOneCard();
									client.player.swingHand(Hand.MAIN_HAND);
									actionCooldown = 5;
									if (!CardClientState.isActive() && CardClientState.getOriginSlot() >= 0 && CardClientState.getOriginSlot() < 9) {
										client.player.getInventory().selectedSlot = CardClientState.getOriginSlot();
									}
								}
							}
						} else if (CardClientState.isThrowCharging() && cardSelected) {
							// holding >1sec
							float power = CardClientState.getThrowPower();

							CardClientState.startFlick(power);
						}
					}
					CardClientState.resetHold();
				}
			} else {
				CardClientState.resetHold();
			}
		});
	}
}