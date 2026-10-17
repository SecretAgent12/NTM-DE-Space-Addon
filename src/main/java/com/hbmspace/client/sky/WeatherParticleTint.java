// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.api.client.ParticleSpawns;
import com.hbmspace.dim.WorldProviderCelestial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import com.hbm.backport.ARGB;
import net.minecraft.util.RandomSource;

/**
 * Rain, splashes and drips on planets take the planet's rain colour (white sprites for the rain drops,
 * so the colour shows).
 */
public final class WeatherParticleTint implements ParticleSpawns.Listener {

    public static void register() {
        ParticleSpawns.register(new WeatherParticleTint());
    }

    @Override
    public void created(ParticleOptions options, double x, double y, double z, Particle particle) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !(particle instanceof TextureSheetParticle quad)) return;
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider == null || !provider.hasWeatherCycle() || level.getRainLevel(1.0F) <= 0.0F)
            return;

        ParticleType<?> type = options.getType();
        boolean rain = type == ParticleTypes.RAIN;
        boolean drop =
                type == ParticleTypes.DRIPPING_WATER
                        || type == ParticleTypes.FALLING_WATER
                        || type == ParticleTypes.DRIPPING_DRIPSTONE_WATER
                        || type == ParticleTypes.FALLING_DRIPSTONE_WATER;
        if (!rain
                && type != ParticleTypes.SPLASH
                && !(drop && !waterSourceAbove(level, BlockPos.containing(x, y, z)))) {
            return;
        }

        int color = provider.getWeatherColor();
        quad.setColor(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color));
        if (rain) quad.setSpriteFromAge(new Fixed(generic(level.getRandom().nextInt(3))));
    }

    private static boolean waterSourceAbove(ClientLevel level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isFaceSturdy(level, above, Direction.UP)
                && level.getFluidState(above.above()).is(FluidTags.WATER);
    }

    private static TextureAtlasSprite generic(int index) {
        // backport: 26.x AtlasManager / AtlasIds.PARTICLES; 1.21.1 particle atlas by location
        return ((TextureAtlas)
                        Minecraft.getInstance()
                                .getTextureManager()
                                .getTexture(TextureAtlas.LOCATION_PARTICLES))
                .getSprite(ResourceLocation.withDefaultNamespace("generic_" + index));
    }

    private record Fixed(TextureAtlasSprite sprite) implements SpriteSet {
        @Override
        public TextureAtlasSprite get(int index, int max) {
            return sprite;
        }

        @Override
        public TextureAtlasSprite get(RandomSource random) {
            return sprite;
        }

    }
}
