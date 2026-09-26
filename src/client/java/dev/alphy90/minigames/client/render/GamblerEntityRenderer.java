package dev.alphy90.minigames.client.render;

import dev.alphy90.minigames.entity.GamblerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;   // gotta check this later
import net.minecraft.util.Identifier;

public class GamblerEntityRenderer extends MobEntityRenderer<GamblerEntity, VillagerResemblingModel<GamblerEntity>>{  //method is resemble while there was rescale
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/entity/villager/type/plains.png");

    public GamblerEntityRenderer(EntityRendererFactory.Context context){
        super(context, new VillagerResemblingModel<>(context.getPart(EntityModelLayers.VILLAGER)), 0.5f);
    }

    @Override
    public Identifier getTexture(GamblerEntity entity){
        return TEXTURE;
    }
}
