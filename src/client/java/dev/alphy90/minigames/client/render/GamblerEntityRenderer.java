package dev.alphy90.minigames.client.render;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.block.GamblingTableBlock;
import dev.alphy90.minigames.entity.GamblerEntity;
import dev.alphy90.minigames.game.CardSuit;
import dev.alphy90.minigames.game.TavernCard;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class GamblerEntityRenderer extends MobEntityRenderer<GamblerEntity, GamblerEntityModel> {
    private static final Identifier BASE_TEXTURE = Identifier.ofVanilla("textures/entity/villager/villager.png");
    private static final Identifier PLAINS_TEXTURE = Identifier.ofVanilla("textures/entity/villager/type/plains.png");
    private static final Identifier LIBRARIAN_TEXTURE = Identifier.ofVanilla("textures/entity/villager/profession/librarian.png");

    public GamblerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new GamblerEntityModel(context.getPart(EntityModelLayers.VILLAGER)), 0.5f);
        this.addFeature(new GamblerClothingFeatureRenderer(this));
        this.addFeature(new GamblerHeldCardsFeatureRenderer(this));
    }

    @Override
    public Identifier getTexture(GamblerEntity entity) {
        return BASE_TEXTURE;
    }

    private static class GamblerClothingFeatureRenderer extends FeatureRenderer<GamblerEntity, GamblerEntityModel> {
        public GamblerClothingFeatureRenderer(FeatureRendererContext<GamblerEntity, GamblerEntityModel> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                GamblerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                float headYaw, float headPitch) {
            if (!entity.isInvisible()) {
                renderModel(this.getContextModel(), PLAINS_TEXTURE, matrices, vertexConsumers, light, entity, -1);
                renderModel(this.getContextModel(), LIBRARIAN_TEXTURE, matrices, vertexConsumers, light, entity, -1);
            }
        }
    }

    private static class GamblerHeldCardsFeatureRenderer extends FeatureRenderer<GamblerEntity, GamblerEntityModel> {
        private static final Identifier CARD_BACK_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card.png");
        private static final Identifier CARD_FRONT_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card_front.png");

        private static final TavernCard[] DUMMY_CARDS = new TavernCard[] {
                new TavernCard(CardSuit.SHIELD, 3),
                new TavernCard(CardSuit.SWORD, 7),
                new TavernCard(CardSuit.SHIELD, 5),
                new TavernCard(CardSuit.SWORD, 8),
                new TavernCard(CardSuit.SHIELD, 2)
        };

        public GamblerHeldCardsFeatureRenderer(FeatureRendererContext<GamblerEntity, GamblerEntityModel> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                GamblerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                float headYaw, float headPitch) {
            if (entity.isInvisible()) return;
            if (!entity.hasVehicle()) return;

            int leftCards = entity.getLeftCards();
            if (leftCards <= 0) return;

            BlockPos seatPos = entity.getVehicle().getBlockPos();
            boolean nearTable = false;
            for (Direction dir : Direction.Type.HORIZONTAL) {
                if (entity.getWorld().getBlockState(seatPos.offset(dir)).getBlock() instanceof GamblingTableBlock) {
                    nearTable = true;
                    break;
                }
            }
            if (!nearTable) return;

            matrices.push();
            this.getContextModel().getArms().rotate(matrices);
            matrices.translate(0.0F, 0.16F, -0.20F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(48.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(10.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-4.0F));

            float cardW = 0.22F;
            float cardH = 0.32F;
            float cardSpacing = leftCards <= 1 ? 0.0F : Math.min(0.045F, 0.20F / leftCards);
            float anglestep = leftCards <= 1 ? 0.0F : Math.min(6.5F, 28.0F / leftCards);

            for (int i = 0; i < leftCards; i++) {
                float t = (float) (i - (leftCards - 1) / 2.0F);
                matrices.push();
                matrices.translate(t * cardSpacing, -(t * t) * 0.003F, i * 0.003F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-t * anglestep));
                renderHeldCard(matrices, vertexConsumers, light, cardW, cardH, DUMMY_CARDS[i % DUMMY_CARDS.length]);
                matrices.pop();
            }

            matrices.pop();
        }

        private void renderHeldCard(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                float cardW, float cardH, TavernCard card) {
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f pos = entry.getPositionMatrix();
            float halfW = cardW / 2.0F;

            // Front face (facing gambler)
            VertexConsumer frontBuffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(CARD_FRONT_TEXTURE));
            frontBuffer.vertex(pos, -halfW, 0.0F, 0.0F).color(255, 255, 255, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
            frontBuffer.vertex(pos, halfW, 0.0F, 0.0F).color(255, 255, 255, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
            frontBuffer.vertex(pos, halfW, cardH, 0.0F).color(255, 255, 255, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
            frontBuffer.vertex(pos, -halfW, cardH, 0.0F).color(255, 255, 255, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);

            // Back face (facing opponent across table)
            VertexConsumer backBuffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(CARD_BACK_TEXTURE));
            backBuffer.vertex(pos, halfW, 0.0F, -0.001F).color(255, 255, 255, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
            backBuffer.vertex(pos, -halfW, 0.0F, -0.001F).color(255, 255, 255, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
            backBuffer.vertex(pos, -halfW, cardH, -0.001F).color(255, 255, 255, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
            backBuffer.vertex(pos, halfW, cardH, -0.001F).color(255, 255, 255, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);

            // Front details (symbols and numbers)
            if (card != null) {
                CardRenderHelper.renderCardFrontDetails(matrices, vertexConsumers, light, cardW, cardH, cardH / 2.0F, card);
            }
        }
    }
}
