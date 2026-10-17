// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.multiblock;

import static com.hbm.blocks.multiblock.BlockMultiblockCore.*;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * The NTM 1.0.2 shapes of the core machines NTM: Space builds on (its subclasses of them and its
 * reshapes of them), as 1.0.2 wrote them in the machines' blocks.
 *
 * <p>backport: 1.0.3 replaced these methods by its baked layouts. A Space block that extends or
 * reshapes one of these machines adds its 1.0.2 changes on top of the 1.0.2 shape, so the shape is
 * kept here; built through {@link LegacyLayouts} each gives exactly the layout 1.0.3 bakes for the
 * machine (checked against multiblock_layouts.nbt of NTM 1.0.3).
 */
public final class CoreShapes102 {

    private CoreShapes102() {}

    /** DeuteriumTower, NTM 1.0.2. */
    public static class DeuteriumTower extends LegacyShapeBase {
        public DeuteriumTower(BlockMultiblockCore block) {
            super(block);
        }

        private static final int[] DIMENSIONS = {9, 0, 1, 0, 0, 1};
        @Override
        public int[] getDimensions() {
            return DIMENSIONS;
        }
        @Override
        public int getOffset() {
            return 0;
        }
        @Override
        public int coreMask() {
            return MASK_SOUTH | MASK_WEST;
        }
        @Override
        protected void visitCells(BlockPos core, Direction facing, SpaceMultiblockCore.CellVisitor visitor) {
            super.visitCells(core, facing, visitor);
            Direction rot = facing.getClockWise();
            int dx = -facing.getStepX() - rot.getStepX();
            int dz = -facing.getStepZ() - rot.getStepZ();
            visitor.cell(core.offset(0, 0, dz), MASK_NORTH | MASK_WEST);
            visitor.cell(core.offset(dx, 0, dz), MASK_EAST | MASK_NORTH);
            visitor.cell(core.offset(dx, 0, 0), MASK_EAST | MASK_SOUTH);
        }
        @Override
        protected void visitPassiveCells(BlockPos core, Direction facing, SpaceMultiblockCore.PassiveCellVisitor visitor) {
            int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN;
            Direction rot = facing.getClockWise();
            int dx = -facing.getStepX() - rot.getStepX();
            int dz = -facing.getStepZ() - rot.getStepZ();
            visitor.passiveCell(core.offset(0, 0, dz), MASK_ALL, domains);
            visitor.passiveCell(core.offset(dx, 0, dz), MASK_ALL, domains);
            visitor.passiveCell(core.offset(dx, 0, 0), MASK_ALL, domains);
        }
    }

    /** MachineIntake, NTM 1.0.2. */
    public static class MachineIntake extends LegacyShapeBase {
        public MachineIntake(BlockMultiblockCore block) {
            super(block);
        }

        private static final int[] DIMENSIONS = {0, 0, 1, 0, 1, 0};
        @Override
        public int[] getDimensions() {
            return DIMENSIONS;
        }
        @Override
        public int getOffset() {
            return 0;
        }
        @Override
        public int coreMask() {
            return MASK_EAST | MASK_SOUTH;
        }
        @Override
        protected void visitCells(BlockPos core, Direction facing, SpaceMultiblockCore.CellVisitor visitor) {
            super.visitCells(core, facing, visitor);

            Direction rot = facing.getClockWise();
            BlockPos back = core.relative(facing.getOpposite());
            visitor.cell(back, MASK_NORTH | MASK_EAST);
            visitor.cell(core.relative(rot), MASK_SOUTH | MASK_WEST);
            visitor.cell(back.relative(rot), MASK_NORTH | MASK_WEST);
        }
        @Override
        protected void visitPassiveCells(BlockPos core, Direction facing, SpaceMultiblockCore.PassiveCellVisitor visitor) {
            passiveEverywhere(core, facing, visitor, PASSIVE_POWER_IN | PASSIVE_FLUID_IN);
        }
    }

    /** MachineElectrolyser, NTM 1.0.2. */
    public static class MachineElectrolyser extends LegacyShapeBase {
        public MachineElectrolyser(BlockMultiblockCore block) {
            super(block);
        }

