// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.mojang.serialization.MapCodec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;

public final class SpaceChunkGenerators {
    private static final Map<ResourceLocation, MapCodec<? extends ChunkGenerator>> CODECS =
            new LinkedHashMap<>();

    private SpaceChunkGenerators() {}

    static void register(String name, MapCodec<? extends ChunkGenerator> codec) {
        if (CODECS.putIfAbsent(ResourceLocation.parse(name), codec) != null) {
            throw new IllegalStateException(name);
        }
    }

    public static void forEach(
            BiConsumer<ResourceLocation, MapCodec<? extends ChunkGenerator>> registrar) {
        CODECS.forEach(registrar);
    }
}
