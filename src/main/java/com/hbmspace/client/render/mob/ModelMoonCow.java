// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.hbm.backport.client.core.EntityModel;
import net.minecraft.util.Mth;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import com.hbm.backport.client.core.LivingEntityRenderState;

// backport: the 26.x QuadrupedModel poses from a render state; 1.21.1's poses from the entity, so
// the moon cow is a state model on the core's shim with the same quadruped pose (head look, legs)
public class ModelMoonCow extends EntityModel<LivingEntityRenderState> {

    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public ModelMoonCow(ModelPart root) {
        super(root);
        head = root.getChild("head");
        rightHindLeg = root.getChild("right_hind_leg");
        leftHindLeg = root.getChild("left_hind_leg");
        rightFrontLeg = root.getChild("right_front_leg");
        leftFrontLeg = root.getChild("left_front_leg");
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        float pos = state.walkAnimationPos, speed = state.walkAnimationSpeed;
        rightHindLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        leftHindLeg.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed;
        rightFrontLeg.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed;
        leftFrontLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
    }

    public ModelPart head() {
        return head;
    }

    private static MeshDefinition mesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -6.0F, 8, 8, 6)
                        .texOffs(22, 0)
                        .addBox(-5.0F, -5.0F, -4.0F, 1, 3, 1)
                        .texOffs(22, 0)
                        .addBox(4.0F, -5.0F, -4.0F, 1, 3, 1),
                PartPose.offset(0.0F, 4.0F, -8.0F));
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(18, 4)
                        .addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10)
                        .texOffs(52, 0)
                        .addBox(-2.0F, 2.0F, -8.0F, 4, 6, 1),
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2.0D), 0.0F, 0.0F));
        CubeListBuilder leg =
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-4.0F, 12.0F, -6.0F));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(4.0F, 12.0F, -6.0F));
        return mesh;
    }

    public static LayerDefinition adult() {
        return LayerDefinition.create(mesh(), 64, 32);
    }

    // backport: 26.x baby = adult mesh with CowModel.BABY_TRANSFORMER; here the renderer scales the
    // adult model by half for babies
    public static LayerDefinition baby() {
        return adult();
    }
}