        private static final int[] PLACEMENT_DIMENSIONS = {
            0, 0, 5, 5, 1, 3, 0, 0, 0,
            2, -1, 5, 5, 1, 1, 0, 0, 0,
            3, -3, 5, 5, 0, 0, 0, 0, 0,
            3, -1, 4, -4, -3, 3, 0, 0, 0,
            3, -1, 2, -2, -3, 3, 0, 0, 0,
            3, -1, 0, 0, -3, 3, 0, 0, 0,
            3, -1, -2, 2, -3, 3, 0, 0, 0,
            3, -1, -4, 4, -3, 3, 0, 0, 0,
            0, 0, 0, 0, -1, 2, 4, 3, 0,
            0, 0, 0, 0, -1, 2, 2, 3, 0,
            0, 0, 0, 0, -1, 2, 0, 3, 0,
            0, 0, 0, 0, -1, 2, -2, 3, 0,
            0, 0, 0, 0, -1, 2, -4, 3, 0,
        };
        private static final int[] DIMENSIONS = {0, 0, 5, 5, 1, 3};
        private static final int[] TOWER = {0, 0, 0, 0, -1, 2};
        private static final int[][] SLABS = {
            {2, -1, 5, 5, 1, 1},
            {3, -3, 5, 5, 0, 0},
            {3, -1, 4, -4, -3, 3},
            {3, -1, 2, -2, -3, 3},
            {3, -1, 0, 0, -3, 3},
            {3, -1, -2, 2, -3, 3},
            {3, -1, -4, 4, -3, 3},
        };
        private static final int[] TOWER_STEPS = {4, 2, 0, -2, -4};
        @Override
        public int[] getDimensions() {
            return DIMENSIONS;
        }
        @Override
        public int getOffset() {
            return 5;
        }
        @Override
        protected void visitCells(BlockPos core, Direction facing, SpaceMultiblockCore.CellVisitor visitor) {
            super.visitCells(core, facing, visitor);

            for (int[] slab : SLABS) {
                MultiblockHandlerXR.visitBox(core, slab, facing, visitor);
            }
            for (int step : TOWER_STEPS) {
                MultiblockHandlerXR.visitBox(
                        core.offset(facing.getStepX() * step, 3, facing.getStepZ() * step),
                        TOWER,
                        facing,
                        visitor);
            }

            Direction rot = facing.getClockWise();
            int dx = facing.getStepX(), dz = facing.getStepZ();
            int rx = rot.getStepX(), rz = rot.getStepZ();

            visitor.cell(core.offset(-dx * 5, 0, -dz * 5), MASK_NORTH);
            visitor.cell(core.offset(-dx * 5 + rx, 0, -dz * 5 + rz), MASK_NORTH);
            visitor.cell(core.offset(-dx * 5 - rx, 0, -dz * 5 - rz), MASK_NORTH);
            visitor.cell(core.offset(dx * 5, 0, dz * 5), MASK_SOUTH);
            visitor.cell(core.offset(dx * 5 + rx, 0, dz * 5 + rz), MASK_SOUTH);
            visitor.cell(core.offset(dx * 5 - rx, 0, dz * 5 - rz), MASK_SOUTH);
        }
        @Override
        protected void visitPassiveCells(BlockPos core, Direction facing, SpaceMultiblockCore.PassiveCellVisitor visitor) {
            Direction rot = facing.getClockWise();
            int dx = facing.getStepX(), dz = facing.getStepZ();
            int rx = rot.getStepX(), rz = rot.getStepZ();
            int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN | PASSIVE_ITEMS;

            visitor.passiveCell(core.offset(-dx * 5, 0, -dz * 5), MASK_ALL, domains);
            visitor.passiveCell(core.offset(-dx * 5 + rx, 0, -dz * 5 + rz), MASK_ALL, domains);
            visitor.passiveCell(core.offset(-dx * 5 - rx, 0, -dz * 5 - rz), MASK_ALL, domains);
            visitor.passiveCell(core.offset(dx * 5, 0, dz * 5), MASK_ALL, domains);
            visitor.passiveCell(core.offset(dx * 5 + rx, 0, dz * 5 + rz), MASK_ALL, domains);
            visitor.passiveCell(core.offset(dx * 5 - rx, 0, dz * 5 - rz), MASK_ALL, domains);
        }
        @Override
        protected int[] placementDimensionBoxes() {
            return PLACEMENT_DIMENSIONS;
        }
        @Override
        public boolean checkRequirement(Level level, BlockPos placed, Direction dir, int o) {
            if (!super.checkRequirement(level, placed, dir, o)) return false;
            BlockPos origin = placed.offset(dir.getStepX() * o, dir.getStepY() * o, dir.getStepZ() * o);

            for (int[] slab : SLABS) {
                if (!LegacyLayouts.checkSpace(level, origin, slab, placed, dir)) return false;
            }
            for (int step : TOWER_STEPS) {
                BlockPos at = origin.offset(dir.getStepX() * step, 3, dir.getStepZ() * step);
                if (!LegacyLayouts.checkSpace(level, at, TOWER, placed, dir)) return false;
            }
            return true;
        }
    }

