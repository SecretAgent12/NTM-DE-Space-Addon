// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.entity.item.EntityTntNtm;
import com.hbmspace.entity.SpinningCharge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * The core's charges (vanilla TNT renderer), spinning in low gravity.
 *
 * <p>backport: 26.x renders through a TntRenderState; this is 1.21.1's TntRenderer.render with the
 * spin rotations of the 26.x renderer before the fuse swell.
 */
public class RenderTntSpin extends EntityRenderer<EntityTntNtm> {

    private final BlockRenderDispatcher blockRenderer;

    public RenderTntSpin(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(
            EntityTntNtm entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        SpinningCharge spin = (SpinningCharge) entity;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin.hbmspace$spinYaw(partialTicks) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin.hbmspace$spinPitch(partialTicks)));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        int fuse = entity.getFuse();
        if (fuse - partialTicks + 1.0F < 10.0F) {
            float swell = 1.0F - (fuse - partialTicks + 1.0F) / 10.0F;
            swell = Mth.clamp(swell, 0.0F, 1.0F);
            swell *= swell;
            swell *= swell;
            float scale = 1.0F + swell * 0.3F;
            poseStack.scale(scale, scale, scale);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        TntMinecartRenderer.renderWhiteSolidBlock(
                blockRenderer, entity.getBlockState(), poseStack, buffer, packedLight, fuse / 5 % 2 == 0);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTntNtm entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
