// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbmspace.blockentity.machine.BlockEntityStationPropulsionCreative;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import com.hbm.backport.compat.BlockCompat;

public class BlockStationPropulsionCreative extends BlockCompat implements ITickingBlock {

    public BlockStationPropulsionCreative(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityStationPropulsionCreative(pos, state);
    }
}
