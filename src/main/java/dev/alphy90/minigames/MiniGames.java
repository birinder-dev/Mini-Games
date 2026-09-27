package dev.alphy90.minigames;

import dev.alphy90.minigames.block.GamblingStoolBlock;
import dev.alphy90.minigames.block.CardBlock;
import dev.alphy90.minigames.network.PlaceCardPayload;
import dev.alphy90.minigames.entity.GamblerEntity;
import dev.alphy90.minigames.entity.SeatEntity;
import dev.alphy90.minigames.entity.CardProjectileEntity;
import dev.alphy90.minigames.network.ThrowCardPayLoad;
import dev.alphy90.minigames.network.PlaceCardPayload;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniGames implements ModInitializer {
	public static final String MOD_ID = "minigames";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Block GAMBLING_STOOL = new GamblingStoolBlock(AbstractBlock.Settings.create().nonOpaque().strength(1.5F));
	public static final Item GAMBLING_STOOL_ITEM = new BlockItem(GAMBLING_STOOL, new Item.Settings());
	public static final Block PLACED_CARD = new CardBlock(
			AbstractBlock.Settings.create()
					.breakInstantly()
					.noCollision()
					.nonOpaque()
					.sounds(BlockSoundGroup.WOOL)
	);


	public static final EntityType<CardProjectileEntity> CARD_PROJECTILE = Registry.register(
			Registries.ENTITY_TYPE,
			id("card_projectile"),
			EntityType.Builder.<CardProjectileEntity>create(CardProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.4F, 0.4F)
					.maxTrackingRange(4)
					.trackingTickInterval(20)
					.build()
	);

	public static final EntityType<GamblerEntity> GAMBLER = Registry.register(
			Registries.ENTITY_TYPE,
			id("gambler"),
			EntityType.Builder.create(GamblerEntity::new, SpawnGroup.CREATURE).dimensions(0.6F, 1.95F).build()
	);

	public static final EntityType<SeatEntity> SEAT_ENTITY = Registry.register(
			Registries.ENTITY_TYPE,
			id("seat"),
			EntityType.Builder.<SeatEntity>create(SeatEntity::new, SpawnGroup.MISC).dimensions(0.0F, 0.0F).build()
	);

	public static final Item TAVERN_CARD = new Item(new Item.Settings().maxCount(64));

	public static final Item GAMBLER_SPAWN_EGG = new SpawnEggItem(
			GAMBLER,
			0x563c24, //base:coat brown
			0xecb22e,  //speckle: gold
			new Item.Settings()
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.

		LOGGER.info("Initializing Minigames...");

		Registry.register(Registries.ITEM, id("tavern_card"), TAVERN_CARD);
		Registry.register(Registries.BLOCK, id("gambling_stool"), GAMBLING_STOOL);
		Registry.register(Registries.ITEM, id("gambling_stool"), GAMBLING_STOOL_ITEM);
		Registry.register(Registries.ITEM, id("gambler_spawn_Egg"), GAMBLER_SPAWN_EGG);
		Registry.register(Registries.BLOCK, id("placed_card"), PLACED_CARD);

		FabricDefaultAttributeRegistry.register(GAMBLER, GamblerEntity.createGamblerAttributes());

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
			entries.add(GAMBLER_SPAWN_EGG);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
			entries.add(GAMBLING_STOOL_ITEM);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(TAVERN_CARD);
		});

		// flat placement networking
		PayloadTypeRegistry.playC2S().register(PlaceCardPayload.ID, PlaceCardPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PlaceCardPayload.ID, ((payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = player.getServerWorld();
				BlockPos targetPos = payload.pos().offset(payload.side());
				Direction facing = payload.side();

				if(world.getBlockState(targetPos).isAir()){
					BlockState state = PLACED_CARD.getDefaultState().with(CardBlock.FACING, facing);
					if(state.canPlaceAt(world, targetPos)){
						world.setBlockState(targetPos, state);
						world.playSound(null, targetPos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 0.8F, 1.3F);
					}
				}
			});
		}));

		// projectile throw networking
		PayloadTypeRegistry.playC2S().register(ThrowCardPayLoad.ID, ThrowCardPayLoad.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ThrowCardPayLoad.ID, ((payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = player.getServerWorld();

				CardProjectileEntity projectile = new CardProjectileEntity(world, player, payload.power());
				world.spawnEntity(projectile);

				world.playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS,
						0.6F, 1.2F + (payload.power() * 0.4F));
			});
		}));
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}