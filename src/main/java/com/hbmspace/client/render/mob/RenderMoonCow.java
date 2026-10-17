// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.hbm.client.render.WorldRenderPipeline;
import com.hbm.lib.Library;
import com.hbm.render.util.Vertices;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.mob.EntityMoonCow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.hbm.lib.crankshaft.ConcurrentRenderStateExtraction;
import com.hbm.backport.client.core.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.hbm.backport.client.core.LivingEntityRenderer;
import com.hbm.backport.client.core.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import com.hbm.backport.client.core.LivingEntityRenderState;
import net.minecraft.client.renderer.RenderType;
import com.hbm.backport.client.core.CameraRenderState;
import net.minecraft.resources.ResourceLocation;

public class RenderMoonCow extends MobRenderer<EntityMoonCow, LivingEntityRenderState, ModelMoonCow>
        implements ConcurrentRenderStateExtraction {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, "textures/entity/moon_cow.png");

    public RenderMoonCow(EntityRendererProvider.Context context) {
        super(context, new ModelMoonCow(ModelMoonCow.adult().bakeRoot()), 0.7F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public void submit(
            LivingEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        Helmet.submit(this, state, poseStack, collector);
    }

    // backport: babies are the adult model at half size (26.x: CowModel.BABY_TRANSFORMER)
    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        if (state.isBaby) poseStack.scale(0.5F, 0.5F, 0.5F);
    }

    // backport: the core's state renderer has no render layers, so the glass helmet (a 26.x layer) is
    // drawn after the model with the same transforms
    private static final class Helmet {

        private static final RenderType GLASS =
                WorldRenderPipeline.oneSidedCutout(Library.id("textures/block/glass_boron.png"));
        private static final float[][] FACES = {
            {
                -0.5F, 0.5F, -0.5F, 1, 1, 0.5F, 0.5F, -0.5F, 0, 1, 0.5F, -0.5F, -0.5F, 0, 0, -0.5F,
                -0.5F, -0.5F, 1, 0
            },
            {
                -0.5F, 0.5F, 0.5F, 1, 1, -0.5F, 0.5F, -0.5F, 0, 1, -0.5F, -0.5F, -0.5F, 0, 0, -0.5F,
                -0.5F, 0.5F, 1, 0
            },
            {
                0.5F, 0.5F, 0.5F, 1, 1, -0.5F, 0.5F, 0.5F, 0, 1, -0.5F, -0.5F, 0.5F, 0, 0, 0.5F,
                -0.5F, 0.5F, 1, 0
            },
            {
                0.5F, 0.5F, -0.5F, 1, 1, 0.5F, 0.5F, 0.5F, 0, 1, 0.5F, -0.5F, 0.5F, 0, 0, 0.5F,
                -0.5F, -0.5F, 1, 0
            },
            {
                -0.5F, -0.5F, -0.5F, 1, 1, 0.5F, -0.5F, -0.5F, 0, 1, 0.5F, -0.5F, 0.5F, 0, 0, -0.5F,
                -0.5F, 0.5F, 1, 0
            },
            {
                0.5F, 0.5F, -0.5F, 1, 1, -0.5F, 0.5F, -0.5F, 0, 1, -0.5F, 0.5F, 0.5F, 0, 0, 0.5F,
                0.5F, 0.5F, 1, 0
            }
        };
        private static final float[][] NORMALS = {
            {0, 0, -1}, {-1, 0, 0}, {0, 0, 1}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}
        };

        static void submit(
                RenderMoonCow renderer,
                LivingEntityRenderState state,
                PoseStack poseStack,
                SubmitNodeCollector collector) {
            if (state.isInvisible) return;
            int lightCoords = state.lightCoords;
            int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
            poseStack.pushPose();
            float scale = state.scale;
            poseStack.scale(scale, scale, scale);
            renderer.setupRotations(state, poseStack, state.bodyRot, scale);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            renderer.scale(state, poseStack);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            ModelMoonCow model = renderer.getModel();
            model.setupAnim(state);
            model.head().translateAndRotate(poseStack);
            poseStack.translate(0.0F, 0.0F, -0.25F);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            collector.submitCustomGeometry(
                    poseStack, GLASS, (pose, buffer) -> cube(pose, buffer, lightCoords, overlay));
            poseStack.popPose();
        }

        private static void cube(
                PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay) {
            for (int face = 0; face < FACES.length; face++) {
                float[] f = FACES[face];
                float[] n = NORMALS[face];
                for (int c = 0; c < 20; c += 5) {
                    Vertices.emit(
                            buffer, pose, f[c], f[c + 1], f[c + 2], -1, f[c + 3], f[c + 4], overlay,
                            light, n[0], n[1], n[2]);
                }
            }
        }
    }
}
