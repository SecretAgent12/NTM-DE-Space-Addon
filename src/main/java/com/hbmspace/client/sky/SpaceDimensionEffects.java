// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.client.sky;

import com.hbmspace.HbmSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * backport: the special effects of NTM: Space's dimensions ({@code "effects": "hbmspace:celestial"} in
 * their dimension types). 26.x has no special effects: a dimension's sky, fog and clouds are
 * environment attributes, and Space's dimensions have no cloud height, so vanilla's clouds stay away
 * (in orbit, and where the air is too thin for clouds the cloud colour is fully transparent). 1.21.1
 * takes the clouds from the special effects, so these are the overworld's, with clouds only where the
 * body's air holds them (WorldProviderCelestial.getCloudLayerCount, as 1.7.10): none in orbit or on
 * airless bodies. NaN keeps vanilla's, Sodium's and the extra layers' (CelestialClouds) clouds away.
 */
public final class SpaceDimensionEffects extends DimensionSpecialEffects {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, "celestial");

    private static final DimensionSpecialEffects OVERWORLD = new OverworldEffects();

    public SpaceDimensionEffects() {
        super(OVERWORLD.getCloudHeight(), true, SkyType.NORMAL, false, false);
    }

    @Override
    public float getCloudHeight() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return super.getCloudHeight();
        if (CelestialBody.inOrbit(level)) return Float.NaN;
        if (WorldProviderCelestial.of(level) != null
                && WorldProviderCelestial.getCloudLayerCount(
                                CelestialBody.getBody(level).getTrait(CBT_Atmosphere.class))
                        <= 0) return Float.NaN;
        return super.getCloudHeight();
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return OVERWORLD.getBrightnessDependentFogColor(fogColor, brightness);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return OVERWORLD.isFoggyAt(x, y);
    }
}
