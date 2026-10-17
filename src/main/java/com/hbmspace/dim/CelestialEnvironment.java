// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.api.environment.AtmosphereLayers;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.saveddata.TomSaveData;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.orbit.WorldProviderOrbit;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereHandler;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class CelestialEnvironment implements AtmosphereLayers.Provider {

    @Override
    public boolean claims(Level level) {
        return WorldProviderCelestial.of(level) != null || CelestialBody.inOrbit(level);
    }

    // backport: 26.x layers the planets' sky, fog, sunrise, stars, sky light, clouds, moon phase and
    // water evaporation onto the environment attribute system here (addLayers). 1.21.1 has none of
    // that; the same values are computed by the methods below and put into the 1.21.1 places that
    // read them: sky light, sky and cloud colour into ClientLevel (MixinClientLevelSky; the light map,
    // vanilla fog and clouds read those), fog colour into FogRenderer (MixinFogRendererSpace), stars
    // and sunrise into the sky state (SkyProviderCelestial.extract), the day length into
    // Level.getTimeOfDay and the moon phase into Level.getMoonPhase() (MixinLevel), water evaporation
    // into vanilla's ultraWarm() checks (MixinEvaporation*).

    public static int skyColor(Level level, Vec3 pos, int vanilla) {
        Inputs inputs = inputs(level);
        if (inputs == null) return vanilla;
        return inputs.provider.skyColorClient(
                level, inputs.provider.getSkyColor(level, vanilla, inputs.sky(pos)));
    }

    public static int fogColor(Level level, Vec3 pos, int vanilla) {
        Inputs inputs = inputs(level);
        return inputs == null ? vanilla : inputs.provider.getFogColor(level, inputs.sky(pos), pos.y);
    }

    public static int sunriseColor(Level level, Vec3 pos, int vanilla) {
        Inputs inputs = inputs(level);
        return inputs == null ? vanilla : inputs.provider.getSunriseColor(level, vanilla, inputs.sky(pos));
    }

    public static float starBrightness(Level level, float vanilla) {
        if (CelestialBody.inOrbit(level)) {
            return WorldProviderOrbit.getStarBrightness(
                    OrbitalStation.clientStation, new OrbitInputs(level).eclipse(), 0);
        }
        Inputs inputs = inputs(level);
        return inputs == null ? vanilla : inputs.provider.getStarBrightness(vanilla, inputs.dust());
    }

    public static float skyLightFactor(Level level, Vec3 pos, float vanilla) {
        if (CelestialBody.inOrbit(level)) {
            return WorldProviderOrbit.getSunBrightness(new OrbitInputs(level).eclipse());
        }
        Inputs inputs = inputs(level);
        if (inputs == null) return vanilla;
        return inputs.provider.sunBrightnessClient(
                level, inputs.provider.getSunBrightness(vanilla, inputs.sky(pos)));
    }

    public static int cloudColor(Level level, int vanilla) {
        Inputs inputs = inputs(level);
        return inputs == null ? vanilla : inputs.provider.getCloudColor(vanilla);
    }

    /** Moon phase index 0..7 (full moon first), or -1 when the level is not a planet. */
    public static int moonPhase(Level level) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        return provider == null ? -1 : provider.getMoonPhase(level);
    }

    /** Whether water evaporates at {@code pos} (26.x: the water_evaporates attribute). */
    public static boolean waterEvaporates(Level level, BlockPos pos) {
        return new CelestialEnvironment().claims(level) && evaporates(level, pos);
    }

    /**
     * backport: the vanilla ultraWarm() checks (bucket, NeoForge fluid placement, melting ice, wet
     * sponge) ask this. Server side only, like 1.7.10's isHellWorld: the client would not know a
     * pressurized room and would predict the water away.
     */
    public static boolean waterEvaporatesServer(Level level, BlockPos pos) {
        return !level.isClientSide() && waterEvaporates(level, pos);
    }

    private static @org.jspecify.annotations.Nullable Inputs inputs(Level level) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        return provider == null ? null : new Inputs(provider, level);
    }

    private static boolean evaporates(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server && server.getServer().isSameThread()) {
            ChunkAtmosphereHandler air = ChunkAtmosphereManager.proxy;
            if (air.hasLiquidPressure(air.getAtmosphere(server, pos))) return false;
        }
        if (CelestialBody.inOrbit(level)) return true;
        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        return atmosphere == null || atmosphere.getPressure() <= 0.2F;
    }

    private static final class OrbitInputs {

        final Level level;

        private long eclipseTime = Long.MIN_VALUE;
        private double eclipse;

        OrbitInputs(Level level) {
            this.level = level;
        }

        double eclipse() {
            long time = level.getGameTime();
            if (time != eclipseTime) {
                eclipse =
                        WorldProviderOrbit.getSky(level, 0, OrbitalStation.clientStation).eclipse();
                eclipseTime = time;
            }
            return eclipse;
        }
    }

    private static final class Inputs {

        final WorldProviderCelestial provider;
        final Level level;

        private long eclipseTime = Long.MIN_VALUE;
        private double eclipse;

        Inputs(WorldProviderCelestial provider, Level level) {
            this.provider = provider;
            this.level = level;
        }

        WorldProviderCelestial.Sky sky(Vec3 pos) {
            long time = level.getGameTime();
            if (time != eclipseTime) {
                eclipse = provider.getEclipseAmount(level);
                eclipseTime = time;
            }

            // backport: 26.x SUN_ANGLE attribute (degrees) / 360 = the provider's celestial angle
            float solarAngle = provider.getCelestialAngle(level, 0.0F);
            return new WorldProviderCelestial.Sky(solarAngle, eclipse, dust(), fire());
        }

        float dust() {
            if (level instanceof ServerLevel server) {
                TomSaveData data = TomSaveData.published(server);
                return data == null ? 0 : data.dust;
            }
            return ImpactWorldHandler.getDustForClient(level);
        }

        float fire() {
            if (level instanceof ServerLevel server) {
                TomSaveData data = TomSaveData.published(server);
                return data == null ? 0 : data.fire;
            }
            return ImpactWorldHandler.getFireForClient(level);
        }
    }
}
