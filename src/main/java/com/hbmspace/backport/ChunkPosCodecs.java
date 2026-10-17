// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.backport;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;

/** backport: 26.x com.hbmspace.backport.ChunkPosCodecs.CODEC / STREAM_CODEC, which 1.21.1's ChunkPos does not have. */
public final class ChunkPosCodecs {

    private ChunkPosCodecs() {}

    /** [x, z], like BlockPos.CODEC. */
    public static final Codec<ChunkPos> CODEC =
            Codec.INT_STREAM.comapFlatMap(
                    stream -> Util.fixedSize(stream, 2).map(a -> new ChunkPos(a[0], a[1])),
                    pos -> IntStream.of(pos.x, pos.z));

    public static final StreamCodec<ByteBuf, ChunkPos> STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(ChunkPos::new, ChunkPos::toLong);
}
