// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.dim.CelestialEnvironment;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.WorldProviderEarth;
import com.hbmspace.dim.duna.DunaDustStorm;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public abstract class MixinLevel {
    // backport: 26.x Level.isBrightOutside() is 1.21.1 isDay()
    @ModifyReturnValue(method = "isDay", at = @At("RETURN"))
    private boolean hbmspace$stormNight(boolean bright) {
        Level level = (Level) (Object) this;
        if (!bright || level.dimension() != SpaceDimensions.DUNA) return bright;
        float intensity =
                level instanceof ServerLevel server
                        ? DunaDustStorm.get(server).intensity
                        : DunaDustStorm.clientIntensity;
        return !(intensity >= 0.2F);
    }

    // backport: 26.x takes the moon phase from the planets' moon_phase environment attribute; 1.21.1
    // only from the dimension type (LevelTimeAccess defaults). These override the defaults on Level:
    // a planet's phase follows its moon (WorldProviderCelestial.getMoonPhase, as in 1.7.10), which
    // slimes, local difficulty and cats read. Elsewhere the vanilla defaults' result.
    public int getMoonPhase() {
        Level level = (Level) (Object) this;
        int phase = CelestialEnvironment.moonPhase(level);
        return phase >= 0 ? phase : level.dimensionType().moonPhase(level.getDayTime());
    }

    public float getMoonBrightness() {
        return DimensionType.MOON_BRIGHTNESS_PER_PHASE[getMoonPhase()];
    }

    // backport: 26.x gives every planet its own clock (the SUN_ANGLE attribute); 1.21.1 only has the
    // dimension type's timeOfDay (LevelTimeAccess default). This overrides that default on Level, so
    // a planet's day length (WorldProviderCelestial.getCelestialAngle, 1.7.10's calculateCelestialAngle)
    // drives the sun angle, sky light (skyDarken: isDay, spawning) and the client sky and light map,
    // as 1.7.10's WorldProvider did. The Earth provider reads this method itself, so it keeps vanilla.
    public float getTimeOfDay(float partialTicks) {
        Level level = (Level) (Object) this;
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider == null || provider instanceof WorldProviderEarth)
            return level.dimensionType().timeOfDay(level.getDayTime());
        return provider.getCelestialAngle(level, partialTicks);
    }
}
