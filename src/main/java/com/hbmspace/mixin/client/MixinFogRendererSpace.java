// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.mixin.client;

import com.hbm.backport.ARGB;
import com.hbmspace.dim.CelestialEnvironment;
import com.hbmspace.dim.WorldProviderCelestial;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * backport: a planet's fog colour (the colour of its air, darkened by its own day) replaces the biome
 * fog colour, before vanilla blends it towards the sky with the render distance and the sunrise, as
 * 1.7.10's WorldProvider.getFogColor did in updateFogColor. 26.x layers it onto the FOG_COLOR
 * environment attribute (CelestialEnvironment.fogColor).
 */
@Mixin(FogRenderer.class)
public abstract class MixinFogRendererSpace {

    @ModifyExpressionValue(
            method = "setupColor",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 hbmspace$planetFog(
            Vec3 biomeFog,
            @Local(argsOnly = true) Camera camera,
            @Local(argsOnly = true) ClientLevel level) {
        // 26.x orbit dimension type: fog_color #000000
        if (com.hbmspace.dim.CelestialBody.inOrbit(level)) return Vec3.ZERO;
        if (WorldProviderCelestial.of(level) == null) return biomeFog;
        int fog =
                CelestialEnvironment.fogColor(
                        level,
                        camera.getPosition(),
                        ARGB.colorFromFloat(
                                1F, (float) biomeFog.x, (float) biomeFog.y, (float) biomeFog.z));
        return new Vec3(ARGB.redFloat(fog), ARGB.greenFloat(fog), ARGB.blueFloat(fog));
    }
}