    /** MachineLPW2, NTM 1.0.2. */
    public static class MachineLPW2 extends LegacyShapeBase {
        public MachineLPW2(BlockMultiblockCore block) {
            super(block);
        }

        private static final int[] DIMENSIONS = {6, 0, 3, 3, 9, 10};
        @Override
        public int[] getDimensions() {
            return DIMENSIONS;
        }
        @Override
        public int getOffset() {
            return 3;
        }
    }

    /** MachineChungus, NTM 1.0.2. */
    public static class MachineChungus extends LegacyShapeBase {
        public MachineChungus(BlockMultiblockCore block) {
            super(block);
        }

        private static final int[] PLACEMENT_DIMENSIONS = {
            3, 0, 0, 3, 2, 2, 0, 0, 0,
            4, -4, 0, 3, 1, 1, 0, 0, 0,
            3, 0, 6, -1, 1, 1, 0, 0, 0,
            2, 0, 10, -7, 1, 1, 0, 0, 0,
        };
        private static final int[] DIMENSIONS = {3, 0, 0, 3, 2, 2};
        private static final int[] ARM_A = {4, -4, 0, 3, 1, 1};
        private static final int[] ARM_B = {3, 0, 6, -1, 1, 1};
        private static final int[] ARM_C = {2, 0, 10, -7, 1, 1};
        @Override
        public int[] getDimensions() {
            return DIMENSIONS;
        }
        @Override
        public int getOffset() {
            return 3;
        }
        @Override
        protected void visitCells(BlockPos core, Direction facing, SpaceMultiblockCore.CellVisitor visitor) {
            super.visitCells(core, facing, visitor);
            MultiblockHandlerXR.visitBox(core, ARM_A, facing, visitor);
            MultiblockHandlerXR.visitBox(core, ARM_B, facing, visitor);
            MultiblockHandlerXR.visitBox(core, ARM_C, facing, visitor);

            Direction rot = facing.getClockWise();
            visitor.cell(core.relative(rot, 2), MASK_WEST, ROLE_FLUID);
            visitor.cell(core.relative(rot, -2), MASK_EAST, ROLE_FLUID);
            visitor.cell(core.relative(facing, -10), MASK_NORTH, ROLE_POWER);

            visitor.cell(core.relative(facing, 4).above(2), MASK_SOUTH, ROLE_FLUID);
        }
        @Override
        protected void visitPassiveCells(BlockPos core, Direction facing, SpaceMultiblockCore.PassiveCellVisitor visitor) {
            Direction rot = facing.getClockWise();
            int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN;

            visitor.passiveCell(core.relative(facing, 4).above(2), MASK_ALL, domains);
            visitor.passiveCell(core.relative(facing, -10), MASK_ALL, domains);
            visitor.passiveCell(core.relative(rot, 2), MASK_ALL, domains);
            visitor.passiveCell(core.relative(rot, -2), MASK_ALL, domains);
        }
        @Override
        protected int[] placementDimensionBoxes() {
            return PLACEMENT_DIMENSIONS;
        }
        @Override
        public boolean checkRequirement(Level level, BlockPos placed, Direction dir, int o) {
            if (!super.checkRequirement(level, placed, dir, o)) return false;
            BlockPos origin = placed.offset(dir.getStepX() * o, dir.getStepY() * o, dir.getStepZ() * o);
            if (!LegacyLayouts.checkSpace(level, origin, ARM_B, placed, dir)) return false;
            if (!LegacyLayouts.checkSpace(level, origin, ARM_C, placed, dir)) return false;
            BlockPos top = placed.offset(dir.getStepX(), 2, dir.getStepZ());
            return LegacyLayouts.replaceable(level, top);
        }
    }
}
