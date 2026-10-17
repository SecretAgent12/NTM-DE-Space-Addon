// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.mixin.client;

import com.hbm.backport.ARGB;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.CelestialEnvironment;
import com.hbmspace.dim.WorldProviderCelestial;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * backport: 26.x layers a planet's sky light factor, sky colour and cloud colour onto its environment
 * attributes (CelestialEnvironment), which the light map, fog, sky and clouds read. 1.21.1 reads the
 * same values from these ClientLevel methods (as 1.7.10 read them from the WorldProvider), so the
 * planet's values are put in here: the light map darkens with the planet's sun (and Eve's lightning
 * flash), the vanilla fog blends towards the planet's sky and the clouds take its colour.
 */
@Mixin(ClientLevel.class)
public abstract class MixinClientLevelSky {

    // 1.21.1 getSkyDarken(float) is 1.7.10's World.getSunBrightness (the light map's sky factor)
    @ModifyReturnValue(method = "getSkyDarken(F)F", at = @At("RETURN"))
    private float hbmspace$sunBrightness(float vanilla) {
        ClientLevel level = (ClientLevel) (Object) this;
        if (WorldProviderCelestial.of(level) == null && !CelestialBody.inOrbit(level)) return vanilla;
        return CelestialEnvironment.skyLightFactor(level, Vec3.ZERO, vanilla);
    }

    @ModifyReturnValue(method = "getSkyColor", at = @At("RETURN"))
    private Vec3 hbmspace$skyColor(Vec3 vanilla, Vec3 pos, float partialTicks) {
        ClientLevel level = (ClientLevel) (Object) this;
        // 26.x orbit dimension type: sky_color #000000
        if (CelestialBody.inOrbit(level)) return Vec3.ZERO;
        if (WorldProviderCelestial.of(level) == null) return vanilla;
        return rgb(CelestialEnvironment.skyColor(level, pos, argb(vanilla)));
    }

    @ModifyReturnValue(method = "getCloudColor", at = @At("RETURN"))
    private Vec3 hbmspace$cloudColor(Vec3 vanilla) {
        ClientLevel level = (ClientLevel) (Object) this;
        if (WorldProviderCelestial.of(level) == null) return vanilla;
        return rgb(CelestialEnvironment.cloudColor(level, argb(vanilla)));
    }

    private static int argb(Vec3 color) {
        return ARGB.colorFromFloat(
                1F,
                Math.min((float) color.x, 1F),
                Math.min((float) color.y, 1F),
                Math.min((float) color.z, 1F));
    }

    private static Vec3 rgb(int color) {
        return new Vec3(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color));
    }
}
