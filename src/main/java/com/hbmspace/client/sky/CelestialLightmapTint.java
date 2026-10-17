// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbmspace.dim.SpaceDimensions;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;

/**
 * Ike's red and Laythe's green (Jool-lit) night light: the light map rows brighten by how much the
 * sky light outshines the sun (1.7.10 WorldProviderIke / WorldProviderLaythe.updateLightmap).
 *
 * <p>backport: 26.x renders the light map on the GPU and adds this in a second pass
 * (lightmap_tint.fsh); 1.21.1 fills the light map on the CPU, so the bonus is added to its pixels
 * before the upload (MixinLightTexture).
 */
public final class CelestialLightmapTint {

    private CelestialLightmapTint() {}

    public static float bonus(float skyBrightness, float sunBrightness, boolean ike) {
        float sun = ike ? sunBrightness - .1F : sunBrightness;
        return Math.max(skyBrightness - sun, 0F) * (ike ? 20F : 60F);
    }

    /** Adds the bonus to the 16x16 light map (x = block light, y = sky light; ABGR pixels). */
    public static void apply(ClientLevel level, NativeImage pixels) {
        boolean ike = level.dimension() == SpaceDimensions.IKE;
        if (!ike && level.dimension() != SpaceDimensions.LAYTHE) return;
        float sun = level.getSkyDarken(1.0F);
        int shift = ike ? 0 : 8; // red / green byte of NativeImage's ABGR
        for (int sky = 0; sky < 16; sky++) {
            float boost = bonus(LightTexture.getBrightness(level.dimensionType(), sky), sun, ike);
            if (boost <= 0F) continue;
            for (int block = 0; block < 16; block++) {
                int pixel = pixels.getPixelRGBA(block, sky);
                int channel = Math.min((int) ((pixel >>> shift & 0xFF) + boost), 255);
                pixels.setPixelRGBA(block, sky, pixel & ~(0xFF << shift) | channel << shift);
            }
        }
    }
}
