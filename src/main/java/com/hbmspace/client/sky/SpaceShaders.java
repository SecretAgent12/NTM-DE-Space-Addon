// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.client.sky;

import com.hbm.backport.client.rendertype.RenderPipeline;
import com.hbm.backport.client.rendertype.ShaderDefines;
import com.hbmspace.HbmSpace;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * backport: the 1.21.1 programs of the Space sky and planet pipelines. 26.x builds one program per
 * RenderPipeline from its shaders and defines; 1.21.1 loads named programs, which
 * contrib_space_shaders writes from the 26.x sources (one per pipeline variant, see there).
 * {@link #program} picks the program for a pipeline; pipelines on vanilla shaders get the 1.21.1
 * vanilla program of their vertex format.
 */
public final class SpaceShaders {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** program name -> vertex format (the attributes its vertex shader reads). */
    private static final Map<String, VertexFormat> PROGRAMS = new LinkedHashMap<>();

    static {
        PROGRAMS.put("sky", DefaultVertexFormat.POSITION);
        PROGRAMS.put("sky_colored", DefaultVertexFormat.POSITION_COLOR);
        PROGRAMS.put("sky_textured", DefaultVertexFormat.POSITION_TEX);
        PROGRAMS.put("sky_textured_points", DefaultVertexFormat.POSITION_TEX);
        PROGRAMS.put("sky_disc", DefaultVertexFormat.POSITION);
        PROGRAMS.put("swarm", DefaultVertexFormat.POSITION);
        PROGRAMS.put("blackhole", DefaultVertexFormat.POSITION_TEX);
        PROGRAMS.put("flesh", DefaultVertexFormat.POSITION_TEX);
        for (String planet :
                new String[] {
                    "atmosphere", "crescent", "atmosphere_emissive", "nightlights", "lightning", "nuke",
                    "atmosphere_emissive_masked", "nightlights_masked"
                }) {
            PROGRAMS.put(planet, DefaultVertexFormat.POSITION_TEX);
        }
        PROGRAMS.put("weather_tinted", DefaultVertexFormat.PARTICLE);
    }

    private static final Map<String, ShaderInstance> LOADED = new HashMap<>();

    private SpaceShaders() {}

    public static void onRegisterShaders(RegisterShadersEvent event) {
        for (Map.Entry<String, VertexFormat> program : PROGRAMS.entrySet()) {
            String name = program.getKey();
            try {
                event.registerShader(
                        new ShaderInstance(
                                event.getResourceProvider(),
                                ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, name),
                                program.getValue()),
                        shader -> LOADED.put(name, shader));
            } catch (IOException e) {
                LOGGER.error("Failed to load hbmspace shader {}", name, e);
            }
        }
    }

    /** The program name for a pipeline on Space shaders, or null for vanilla ones. */
    static @Nullable String programName(RenderPipeline pipeline) {
        String fragment = pipeline.getFragmentShader().toString();
        ShaderDefines defines = pipeline.getShaderDefines();
        if (!fragment.startsWith(HbmSpace.MOD_ID + ":core/")) return null;
        String name = fragment.substring((HbmSpace.MOD_ID + ":core/").length());
        switch (name) {
            case "sky":
                if (defines.has("TEXTURED"))
                    return defines.has("POINTS") ? "sky_textured_points" : "sky_textured";
                return defines.has("COLORED") ? "sky_colored" : "sky";
            case "atmosphere_emissive":
            case "nightlights":
                return defines.has("BODY_MASK") ? name + "_masked" : name;
            default:
                return PROGRAMS.containsKey(name) ? name : null;
        }
    }

    /** The planets' rain and snow program (CelestialWeatherTint), null until loaded. */
    static @Nullable ShaderInstance weatherTinted() {
        return LOADED.get("weather_tinted");
    }

    /** Whether the pipeline draws points (26.x PrimitiveTopology.POINTS, which 1.21.1 lacks). */
    static boolean drawsPoints(RenderPipeline pipeline) {
        String name = programName(pipeline);
        return "swarm".equals(name) || "sky_textured_points".equals(name);
    }

    static @Nullable ShaderInstance program(RenderPipeline pipeline) {
        String name = programName(pipeline);
        if (name != null) return LOADED.get(name);
        VertexFormat format = pipeline.getVertexFormat();
        if (format == DefaultVertexFormat.POSITION_TEX) return GameRenderer.getPositionTexShader();
        if (format == DefaultVertexFormat.POSITION_COLOR) return GameRenderer.getPositionColorShader();
        if (format == DefaultVertexFormat.POSITION_TEX_COLOR) return GameRenderer.getPositionTexColorShader();
        if (format == DefaultVertexFormat.POSITION) return GameRenderer.getPositionShader();
        return null;
    }
}
