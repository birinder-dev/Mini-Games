package dev.alphy90.minigames.client.render;

import dev.alphy90.minigames.entity.GamblerEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;

public class GamblerEntityModel extends VillagerResemblingModel<GamblerEntity>{
    private final ModelPart jacket;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart arms;

    public GamblerEntityModel(ModelPart root){
        super(root);
        this.jacket = root.getChild("body").getChild("jacket");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.arms = root.getChild("arms");
    }

    public ModelPart getArms(){
        return this.arms;
    }

    @Override
    public void setAngles(GamblerEntity entity, float limbAngle, float limbDistance, float aniamtionProgress, float headYaw, float headPitch){
        super.setAngles(entity, limbAngle, limbDistance, aniamtionProgress, headYaw, headPitch);

        if(this.riding || entity.hasVehicle()){
            this.jacket.visible = false;

            this.rightLeg.pitch = -1.4137167F;
            this.rightLeg.yaw = ((float) Math.PI / 10.0F);
            this.rightLeg.roll = 0.07853982F;

            this.leftLeg.pitch = -1.4137167F;
            this.leftLeg.yaw = -((float) Math.PI / 10.0F);
            this.leftLeg.roll = -0.07853982F;

            this.arms.pitch = -0.75F;
        } else {
            this.jacket.visible = true;
            this.arms.pitch = 0.0F;
            this.rightLeg.yaw = 0.0F;
            this.rightLeg.roll = 0.0F;
            this.leftLeg.yaw = 0.0F;
            this.leftLeg.roll = 0.0F;
            this.rightLeg.pivotY = 12.0F;
            this.leftLeg.pivotY = 12.0F;
            this.rightLeg.pivotZ = 0.0F;
            this.leftLeg.pivotZ = 0.0F;
        }
    }
}