// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import java.nio.ByteBuffer;

// backport: 26.x DynamicUniformStorage.DynamicUniform / Std140Builder are not in 1.21.1. The 1.21.1
// programs take the block's members as plain uniforms of the same names (apply); write() keeps the
// std140 layout (4 vec4, 17 floats, 5 ints, padded to 16 bytes).
public final class PlanetInfo {

    public static final int MAX_SHOCKS = 4;
    public static final int SIZE = 160;

    public final float[] nukeShockTime = {-1, -1, -1, -1};
    public final float[] nukeShockCenterX = {0.5F, 0.5F, 0.5F, 0.5F};
    public final float[] nukeShockCenterY = {0.5F, 0.5F, 0.5F, 0.5F};
    public final float[] nukeShockStrength = new float[MAX_SHOCKS];
    public float atmosphereColorR;
    public float atmosphereColorG;
    public float atmosphereColorB;
    public float cloudColorR;
    public float cloudColorG;
    public float cloudColorB;
    public float cloudTintStrength;
    public float cloudStormDarkness;
    public float atmosphereAlpha;
    public float atmosphereTime;
    public float patternOffset;
    public float impactTime;
    public float phase;
    public float offset;
    public float atmosphereDensity;
    public float cloudLightningStrength;
    public float eveFlashStrength;
    public int nukeShockCount;
    public int atmosphereStyle;
    public int lightningMode;
    public int blackouts;
    public int useBodyAlphaMask;

    public void write(ByteBuffer buffer) {
        float[][] vectors = {nukeShockTime, nukeShockCenterX, nukeShockCenterY, nukeShockStrength};
        for (float[] v : vectors) {
            for (int i = 0; i < 4; i++) buffer.putFloat(v[i]);
        }
        float[] scalars = {
            atmosphereColorR, atmosphereColorG, atmosphereColorB, cloudColorR, cloudColorG,
            cloudColorB, cloudTintStrength, cloudStormDarkness, atmosphereAlpha, atmosphereTime,
            patternOffset, impactTime, phase, offset, atmosphereDensity, cloudLightningStrength,
            eveFlashStrength
        };
        for (float f : scalars) buffer.putFloat(f);
        buffer.putInt(nukeShockCount)
                .putInt(atmosphereStyle)
                .putInt(lightningMode)
                .putInt(blackouts)
                .putInt(useBodyAlphaMask);
        // std140: the block size is rounded up to a vec4
        for (int i = 4 * 4 * 4 + 22 * 4; i < SIZE; i += 4) buffer.putInt(0);
    }

    /** backport: the block's members as the 1.21.1 program's uniforms (see contrib_space_shaders). */
    public void apply(net.minecraft.client.renderer.ShaderInstance shader) {
        shader.safeGetUniform("PlanetNukeShockTime")
                .set(nukeShockTime[0], nukeShockTime[1], nukeShockTime[2], nukeShockTime[3]);
        shader.safeGetUniform("PlanetNukeShockCenterX")
                .set(nukeShockCenterX[0], nukeShockCenterX[1], nukeShockCenterX[2], nukeShockCenterX[3]);
        shader.safeGetUniform("PlanetNukeShockCenterY")
                .set(nukeShockCenterY[0], nukeShockCenterY[1], nukeShockCenterY[2], nukeShockCenterY[3]);
        shader.safeGetUniform("PlanetNukeShockStrength")
                .set(nukeShockStrength[0], nukeShockStrength[1], nukeShockStrength[2], nukeShockStrength[3]);
        shader.safeGetUniform("atmosphereColorR").set(atmosphereColorR);
        shader.safeGetUniform("atmosphereColorG").set(atmosphereColorG);
        shader.safeGetUniform("atmosphereColorB").set(atmosphereColorB);
        shader.safeGetUniform("cloudColorR").set(cloudColorR);
        shader.safeGetUniform("cloudColorG").set(cloudColorG);
        shader.safeGetUniform("cloudColorB").set(cloudColorB);
        shader.safeGetUniform("cloudTintStrength").set(cloudTintStrength);
        shader.safeGetUniform("cloudStormDarkness").set(cloudStormDarkness);
        shader.safeGetUniform("atmosphereAlpha").set(atmosphereAlpha);
        shader.safeGetUniform("atmosphereTime").set(atmosphereTime);
        shader.safeGetUniform("patternOffset").set(patternOffset);
        shader.safeGetUniform("impactTime").set(impactTime);
        shader.safeGetUniform("phase").set(phase);
        shader.safeGetUniform("offset").set(offset);
        shader.safeGetUniform("atmosphereDensity").set(atmosphereDensity);
        shader.safeGetUniform("cloudLightningStrength").set(cloudLightningStrength);
        shader.safeGetUniform("eveFlashStrength").set(eveFlashStrength);
        shader.safeGetUniform("nukeShockCount").set(nukeShockCount);
        shader.safeGetUniform("atmosphereStyle").set(atmosphereStyle);
        shader.safeGetUniform("lightningMode").set(lightningMode);
        shader.safeGetUniform("blackouts").set(blackouts);
        shader.safeGetUniform("useBodyAlphaMask").set(useBodyAlphaMask);
    }

    /** backport: a snapshot for a recorded draw (callers may reuse their instance). */
    public PlanetInfo copy() {
        PlanetInfo c = new PlanetInfo();
        System.arraycopy(nukeShockTime, 0, c.nukeShockTime, 0, MAX_SHOCKS);
        System.arraycopy(nukeShockCenterX, 0, c.nukeShockCenterX, 0, MAX_SHOCKS);
        System.arraycopy(nukeShockCenterY, 0, c.nukeShockCenterY, 0, MAX_SHOCKS);
        System.arraycopy(nukeShockStrength, 0, c.nukeShockStrength, 0, MAX_SHOCKS);
        c.atmosphereColorR = atmosphereColorR;
        c.atmosphereColorG = atmosphereColorG;
        c.atmosphereColorB = atmosphereColorB;
        c.cloudColorR = cloudColorR;
        c.cloudColorG = cloudColorG;
        c.cloudColorB = cloudColorB;
        c.cloudTintStrength = cloudTintStrength;
        c.cloudStormDarkness = cloudStormDarkness;
        c.atmosphereAlpha = atmosphereAlpha;
        c.atmosphereTime = atmosphereTime;
        c.patternOffset = patternOffset;
        c.impactTime = impactTime;
        c.phase = phase;
        c.offset = offset;
        c.atmosphereDensity = atmosphereDensity;
        c.cloudLightningStrength = cloudLightningStrength;
        c.eveFlashStrength = eveFlashStrength;
        c.nukeShockCount = nukeShockCount;
        c.atmosphereStyle = atmosphereStyle;
        c.lightningMode = lightningMode;
        c.blackouts = blackouts;
        c.useBodyAlphaMask = useBodyAlphaMask;
        return c;
    }
}
