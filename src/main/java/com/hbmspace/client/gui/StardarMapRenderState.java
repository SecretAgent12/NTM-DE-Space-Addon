// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbmspace.client.sky.PlanetInfo;
import com.hbm.backport.client.rendertype.RenderPipeline;
import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import com.hbm.backport.client.gui.state.pip.PictureInPictureRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public record StardarMapRenderState(List<Draw> draws, int x0, int y0, int x1, int y1)
        implements PictureInPictureRenderState {

    public sealed interface Draw {}

    public record Textured(
            RenderPipeline pipeline,
            @Nullable ResourceLocation texture,
            @Nullable ResourceLocation texture1,
            @Nullable ResourceLocation texture2,
            @Nullable PlanetInfo planet,
            float r,
            float g,
            float b,
            float a,
            float x0,
            float y0,
            float x1,
            float y1,
            float u0,
            float v0,
            float u1,
            float v1,
            float angle,
            float pivotX,
            float pivotY)
            implements Draw {}

    public record Colored(float[] vertices) implements Draw {}

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

    @Override
    public ScreenRectangle bounds() {
        return new ScreenRectangle(x0, y0, x1 - x0, y1 - y0);
    }
}
