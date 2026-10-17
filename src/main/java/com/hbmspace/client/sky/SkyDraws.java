// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.backport.client.rendertype.BlendFunction;
import com.hbm.backport.client.rendertype.ColorTargetState;
import com.hbm.backport.client.rendertype.CompareOp;
import com.hbm.backport.client.rendertype.DepthStencilState;
import com.hbm.backport.client.rendertype.RenderPipeline;
import com.hbm.render.loader.GroupObject;
import com.hbm.render.loader.HFRWavefrontObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

/**
 * Batched sky draws: {@link #draw} records a draw and returns the builder for its vertices,
 * {@link #flush} renders the recorded draws in order.
 *
 * <p>backport: 26.x queues the vertices into a StagedVertexBuffer and replays them through RenderPasses with
 * DynamicTransforms / PlanetInfo / ShaderTime uniform blocks. On 1.21.1 every draw is a mesh of its own in
 * one ByteBufferBuilder, drawn immediately in {@link #flush} with the pipeline's 1.21.1 program
 * ({@link SpaceShaders}): the block members are plain uniforms of the same names, the model view (the
 * model view stack times the pose, taken when the draw is recorded, as 26.x) and the colour go through
 * RenderSystem, blending, depth and culling come from the pipeline. 26.x renders with reversed depth
 * (cleared to 0, GREATER_THAN_OR_EQUAL): the comparisons are mirrored and depth is cleared to 1. The
 * point pipelines (swarm, point stars) are drawn as GL_POINTS by hand, 1.21.1 having no point mode.
 */
public final class SkyDraws implements AutoCloseable {

    private final ByteBufferBuilder bytes = new ByteBufferBuilder(1 << 16);
    private final List<Step> steps = new ArrayList<>();
    private @Nullable BufferBuilder open;
    private @Nullable Draw openDraw;
    private @Nullable VertexBuffer points;

    private sealed interface Step permits Draw, ClearDepth {}

    private static final class Draw implements Step {
        final RenderPipeline pipeline;
        final Matrix4f modelView;
        final float r, g, b, a;
        final @Nullable ResourceLocation texture0, texture1, texture2;
        final @Nullable PlanetInfo planet;
        final float time;
        final boolean points;
        @Nullable MeshData mesh;

        Draw(
                RenderPipeline pipeline,
                Matrix4f modelView,
                float r,
                float g,
                float b,
                float a,
                @Nullable ResourceLocation texture0,
                @Nullable ResourceLocation texture1,
                @Nullable ResourceLocation texture2,
                @Nullable PlanetInfo planet,
                float time,
                boolean points) {
            this.pipeline = pipeline;
            this.modelView = modelView;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.texture0 = texture0;
            this.texture1 = texture1;
            this.texture2 = texture2;
            this.planet = planet;
            this.time = time;
            this.points = points;
        }
    }

    private record ClearDepth() implements Step {}

