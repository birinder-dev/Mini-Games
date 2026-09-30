package dev.alphy90.minigames.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.alphy90.minigames.MiniGames;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("minigames.json").toFile();

    private static ModConfig INSTANCE = new ModConfig();

    //options available
    public int cardDespawnSeconds = 300; //-1 for no despawn
    public boolean allowCardPickup = true; // use right click with card state disabled
    public float cardDamage = 0.0F; //hitting entity
    public boolean allowGambler = true; // npc spawning
    public boolean allowCrafting = true; // of table stool
    public int maxCardsInHand = 9;

    public static ModConfig get(){
        return INSTANCE;
    }

    public static void load(){
        if(CONFIG_FILE.exists()){
            try(FileReader reader = new FileReader(CONFIG_FILE)){
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if(INSTANCE == null) INSTANCE = new ModConfig();
            } catch (IOException e){
                MiniGames.LOGGER.error("Failed to load minigames.json, using defaults", e);
                INSTANCE = new ModConfig();
            }
        } else {
            save();
        }
    }

    public static void save(){
        try{
            CONFIG_FILE.getParentFile().mkdirs();
            try(FileWriter writer = new FileWriter(CONFIG_FILE)){
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e){
            MiniGames.LOGGER.error("Failed to save minigames.json", e);
        }
    }

    public static void reset(){
        INSTANCE = new ModConfig();
        save();
    }
}