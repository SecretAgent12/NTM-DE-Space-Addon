// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.client.render.WorldRenderPipeline;
import com.hbmspace.HbmSpace;
import com.hbm.backport.client.rendertype.PrimitiveTopology;
import com.hbm.backport.client.rendertype.BindGroupLayout;
import com.hbm.backport.client.rendertype.BlendFunction;
import com.hbm.backport.client.rendertype.ColorTargetState;
import com.hbm.backport.client.rendertype.DepthStencilState;
import com.hbm.backport.client.rendertype.RenderPipeline;
import com.hbm.backport.client.rendertype.BlendFactor;
import com.hbm.backport.client.rendertype.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.function.Consumer;
import com.hbm.backport.client.rendertype.BindGroupLayouts;
import com.hbm.backport.client.rendertype.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public final class SkyPipelines {

    public static final BindGroupLayout PLANET_INFO =
            BindGroupLayout.uniforms("planet_info", "PlanetInfo");
    public static final BindGroupLayout SHADER_TIME =
            BindGroupLayout.uniforms("shader_time", "ShaderTime");

    private static final BlendFunction TRANSLUCENT =
            new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
    private static final BlendFunction TRANSLUCENT_ALPHA_REPLACE =
            new BlendFunction(
                    BlendFactor.SRC_ALPHA,
                    BlendFactor.ONE_MINUS_SRC_ALPHA,
                    BlendFactor.ONE,
                    BlendFactor.ZERO);
    private static final BlendFunction ADDITIVE = BlendFunction.OVERLAY;

    // backport: the shim has no point topology; SkyDraws draws these pipelines as GL_POINTS itself
    // (SpaceShaders.drawsPoints)
    private static final PrimitiveTopology POINTS = PrimitiveTopology.QUADS;
    // backport: 26.x RenderPipelines.WEATHER_SNIPPET; 1.21.1 draws weather with the particle shader
    private static final RenderPipeline.Snippet WEATHER_SNIPPET =
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withVertexShader(ResourceLocation.withDefaultNamespace("core/particle"))
                    .withFragmentShader(ResourceLocation.withDefaultNamespace("core/particle"))
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
                    .withColorTargetState(new ColorTargetState(TRANSLUCENT))
                    .withVertexBinding(0, DefaultVertexFormat.PARTICLE)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .buildSnippet();

    public static final RenderPipeline SKY_DISC =
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(id("pipeline/sky_disc"))
                    .withVertexShader(ResourceLocation.withDefaultNamespace("core/sky"))
                    .withFragmentShader(id("core/sky_disc"))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
                    .build();
    public static final RenderPipeline TEXTURED_ADDITIVE = RenderPipelines.CELESTIAL;
    public static final RenderPipeline TEXTURED_ADDITIVE_NO_CULL =
            WorldRenderPipeline.nativeSky(textured("textured_additive_no_cull", ADDITIVE, false));
    public static final RenderPipeline TEXTURED_ADDITIVE_ALPHA = RenderPipelines.CELESTIAL;
    public static final RenderPipeline TEXTURED_TRANSLUCENT =
            WorldRenderPipeline.nativeSky(textured("textured_translucent", TRANSLUCENT, true));
    public static final RenderPipeline TEXTURED_OPAQUE =
            WorldRenderPipeline.nativeSky(textured("textured_opaque", null, true));
    // backport: 26.x RenderPipelines.STARS (not in the core's shim): the flat sky shader, additive
    public static final RenderPipeline FLAT_ADDITIVE =
            WorldRenderPipeline.nativeSky(
                    sky("flat_additive", DefaultVertexFormat.POSITION, PrimitiveTopology.QUADS, ADDITIVE, true)
                            .withDepthStencilState(null)
                            .build());
    public static final RenderPipeline FLAT_ADDITIVE_DEPTH_WRITE =
            WorldRenderPipeline.nativeSky(
                    sky(
                                    "flat_additive_depth_write",
                                    DefaultVertexFormat.POSITION,
                                    PrimitiveTopology.QUADS,
                                    ADDITIVE,
                                    true)
                            .withDepthStencilState(
                                    new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
                            .build());
    public static final RenderPipeline COLORED_TRANSLUCENT_NO_CULL =
            WorldRenderPipeline.nativeSky(
                    sky(
                                    "colored_translucent_no_cull",
                                    DefaultVertexFormat.POSITION_COLOR,
                                    PrimitiveTopology.QUADS,
                                    TRANSLUCENT_ALPHA_REPLACE,
                                    false)
                            .withShaderDefine("COLORED")
                            .build());
    public static final RenderPipeline SUNSET_ADDITIVE =
            WorldRenderPipeline.nativeSky(
                    sky(
                                    "sunset_additive",
                                    DefaultVertexFormat.POSITION_COLOR,
                                    PrimitiveTopology.TRIANGLE_FAN,
                                    ADDITIVE,
                                    true)
                            .withShaderDefine("COLORED")
                            .build());
    public static final RenderPipeline BEAM_ADDITIVE =
            WorldRenderPipeline.nativeSky(
                    sky(
                                    "beam_additive",
                                    DefaultVertexFormat.POSITION_COLOR,
                                    PrimitiveTopology.QUADS,
                                    ADDITIVE,
                                    false)
                            .withShaderDefine("COLORED")
                            .build());
    public static final RenderPipeline RAILGUN_BODY =
            WorldRenderPipeline.nativeSky(
                    texturedBuilder("railgun_body", null, true)
                            .withDepthStencilState(
                                    new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
                            .build());
    public static final RenderPipeline RAILGUN_BEAM =
            WorldRenderPipeline.nativeSky(
                    sky(
                                    "railgun_beam",
                                    DefaultVertexFormat.POSITION_COLOR,
                                    PrimitiveTopology.QUADS,
                                    BlendFunction.LIGHTNING,
                                    false)
                            .withShaderDefine("COLORED")
                            .build());
    public static final RenderPipeline SATELLITE_ORBIT_LINE =
            WorldRenderPipeline.nativeSky(
                    sky(
                                    "satellite_orbit_line",
                                    DefaultVertexFormat.POSITION_COLOR,
                                    PrimitiveTopology.DEBUG_LINE_STRIP,
                                    ADDITIVE,
                                    false)
                            .withShaderDefine("COLORED")
                            .build());
    public static final RenderPipeline POINTS_TEXTURED_ADDITIVE =
            WorldRenderPipeline.nativeSky(
                    sky(
                                    "points_textured_additive",
                                    DefaultVertexFormat.POSITION_TEX,
                                    POINTS,
                                    ADDITIVE,
                                    true)
                            .withShaderDefine("TEXTURED")
                            .withShaderDefine("POINTS")
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                            .build());

    public static final RenderPipeline SWARM =
            WorldRenderPipeline.nativeSky(
                    RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                            .withBindGroupLayout(SHADER_TIME)
                            .withLocation(id("pipeline/sky_swarm"))
                            .withVertexShader(id("core/swarm"))
                            .withFragmentShader(id("core/swarm"))
                            .withColorTargetState(new ColorTargetState(TRANSLUCENT))
                            .withDepthStencilState(
                                    new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                            .withVertexBinding(0, DefaultVertexFormat.POSITION)
                            .withPrimitiveTopology(POINTS)
                            .build());
    public static final RenderPipeline BLACK_HOLE =
            WorldRenderPipeline.nativeSky(
                    RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                            .withBindGroupLayout(SHADER_TIME)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                            .withLocation(id("pipeline/sky_black_hole"))
                            .withVertexShader(id("core/planet"))
                            .withFragmentShader(id("core/blackhole"))
                            .withColorTargetState(new ColorTargetState(TRANSLUCENT))
                            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                            .withPrimitiveTopology(PrimitiveTopology.QUADS)
                            .build());

    public static final RenderPipeline FLESH =
            WorldRenderPipeline.nativeSky(
                    RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                            .withBindGroupLayout(SHADER_TIME)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                            .withLocation(id("pipeline/sky_flesh"))
                            .withVertexShader(id("core/flesh"))
                            .withFragmentShader(id("core/flesh"))
                            .withColorTargetState(new ColorTargetState(TRANSLUCENT))
                            .withCull(false)
                            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                            .withPrimitiveTopology(PrimitiveTopology.QUADS)
                            .build());

    public static final RenderPipeline ATMOSPHERE =
            planet("atmosphere", TRANSLUCENT_ALPHA_REPLACE, BindGroupLayouts.SAMPLER0);
    public static final RenderPipeline CRESCENT =
            planet("crescent", TRANSLUCENT_ALPHA_REPLACE, BindGroupLayouts.SAMPLER0);
    public static final RenderPipeline ATMOSPHERE_EMISSIVE =
            planet("atmosphere_emissive", ADDITIVE, BindGroupLayouts.SAMPLER0_SAMPLER1);
    public static final RenderPipeline NIGHT_LIGHTS =
            planet("nightlights", ADDITIVE, BindGroupLayouts.SAMPLER0_SAMPLER1);
    public static final RenderPipeline LIGHTNING =
            planet("lightning", ADDITIVE, BindGroupLayouts.SAMPLER0_SAMPLER1);
    public static final RenderPipeline NUKE = planet("nuke", ADDITIVE, null);

    public static final RenderPipeline WEATHER_TINTED =
            RenderPipeline.builder(WEATHER_SNIPPET)
                    .withLocation(id("pipeline/weather_tinted_depth_write"))
                    .withFragmentShader(id("core/weather_tinted"))
                    .build();
    public static final RenderPipeline WEATHER_TINTED_NO_DEPTH_WRITE =
            RenderPipeline.builder(WEATHER_SNIPPET)
                    .withLocation(id("pipeline/weather_tinted_no_depth_write"))
                    .withFragmentShader(id("core/weather_tinted"))
                    .withDepthStencilState(
                            new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .build();

    private static final List<RenderPipeline> ALL =
            List.of(
                    SKY_DISC,
                    FLAT_ADDITIVE,
                    TEXTURED_ADDITIVE_NO_CULL,
                    TEXTURED_TRANSLUCENT,
                    TEXTURED_OPAQUE,
                    FLAT_ADDITIVE_DEPTH_WRITE,
                    COLORED_TRANSLUCENT_NO_CULL,
                    SUNSET_ADDITIVE,
                    BEAM_ADDITIVE,
                    RAILGUN_BODY,
                    RAILGUN_BEAM,
                    SATELLITE_ORBIT_LINE,
                    POINTS_TEXTURED_ADDITIVE,
                    SWARM,
                    BLACK_HOLE,
                    FLESH,
                    ATMOSPHERE,
                    CRESCENT,
                    ATMOSPHERE_EMISSIVE,
                    NIGHT_LIGHTS,
                    LIGHTNING,
                    NUKE,
                    WEATHER_TINTED,
                    WEATHER_TINTED_NO_DEPTH_WRITE);

    private SkyPipelines() {}

    public static void forEach(Consumer<RenderPipeline> registrar) {
        ALL.forEach(registrar);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }

    private static RenderPipeline.Builder sky(
            String name,
            VertexFormat format,
            PrimitiveTopology topology,
            BlendFunction blend,
            boolean cull) {
        return RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withLocation(id("pipeline/sky_" + name))
                .withVertexShader(id("core/sky"))
                .withFragmentShader(id("core/sky"))
                .withColorTargetState(new ColorTargetState(blend))
                .withCull(cull)
                .withVertexBinding(0, format)
                .withPrimitiveTopology(topology);
    }

    private static RenderPipeline textured(
            String name, @Nullable BlendFunction blend, boolean cull) {
        return texturedBuilder(name, blend, cull).build();
    }

    private static RenderPipeline.Builder texturedBuilder(
            String name, @Nullable BlendFunction blend, boolean cull) {
        RenderPipeline.Builder builder =
                RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                        .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                        .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                        .withLocation(id("pipeline/sky_" + name))
                        .withVertexShader(id("core/sky"))
                        .withFragmentShader(id("core/sky"))
                        .withShaderDefine("TEXTURED")
                        .withCull(cull)
                        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                        .withPrimitiveTopology(PrimitiveTopology.QUADS);
        if (blend != null) builder.withColorTargetState(new ColorTargetState(blend));
        return builder;
    }

    private static RenderPipeline planet(
            String shader, BlendFunction blend, @Nullable BindGroupLayout samplers) {
        RenderPipeline.Builder builder =
                RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                        .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                        .withBindGroupLayout(PLANET_INFO)
                        .withLocation(id("pipeline/sky_" + shader))
                        .withVertexShader(id("core/planet"))
                        .withFragmentShader(id("core/" + shader))
                        .withColorTargetState(new ColorTargetState(blend))
                        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                        .withPrimitiveTopology(PrimitiveTopology.QUADS);
        if (samplers != null) builder.withBindGroupLayout(samplers);
        return WorldRenderPipeline.nativeSky(builder.build());
    }
}