    public VertexConsumer draw(
            RenderPipeline pipeline,
            PoseStack pose,
            float r,
            float g,
            float b,
            float a,
            @Nullable ResourceLocation texture0,
            @Nullable ResourceLocation texture1,
            @Nullable ResourceLocation texture2,
            @Nullable PlanetInfo planet,
            float time) {
        closeOpen();
        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewStack()).mul(pose.last().pose());
        boolean points = SpaceShaders.drawsPoints(pipeline);
        Draw draw =
                new Draw(
                        pipeline,
                        modelView,
                        Mth.clamp(r, 0, 1),
                        Mth.clamp(g, 0, 1),
                        Mth.clamp(b, 0, 1),
                        Mth.clamp(a, 0, 1),
                        texture0,
                        texture1,
                        texture2,
                        planet == null ? null : planet.copy(),
                        time,
                        points);
        // points: one vertex per point, drawn as GL_POINTS (TRIANGLE_STRIP only for its 1:1 indices)
        VertexFormat.Mode mode = points ? VertexFormat.Mode.TRIANGLE_STRIP : pipeline.getVertexFormatMode();
        open = new BufferBuilder(bytes, mode, pipeline.getVertexFormat());
        openDraw = draw;
        steps.add(draw);
        return open;
    }

    public VertexConsumer draw(
            RenderPipeline pipeline,
            PoseStack pose,
            float r,
            float g,
            float b,
            float a,
            @Nullable ResourceLocation texture0,
            @Nullable ResourceLocation texture1,
            @Nullable PlanetInfo planet,
            float time) {
        return draw(pipeline, pose, r, g, b, a, texture0, texture1, null, planet, time);
    }

    public VertexConsumer draw(
            RenderPipeline pipeline,
            PoseStack pose,
            float r,
            float g,
            float b,
            float a,
            @Nullable ResourceLocation texture) {
        return draw(pipeline, pose, r, g, b, a, texture, null, null, 0);
    }

    public void obj(
            RenderPipeline pipeline,
            PoseStack pose,
            float r,
            float g,
            float b,
            float a,
            ResourceLocation texture,
            HFRWavefrontObject mesh,
            float time) {
        VertexConsumer buffer = draw(pipeline, pose, r, g, b, a, texture, null, null, time);
        for (GroupObject group : mesh.groups) {
            float[] corners = group.quads(true);
            for (int i = 0; i < corners.length; i += GroupObject.STRIDE) {
                buffer.addVertex(corners[i], corners[i + 1], corners[i + 2])
                        .setUv(corners[i + 3], corners[i + 4]);
            }
        }
    }

    public void clearDepth() {
        closeOpen();
        steps.add(new ClearDepth());
    }

    private void closeOpen() {
        if (open != null && openDraw != null) openDraw.mesh = open.build();
        open = null;
        openDraw = null;
    }

    public void flush() {
        closeOpen();
        if (steps.isEmpty()) return;
        GlState saved = GlState.capture();
        try {
            for (Step step : steps) {
                if (step instanceof Draw draw) render(draw);
                else {
                    RenderSystem.depthMask(true);
                    RenderSystem.clearDepth(1.0D);
                    RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                }
            }
        } finally {
            for (Step step : steps) {
                if (step instanceof Draw draw && draw.mesh != null) draw.mesh.close();
            }
            steps.clear();
            bytes.clear();
            saved.restore();
        }
    }

    public void endFrame() {}

    private void render(Draw draw) {
        MeshData mesh = draw.mesh;
        draw.mesh = null;
        if (mesh == null) return;
        ShaderInstance shader = SpaceShaders.program(draw.pipeline);
        if (shader == null) {
            mesh.close();
            return;
        }

        ColorTargetState color = draw.pipeline.getColorTargetState();
        color.blendFunction().ifPresentOrElse(BlendFunction::apply, RenderSystem::disableBlend);
        int mask = color.writeMask();
        RenderSystem.colorMask(
                (mask & ColorTargetState.WRITE_RED) != 0,
                (mask & ColorTargetState.WRITE_GREEN) != 0,
                (mask & ColorTargetState.WRITE_BLUE) != 0,
                (mask & ColorTargetState.WRITE_ALPHA) != 0);
        DepthStencilState depth = draw.pipeline.getDepthStencilState();
        if (depth == null) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
        } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(mirrored(depth.depthTest()).gl());
            RenderSystem.depthMask(depth.writeDepth());
        }
        if (draw.pipeline.isCull()) RenderSystem.enableCull();
        else RenderSystem.disableCull();

        if (draw.texture0 != null) RenderSystem.setShaderTexture(0, draw.texture0);
        if (draw.texture1 != null) RenderSystem.setShaderTexture(1, draw.texture1);
        if (draw.texture2 != null) RenderSystem.setShaderTexture(2, draw.texture2);
        RenderSystem.setShaderColor(draw.r, draw.g, draw.b, draw.a);
        if (draw.planet != null) draw.planet.apply(shader);
        shader.safeGetUniform("iTime").set(draw.time);
        shader.safeGetUniform("BaseVertex").set(0);
        RenderSystem.setShader(() -> shader);

        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.set(draw.modelView);
        RenderSystem.applyModelViewMatrix();
        try {
            if (draw.points) drawPoints(shader, mesh);
            else BufferUploader.drawWithShader(mesh);
        } finally {
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            color.blendFunction().ifPresent(BlendFunction::clear);
        }
    }

    private void drawPoints(ShaderInstance shader, MeshData mesh) {
        int count = mesh.drawState().vertexCount();
        if (points == null) points = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        points.bind();
        points.upload(mesh);
        shader.setDefaultUniforms(
                VertexFormat.Mode.TRIANGLE_STRIP,
                RenderSystem.getModelViewMatrix(),
                RenderSystem.getProjectionMatrix(),
                Minecraft.getInstance().getWindow());
        shader.apply();
        GL11.glDrawArrays(GL11.GL_POINTS, 0, count);
        shader.clear();
        VertexBuffer.unbind();
    }

    /** 26.x depth runs from 1 (near) to 0 (far); 1.21.1 from 0 to 1. */
    private static CompareOp mirrored(CompareOp op) {
        return switch (op) {
            case GREATER_THAN -> CompareOp.LESS_THAN;
            case GREATER_THAN_OR_EQUAL -> CompareOp.LESS_THAN_OR_EQUAL;
            case LESS_THAN -> CompareOp.GREATER_THAN;
            case LESS_THAN_OR_EQUAL -> CompareOp.GREATER_THAN_OR_EQUAL;
            default -> op;
        };
    }

    @Override
    public void close() {
        closeOpen();
        for (Step step : steps) {
            if (step instanceof Draw draw && draw.mesh != null) draw.mesh.close();
        }
        steps.clear();
        bytes.close();
        if (points != null) points.close();
    }

    /** The GL state the draws change, put back after a flush (through RenderSystem, so its cache agrees). */
    private record GlState(
            boolean depthTest,
            int depthFunc,
            boolean depthMask,
            boolean blend,
            int srcRgb,
            int dstRgb,
            int srcAlpha,
            int dstAlpha,
            boolean cull) {

        static GlState capture() {
            return new GlState(
                    GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
                    GL11.glGetInteger(GL11.GL_DEPTH_FUNC),
                    GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),
                    GL11.glIsEnabled(GL11.GL_BLEND),
                    GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_SRC_RGB),
                    GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_DST_RGB),
                    GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_SRC_ALPHA),
                    GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_DST_ALPHA),
                    GL11.glIsEnabled(GL11.GL_CULL_FACE));
        }

        void restore() {
            if (depthTest) RenderSystem.enableDepthTest();
            else RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(depthFunc);
            RenderSystem.depthMask(depthMask);
            RenderSystem.blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
            if (blend) RenderSystem.enableBlend();
            else RenderSystem.disableBlend();
            if (cull) RenderSystem.enableCull();
            else RenderSystem.disableCull();
            RenderSystem.colorMask(true, true, true, true);
        }
    }
}
