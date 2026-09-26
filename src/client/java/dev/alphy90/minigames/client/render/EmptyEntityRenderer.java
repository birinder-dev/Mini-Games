package dev.alphy90.minigames.client.render;

import dev.alphy90.minigames.entity.SeatEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.util.Identifier;

public class EmptyEntityRenderer extends EntityRenderer<SeatEntity>{
    public EmptyEntityRenderer(EntityRendererFactory.Context ctx){
        super(ctx);
    }

    @Override
    public Identifier getTexture(SeatEntity entity){
        return null;
    }

    @Override
    public boolean shouldRender(SeatEntity entity, Frustum frustum, double x, double y, double z){
        return false;
    }
}