package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.MiniGames;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import static dev.alphy90.minigames.MiniGames.MINI_GAMES_GROUP_KEY;

@Mixin(value = ItemGroups.class, priority = 1500)
public class ItemGroupsPageMixin {
    @Inject(method = "updateEntries", at = @At("TAIL"))
    private static void swapTab(CallbackInfo ci){
        ItemGroup minigames = Registries.ITEM_GROUP.get(MINI_GAMES_GROUP_KEY);
        ItemGroup buildingBlocks = Registries.ITEM_GROUP .get(ItemGroups.BUILDING_BLOCKS);
        if(minigames != null && buildingBlocks != null){
            try {
                java.lang.reflect.Method setPage = ItemGroup.class.getMethod("fabric_setPage", int.class);
                setPage.invoke(minigames, 0);
                setPage.invoke(buildingBlocks, 1);
            } catch (Exception ignored){
            }
        }
    }
}