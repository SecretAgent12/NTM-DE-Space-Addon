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
import java.util.function.Consumer;
import java.util.function.Function;
import com.hbm.backport.client.rendertype.BindGroupLayouts;
import com.hbm.backport.client.rendertype.RenderPipelines;
import com.hbm.backport.client.rendertype.RenderSetup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

public final class IndustryRenderTypes {

    public static final RenderPipeline LAMP_PIPELINE =
            WorldRenderPipeline.of(
                    RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
                            .withLocation(
                                    ResourceLocation.fromNamespaceAndPath(
                                            HbmSpace.MOD_ID, "pipeline/hydroponic_lamp"))
                            .withVertexShader("core/entity")
                            .withFragmentShader(ParticleRenderTypes.ENTITY_FADE)
                            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                            .withShaderDefine("EMISSIVE")
                            .withShaderDefine("NO_OVERLAY")
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                            .withVertexBinding(0, DefaultVertexFormat.NEW_ENTITY)
                            .withPrimitiveTopology(PrimitiveTopology.QUADS)
                            .withDepthStencilState(
                                    new DepthStencilState(
                                            DepthStencilState.DEFAULT.depthTest(), false))
                            .withCull(false));

    private static final Function<ResourceLocation, RenderType> LAMP =
            Util.memoize(
                    texture ->
                            com.hbm.backport.client.rendertype.RenderTypeFactory.create(
                                    "hbmspace_hydroponic_lamp",
                                    RenderSetup.builder(LAMP_PIPELINE)
                                            .withTexture("Sampler0", texture)
                                            .createRenderSetup()));

    private IndustryRenderTypes() {}

    public static RenderType lamp(ResourceLocation texture) {
        return LAMP.apply(texture);
    }

    public static void forEach(Consumer<RenderPipeline> registrar) {
        registrar.accept(LAMP_PIPELINE);
    }
}
