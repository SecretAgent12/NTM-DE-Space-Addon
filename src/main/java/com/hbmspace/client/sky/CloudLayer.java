// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.client.sky;

import com.hbm.backport.ARGB;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * One extra cloud layer, shaped by the vanilla cloud texture like vanilla's clouds (a 12 x 12 cell
 * per texel, 4 blocks thick when fancy, flat when fast) and drawn with vanilla's cloud render types.
 *
 * <p>backport: 26.x's extra layers are more instances of its CloudRenderer. 1.21.1's clouds are
 * built inside LevelRenderer (one mesh, its own scroll state), so a layer is built here: one box per
 * cloudy texel within {@link #RADIUS} cells, faces between two cloudy cells left out, cached in a
 * vertex buffer around the cell the (scrolled) camera is in and rebuilt when that cell, the colour or
 * the cloud setting changes.
 */
final class CloudLayer implements AutoCloseable {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/environment/clouds.png");
    private static final int CELL = 12;
    private static final float THICKNESS = 4F;
    private static final int RADIUS = 32;
    private static final double SCROLL_SPEED = 0.03D;

    private static boolean @Nullable [] shape;
    private static int shapeWidth;
    private static int shapeHeight;

    private @Nullable VertexBuffer buffer;
    private boolean empty;
    private int builtX;
    private int builtZ;
    private int builtColor;
    private @Nullable CloudStatus builtStatus;

    /** Forgets the cloud texture (resource reload). */
    static void reloadShape() {
        shape = null;
    }

    void markForRebuild() {
        builtStatus = null;
    }

    void render(
            int color,
            CloudStatus status,
            float bottomY,
            Vec3 camera,
            long gameTime,
            float partialTicks,
            Matrix4f modelView,
            Matrix4f projection) {
        if (status == CloudStatus.OFF || !loadShape()) return;
        double x = camera.x + (gameTime + partialTicks) * SCROLL_SPEED;
        double z = camera.z;
        int cellX = Mth.floor(x / CELL);
        int cellZ = Mth.floor(z / CELL);
        if (buffer == null
                || cellX != builtX
                || cellZ != builtZ
                || color != builtColor
                || status != builtStatus) build(cellX, cellZ, color, status);
        if (empty) return;

        Matrix4f pose =
                new Matrix4f(modelView)
                        .translate(
                                (float) (cellX * CELL - x),
                                (float) (bottomY - camera.y),
                                (float) (cellZ * CELL - z));
        buffer.bind();
        if (status == CloudStatus.FANCY) draw(RenderType.cloudsDepthOnly(), pose, projection);
        draw(RenderType.clouds(), pose, projection);
        VertexBuffer.unbind();
    }

    private void draw(RenderType type, Matrix4f pose, Matrix4f projection) {
        type.setupRenderState();
        buffer.drawWithShader(pose, projection, RenderSystem.getShader());
        type.clearRenderState();
    }

    private void build(int cellX, int cellZ, int color, CloudStatus status) {
        builtX = cellX;
        builtZ = cellZ;
        builtColor = color;
        builtStatus = status;
        BufferBuilder builder =
                Tesselator.getInstance()
                        .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        boolean fancy = status == CloudStatus.FANCY;
        for (int i = -RADIUS; i < RADIUS; i++) {
            for (int j = -RADIUS; j < RADIUS; j++) {
                int tx = Math.floorMod(cellX + i, shapeWidth);
                int tz = Math.floorMod(cellZ + j, shapeHeight);
                if (!cloudy(tx, tz)) continue;
                Cell cell = new Cell(builder, color, i * CELL, j * CELL, tx, tz);
                if (!fancy) {
                    cell.top(0F);
                    continue;
                }
                cell.top(THICKNESS);
                cell.bottom();
                if (!cloudy(tx + 1, tz)) cell.east();
                if (!cloudy(tx - 1, tz)) cell.west();
                if (!cloudy(tx, tz + 1)) cell.south();
                if (!cloudy(tx, tz - 1)) cell.north();
            }
        }
        MeshData mesh = builder.build();
        empty = mesh == null;
        if (buffer == null) buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        if (mesh != null) {
            buffer.bind();
            buffer.upload(mesh);
            VertexBuffer.unbind();
        }
    }

    private static boolean cloudy(int tx, int tz) {
        return shape[Math.floorMod(tz, shapeHeight) * shapeWidth + Math.floorMod(tx, shapeWidth)];
    }

    private static boolean loadShape() {
        if (shape != null) return true;
        try (InputStream in =
                        Minecraft.getInstance().getResourceManager().open(TEXTURE);
                NativeImage image = NativeImage.read(in)) {
            int w = image.getWidth(), h = image.getHeight();
            boolean[] cells = new boolean[w * h];
            for (int z = 0; z < h; z++)
                for (int x = 0; x < w; x++)
                    cells[z * w + x] = image.getPixelRGBA(x, z) >>> 24 >= 128; // alpha byte (ABGR)
            shapeWidth = w;
            shapeHeight = h;
            shape = cells;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** The faces of one cloudy cell; the texture coordinate is the texel's centre (fully opaque). */
    private record Cell(BufferBuilder builder, int color, float x0, float z0, float u, float v) {

        Cell(BufferBuilder builder, int color, float x0, float z0, int tx, int tz) {
            this(
                    builder,
                    color,
                    x0,
                    z0,
                    (tx + .5F) / shapeWidth,
                    (tz + .5F) / shapeHeight);
        }

        void top(float y) {
            face(1F, 0F, 1F, 0F, x0, y, z0, x0, y, z0 + CELL, x0 + CELL, y, z0 + CELL, x0 + CELL, y, z0);
        }

        void bottom() {
            face(.7F, 0F, -1F, 0F, x0, 0F, z0, x0 + CELL, 0F, z0, x0 + CELL, 0F, z0 + CELL, x0, 0F, z0 + CELL);
        }

        void east() {
            float x = x0 + CELL;
            face(.9F, 1F, 0F, 0F, x, 0F, z0, x, THICKNESS, z0, x, THICKNESS, z0 + CELL, x, 0F, z0 + CELL);
        }

        void west() {
            face(.9F, -1F, 0F, 0F, x0, 0F, z0, x0, 0F, z0 + CELL, x0, THICKNESS, z0 + CELL, x0, THICKNESS, z0);
        }

        void south() {
            float z = z0 + CELL;
            face(.8F, 0F, 0F, 1F, x0, 0F, z, x0 + CELL, 0F, z, x0 + CELL, THICKNESS, z, x0, THICKNESS, z);
        }

        void north() {
            face(.8F, 0F, 0F, -1F, x0, 0F, z0, x0, THICKNESS, z0, x0 + CELL, THICKNESS, z0, x0 + CELL, 0F, z0);
        }

        private void face(float shade, float nx, float ny, float nz, float... corners) {
            float r = ARGB.redFloat(color) * shade;
            float g = ARGB.greenFloat(color) * shade;
            float b = ARGB.blueFloat(color) * shade;
            float a = ARGB.alphaFloat(color);
            for (int k = 0; k < 12; k += 3) {
                builder.addVertex(corners[k], corners[k + 1], corners[k + 2])
                        .setUv(u, v)
                        .setColor(r, g, b, a)
                        .setNormal(nx, ny, nz);
            }
        }
    }

    @Override
    public void close() {
        if (buffer != null) {
            buffer.close();
            buffer = null;
        }
    }
}
