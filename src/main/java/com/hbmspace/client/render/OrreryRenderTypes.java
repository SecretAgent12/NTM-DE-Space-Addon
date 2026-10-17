// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.client.render.ParticleRenderTypes;
import com.hbm.client.render.WorldRenderPipeline;
import com.hbmspace.HbmSpace;
import com.hbm.backport.client.rendertype.PrimitiveTopology;
import com.hbm.backport.client.rendertype.BlendFunction;
import com.hbm.backport.client.rendertype.ColorTargetState;
import com.hbm.backport.client.rendertype.DepthStencilState;
import com.hbm.backport.client.rendertype.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import com.hbm.backport.client.rendertype.BindGroupLayouts;
import com.hbm.backport.client.rendertype.RenderPipelines;
import com.hbm.backport.client.rendertype.RenderSetup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

public final class OrreryRenderTypes {

    public static final RenderPipeline CUBE_PIPELINE =
            WorldRenderPipeline.of(
                    RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                            .withLocation(id("pipeline/orrery_cube"))
                            .withVertexShader("core/entity")
                            .withFragmentShader(ParticleRenderTypes.ENTITY_FADE)
                            .withShaderDefine("EMISSIVE")
                            .withShaderDefine("NO_OVERLAY")
                            .withShaderDefine("NO_CARDINAL_LIGHTING")
                            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                            .withVertexBinding(0, DefaultVertexFormat.NEW_ENTITY)
                            .withPrimitiveTopology(PrimitiveTopology.QUADS)
                            .withDepthStencilState(DepthStencilState.DEFAULT)
                            .withCull(true));
    public static final RenderPipeline LINES_PIPELINE =
            WorldRenderPipeline.of(
                    RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                            .withLocation(id("pipeline/orrery_lines"))
                            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING)));

    private static final Function<ResourceLocation, RenderType> CUBE =
            Util.memoize(
                    texture ->
                            com.hbm.backport.client.rendertype.RenderTypeFactory.create(
                                    "hbmspace_orrery_cube",
                                    RenderSetup.builder(CUBE_PIPELINE)
                                            .withTexture("Sampler0", texture)
                                            .createRenderSetup()));
    public static final RenderType LINES =
            com.hbm.backport.client.rendertype.RenderTypeFactory.create(
                    "hbmspace_orrery_lines",
                    RenderSetup.builder(LINES_PIPELINE).createRenderSetup());

    private OrreryRenderTypes() {}

    public static RenderType cube(ResourceLocation texture) {
        return CUBE.apply(texture);
    }

    public static void forEach(Consumer<RenderPipeline> registrar) {
        List.of(CUBE_PIPELINE, LINES_PIPELINE).forEach(registrar);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }
}
