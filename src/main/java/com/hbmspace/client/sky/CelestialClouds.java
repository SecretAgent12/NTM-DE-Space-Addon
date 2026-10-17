// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 detree05
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.api.client.CloudLayers;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import com.hbm.backport.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * The extra cloud layers of planets with thick air (WorldProviderCelestial.getCloudLayerCount), above
 * vanilla's clouds, each fainter, slower and shifted by a per-planet offset.
 *
 * <p>backport: 26.x layers are CloudRenderer instances; on 1.21.1 each is a {@link CloudLayer}.
 */
public final class CelestialClouds implements CloudLayers.Renderer {

    private static final float[] LAYER_HEIGHT_OFFSETS = {0.0F, 12.0F, 24.0F};
    private static final float[] LAYER_ALPHA_MULTIPLIERS = {1.0F, 0.84F, 0.76F};
    private static final double[] LAYER_SCROLL_MULTIPLIERS = {1.0D, 0.84D, 0.7D};
    private static final double LAYER_POSITION_RANGE = 16384.0D;
    private static final double SCROLL_SPEED = 0.03D;

    private final @Nullable CloudLayer[] layers = new CloudLayer[LAYER_HEIGHT_OFFSETS.length];

    public static void register() {
        CloudLayers.register(new CelestialClouds());
    }

    @Override
    public void render(
            int color,
            CloudStatus status,
            float bottomY,
            int range,
            Vec3 camera,
            long gameTime,
            float partialTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || WorldProviderCelestial.of(level) == null) return;
        // backport: the core's cloud hook does not hand on renderClouds' matrices (SkyFrame)
        Matrix4f modelView = SkyFrame.viewRotation();
        Matrix4f projection = SkyFrame.projection();

        int count =
                WorldProviderCelestial.getCloudLayerCount(
                        CelestialBody.getBody(level).getTrait(CBT_Atmosphere.class));
        long dimension = level.dimension().location().hashCode();
        double time = gameTime + partialTicks;

        for (int i = 1; i < count && i < layers.length; i++) {
            if (layers[i] == null) layers[i] = new CloudLayer();

            double shiftX =
                    (LAYER_SCROLL_MULTIPLIERS[i] - 1.0D) * time * SCROLL_SPEED
                            + offset(dimension, i, 0x4F1BBCDCBFA54001L);
            double shiftZ = offset(dimension, i, 0x2C1B3C6D5E7F8103L);
            int alpha = Math.round(ARGB.alpha(color) * LAYER_ALPHA_MULTIPLIERS[i]);

            layers[i].render(
                    ARGB.color(alpha, color),
                    status,
                    bottomY + LAYER_HEIGHT_OFFSETS[i],
                    camera.add(shiftX, 0.0D, shiftZ),
                    gameTime,
                    partialTicks,
                    modelView,
                    projection);
        }
    }

    @Override
    public void reload() {
        CloudLayer.reloadShape();
        close();
    }

    @Override
    public void markForRebuild() {
        for (CloudLayer layer : layers) {
            if (layer != null) layer.markForRebuild();
        }
    }

    @Override
    public void close() {
        for (int i = 0; i < layers.length; i++) {
            if (layers[i] != null) {
                layers[i].close();
                layers[i] = null;
            }
        }
    }

    private static double offset(long dimension, int layer, long salt) {
        long mixed =
                mix(
                        (dimension * 0x9E3779B97F4A7C15L)
                                ^ ((long) (layer + 1) * 0xC2B2AE3D27D4EB4FL)
                                ^ salt);
        double normalized = (double) (mixed & ((1L << 53) - 1)) / (double) (1L << 53);
        return (normalized - 0.5D) * LAYER_POSITION_RANGE;
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }
}
