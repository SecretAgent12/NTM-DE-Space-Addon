// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.machine.MachineElectrolyser;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockLayout;
import com.hbmspace.blocks.multiblock.LegacyLayouts;
import com.hbmspace.blocks.multiblock.SpaceReshapes;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MachineElectrolyser.class)
public abstract class MixinMachineElectrolyser extends BlockMultiblockCore {
    protected MixinMachineElectrolyser(Properties properties) {
        super(properties);
    }

    // backport: 1.0.3 bakes the electrolyser's layout; the side connections and proxies NTM: Space
    // adds (in 1.0.2 at the tail of visitCells / visitPassiveCells) come with a layout of its own
    @Override
    protected MultiblockLayout runtimeLayout(int declaredCaps) {
        return LegacyLayouts.build(new SpaceReshapes.Electrolyser(this), declaredCaps);
    }
}
