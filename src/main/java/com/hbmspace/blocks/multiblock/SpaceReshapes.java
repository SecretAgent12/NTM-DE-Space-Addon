// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.multiblock;

import static com.hbm.blocks.multiblock.BlockMultiblockCore.*;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbmspace.blocks.machine.ChungusPlacementSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * NTM: Space's reshapes of core machines: the 1.0.2 shape of the machine ({@link CoreShapes102})
 * with the changes NTM: Space's mixins made to it.
 *
 * <p>backport: in 1.0.2 the mixins changed the machines' shape methods; in 1.0.3 those methods are
 * gone (the layout is baked), so the mixins hand the core the layout of these shapes instead
 * (runtimeLayout).
 */
public final class SpaceReshapes {

    private SpaceReshapes() {}

    /** MachineElectrolyser: connections and proxies on both long sides. */
    public static final class Electrolyser extends CoreShapes102.MachineElectrolyser {
        public Electrolyser(BlockMultiblockCore block) {
            super(block);
        }

        @Override
        protected void visitCells(
                BlockPos core, Direction facing, SpaceMultiblockCore.CellVisitor visitor) {
            super.visitCells(core, facing, visitor);
            Direction rot = facing.getClockWise();
            int dx = facing.getStepX(), dz = facing.getStepZ();
            int rx = rot.getStepX(), rz = rot.getStepZ();
            int side = 1 << rot.ordinal();
            int opposite = 1 << rot.getOpposite().ordinal();
            int back = 1 << facing.getOpposite().ordinal();
            int front = 1 << facing.ordinal();

            visitor.cell(core.offset(-rx * 3, 0, -rz * 3), opposite);
            visitor.cell(core.offset(rx, 0, rz), side);
            visitor.cell(core.offset(-dx * 5 + rx, 0, -dz * 5 + rz), back | side);
            visitor.cell(core.offset(dx * 5 + rx, 0, dz * 5 + rz), front | side);
        }

        @Override
        protected void visitPassiveCells(
                BlockPos core, Direction facing, SpaceMultiblockCore.PassiveCellVisitor visitor) {
            super.visitPassiveCells(core, facing, visitor);
            Direction rot = facing.getClockWise();
            int rx = rot.getStepX(), rz = rot.getStepZ();
            int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN | PASSIVE_ITEMS;
            visitor.passiveCell(core.offset(-rx * 3, 0, -rz * 3), MASK_ALL, domains);
            visitor.passiveCell(core.offset(rx, 0, rz), MASK_ALL, domains);
        }
    }

    /** MachineLPW2: placed at the nozzle, fluid inlets on the side. */
    public static final class LPW2 extends CoreShapes102.MachineLPW2 {
        public LPW2(BlockMultiblockCore block) {
            super(block);
        }

        @Override
        public int getOffset() {
            return 9;
        }

        @Override
        protected void visitCells(
                BlockPos core, Direction facing, SpaceMultiblockCore.CellVisitor visitor) {
            super.visitCells(core, facing, visitor);
            Direction rot = facing.getClockWise();
            visitor.cell(core.relative(facing, 3).relative(rot.getOpposite()).above(3), MASK_SOUTH);
            visitor.cell(core.relative(facing, -3).relative(rot.getOpposite()).above(3), MASK_NORTH);
        }

        @Override
        protected void visitPassiveCells(
                BlockPos core, Direction facing, SpaceMultiblockCore.PassiveCellVisitor visitor) {
            Direction rot = facing.getClockWise();
            visitor.passiveCell(
                    core.relative(facing, 3).relative(rot.getOpposite()).above(3),
                    MASK_ALL,
                    PASSIVE_FLUID_IN);
            visitor.passiveCell(
                    core.relative(facing, -3).relative(rot.getOpposite()).above(3),
                    MASK_ALL,
                    PASSIVE_FLUID_IN);
        }
    }

    /** MachineChungus: the whole turbine (arms included) must be clear to place it. */
    public static final class Chungus extends CoreShapes102.MachineChungus {
        public Chungus(BlockMultiblockCore block) {
            super(block);
        }

        @Override
        public int[] getDimensions() {
            return ChungusPlacementSpace.DIMENSIONS;
        }

        @Override
        protected int[] cellBox() {
            return ChungusPlacementSpace.BOXES[0];
        }

        @Override
        public boolean checkRequirement(
                @Nullable Level level, BlockPos placed, Direction dir, int o) {
            return ChungusPlacementSpace.check(level, placed, dir, o);
        }
    }
}
