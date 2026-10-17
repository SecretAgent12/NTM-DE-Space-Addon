// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.api.client.WeatherTints;
import com.hbmspace.dim.WorldProviderCelestial;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jspecify.annotations.Nullable;

/** The planets' rain and snow in their own colours (WorldProviderCelestial weather / snow colour). */
public final class CelestialWeatherTint implements WeatherTints.Provider {

    public static void register() {
        WeatherTints.register(new CelestialWeatherTint());
    }

    @Override
    public WeatherTints.@Nullable Tint tint(ClientLevel level) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider == null) return null;

        int rain = provider.getWeatherColor();
        int snow = provider.getSnowColor();
        // backport: the core tints 1.21.1's weather through its vertex colour and does not draw with
        // the tint's pipelines; 26.x hands vanilla's weather pipelines under a shader pack, which
        // 1.21.1 does not have, so NTM: Space's are handed either way
        return new WeatherTints.Tint(
                SkyPipelines.WEATHER_TINTED, SkyPipelines.WEATHER_TINTED_NO_DEPTH_WRITE, rain, snow);
    }
}
