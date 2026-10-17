// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.client.sky;

import com.hbm.backport.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * backport: the parts of 26.x's {@code SkyRenderer} the Space skies call. 1.21.1 draws the same inline in
 * {@code LevelRenderer.renderSky} (its sky, dark-sky buffers and the sunrise fan are private there), so
 * they are drawn here the way 1.21.1 builds them: the sky disc at y 16 and the dark disc at y -16 (moved
 * up by 12 like vanilla), both radius 512, and the sunrise fan of 16 segments at radius 120. The model
 * view of the RenderSystem stack (the view rotation, set by SpaceSkies) applies.
 */
public final class VanillaSky {

    private final LevelRenderer level;

    VanillaSky(LevelRenderer level) {
        this.level = level;
    }

    public LevelRenderer levelRenderer() {
        return level;
    }

    /** The fogged sky disc in one colour (as vanilla's, used under a shader pack). */
    public void renderSkyDisc(int color) {
        RenderSystem.setShaderColor(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color), 1F);
        disc(16F, new Matrix4f());
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    /** The black disc below the horizon. */
    public void renderDarkDisc() {
        RenderSystem.setShaderColor(0F, 0F, 0F, 1F);
        disc(-16F, new Matrix4f().translation(0F, 12F, 0F));
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    private static void disc(float y, Matrix4f model) {
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        Matrix4f m = new Matrix4f(RenderSystem.getModelViewStack()).mul(model);
        BufferBuilder buffer =
                Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        float sign = Math.signum(y) * 512F;
        buffer.addVertex(m, 0F, y, 0F);
        for (int i = -180; i <= 180; i += 45) {
            buffer.addVertex(
                    m, sign * Mth.cos(i * Mth.DEG_TO_RAD), y, 512F * Mth.sin(i * Mth.DEG_TO_RAD));
        }
        draw(buffer.build());
    }

    /** The sunrise / sunset fan, as 1.21.1 renderSky draws it (colour with its alpha). */
    public void renderSunriseAndSunset(PoseStack pose, float sunAngle, int color) {
        float r = ARGB.redFloat(color), g = ARGB.greenFloat(color), b = ARGB.blueFloat(color);
        float a = ARGB.alphaFloat(color);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(sunAngle) < 0.0F ? 180.0F : 0.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
        Matrix4f m = new Matrix4f(RenderSystem.getModelViewStack()).mul(pose.last().pose());
        BufferBuilder buffer =
                Tesselator.getInstance()
                        .begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(m, 0.0F, 100.0F, 0.0F).setColor(r, g, b, a);
        for (int i = 0; i <= 16; i++) {
            float angle = i * (float) (Math.PI * 2) / 16.0F;
            float sin = Mth.sin(angle);
            float cos = Mth.cos(angle);
            buffer.addVertex(m, sin * 120.0F, cos * 120.0F, -cos * 40.0F * a).setColor(r, g, b, 0.0F);
        }
        draw(buffer.build());
        pose.popPose();
        RenderSystem.disableBlend();
    }

    private static void draw(MeshData mesh) {
        if (mesh == null) return;
        // the vertices are already in view space: draw with an identity model view
        org.joml.Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.identity();
        RenderSystem.applyModelViewMatrix();
        BufferUploader.drawWithShader(mesh);
        stack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
}
