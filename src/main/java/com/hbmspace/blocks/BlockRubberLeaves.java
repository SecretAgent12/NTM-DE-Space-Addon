// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import com.hbmspace.backport.compat.LeavesBlockCompat;

public class BlockRubberLeaves extends LeavesBlockCompat {

    public static final MapCodec<BlockRubberLeaves> CODEC = simpleCodec(BlockRubberLeaves::new);

    public BlockRubberLeaves(Properties properties) {
        super(properties); // backport: 1.21.1 LeavesBlock has no falling-leaf chance
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    // backport: 26.x spawnFallingLeavesParticle (no particles) - 1.21.1 leaves spawn none anyway
}
