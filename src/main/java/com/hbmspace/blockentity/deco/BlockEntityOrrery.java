// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.deco;

import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.hbm.backport.BlockEntityCompat;

public class BlockEntityOrrery extends BlockEntityCompat {

    public BlockEntityOrrery(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.ORRERY.get(), pos, state);
    }
}
