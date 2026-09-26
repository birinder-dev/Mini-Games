package dev.alphy90.minigames;

import dev.alphy90.minigames.block.GamblingStoolBlock;
import dev.alphy90.minigames.entity.GamblerEntity;
import dev.alphy90.minigames.entity.SeatEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniGames implements ModInitializer {
	public static final String MOD_ID = "minigames";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Block GAMBLING_STOOL = new GamblingStoolBlock(AbstractBlock.Settings.create().nonOpaque().strength(1.5F));
	public static final Item GAMBLING_STOOL_ITEM = new BlockItem(GAMBLING_STOOL, new Item.Settings());


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
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}