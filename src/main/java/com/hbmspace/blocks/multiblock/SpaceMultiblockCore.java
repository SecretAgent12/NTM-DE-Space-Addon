// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.multiblock;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbm.blocks.multiblock.MultiblockLayout;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The base of NTM: Space's multiblocks: a 1.21.1 / NTM 1.0.3 multiblock core whose shape is still
 * written the NTM 1.0.2 way, with the 1.0.2 methods and their 1.0.2 defaults (dimensions box,
 * offset, visitCells / visitPassiveCells, bounding boxes, heat / soil / proxy cells, placement
 * checks and preview boxes).
 *
 * <p>backport: NTM: Space is written against NTM 1.0.2. In 1.0.3 these methods are gone from
 * BlockMultiblockCore and a multiblock's layout comes baked from the data generator; this class
 * hands the core the layout {@link LegacyLayouts} builds from them at runtime
 * ({@link #runtimeLayout}, the hook the RBMK columns use), and keeps the 1.0.2 placed / teardown
 * cell visits (CellVisitor) behind 1.0.3's (FootprintVisitor).
 */
public abstract class SpaceMultiblockCore extends BlockMultiblockCore {

    /** 1.0.2 BlockMultiblockCore.bounding: the outline boxes, around the core, facing north. */
    protected final List<AABB> bounding = new ArrayList<>();

    protected SpaceMultiblockCore(Properties properties) {
        super(properties);
    }

    // ---------------------------------------------------------------- 1.0.2 shape API

    public abstract int[] getDimensions();

    public abstract int getOffset();

    public int getHeightOffset() {
        return 0;
    }

    public int coreMask() {
        return MASK_NONE;
    }

    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        MultiblockHandlerXR.visitBox(core, getDimensions(), facing, visitor);
    }

    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {}

    protected final void passiveEverywhere(
            BlockPos core, Direction facing, PassiveCellVisitor visitor, int domains) {
        visitCells(core, facing, (pos, mask) -> visitor.passiveCell(pos, MASK_ALL, domains));
    }

    public boolean heatSourceAt(int lx, int ly, int lz) {
        return false;
    }

    public boolean plantSoilAt(int lx, int ly, int lz) {
        return false;
    }

    protected boolean proxyCellAt(int lx, int ly, int lz) {
        return false;
    }

    public @Nullable VoxelShape cellShape(
            int lx, int ly, int lz, Direction facing, boolean open, boolean forCollision) {
        return LegacyLayouts.boundingShape(boundingOutline(facing), lx, ly, lz, facing);
    }

    public @Nullable VoxelShape boundingOutline(Direction facing) {
        return LegacyLayouts.boundingOutline(bounding, facing);
    }

    public @Nullable AABB climbBox(Direction facing) {
        return null;
    }

    protected int[] placementDimensionBoxes() {
        return LegacyLayouts.dimensionBoxes(getDimensions());
    }

    protected double[] placementExtraBoxes() {
        return new double[0];
    }

    /**
     * 1.0.2 placement check. 1.0.3 places by the layout (the footprint plus the cells this tests
     * beyond it), so this is read once, when the layout is built; it still answers for code of its
     * own that asks it.
     */
    public boolean checkRequirement(@Nullable Level level, BlockPos placed, Direction dir, int o) {
        return LegacyLayouts.checkDimensions(level, getDimensions(), placed, dir, o);
    }

    /** 1.0.2 visitPlacedCells: a variable footprint visits its cells, a fixed one its footprint. */
    protected void visitPlacedCells(
            LevelAccessor level, BlockPos core, Direction facing, CellVisitor visitor) {
        if (hasVariableFootprint()) visitCells(core, facing, visitor);
        else footprint(facing).visit(core, pos -> visitor.cell(pos, MASK_NONE));
    }

    protected void visitTeardownCells(BlockPos core, Direction facing, CellVisitor visitor) {
        if (hasVariableFootprint()) visitCells(core, facing, visitor);
        else footprint(facing).visit(core, pos -> visitor.cell(pos, MASK_NONE));
    }

    protected void visitTeardownCells(
            LevelAccessor level, BlockPos core, Direction facing, CellVisitor visitor) {
        visitTeardownCells(core, facing, visitor);
    }

    // ---------------------------------------------------------------- 1.0.3 side

    @Override
    protected MultiblockLayout runtimeLayout(int declaredCaps) {
        return LegacyLayouts.build(shape(), declaredCaps);
    }

    @Override
    protected void visitPlacedCells(
            LevelAccessor level, BlockPos core, Direction facing, FootprintVisitor visitor) {
        visitPlacedCells(level, core, facing, (CellVisitor) (pos, mask) -> visitor.cell(pos));
    }

    @Override
    protected void visitTeardownCells(BlockPos core, Direction facing, FootprintVisitor visitor) {
        visitTeardownCells(core, facing, (CellVisitor) (pos, mask) -> visitor.cell(pos));
    }

    @Override
    protected void visitTeardownCells(
            LevelAccessor level, BlockPos core, Direction facing, FootprintVisitor visitor) {
        visitTeardownCells(level, core, facing, (CellVisitor) (pos, mask) -> visitor.cell(pos));
    }

    /** This block's 1.0.2 methods, as the shape a layout is built from. */
    public LegacyLayouts.Shape shape() {
        SpaceMultiblockCore block = this;
        return new LegacyLayouts.Shape() {
            @Override
            public int[] dimensions() {
                return getDimensions();
            }

            @Override
            public int offset() {
                return getOffset();
            }

            @Override
            public int heightOffset() {
                return getHeightOffset();
            }

            @Override
            public int coreFaces() {
                return coreMask();
            }

            @Override
            public void cells(BlockPos core, Direction facing, LegacyLayouts.CellVisitor visitor) {
                visitCells(core, facing, CellVisitor.of(visitor));
            }

            @Override
            public void passiveCells(
                    BlockPos core, Direction facing, LegacyLayouts.PassiveCellVisitor visitor) {
                visitPassiveCells(core, facing, visitor::passiveCell);
            }

            @Override
            public boolean heatSource(int lx, int ly, int lz) {
                return heatSourceAt(lx, ly, lz);
            }

            @Override
            public boolean plantSoil(int lx, int ly, int lz) {
                return plantSoilAt(lx, ly, lz);
            }

            @Override
            public boolean proxyCell(int lx, int ly, int lz) {
                return proxyCellAt(lx, ly, lz);
            }

            @Override
            public @Nullable VoxelShape shapeAt(
                    int lx, int ly, int lz, Direction facing, boolean open, boolean forCollision) {
                return cellShape(lx, ly, lz, facing, open, forCollision);
            }

            @Override
            public @Nullable VoxelShape outline(Direction facing) {
                return boundingOutline(facing);
            }

            @Override
            public @Nullable AABB climb(Direction facing) {
                return climbBox(facing);
            }

            @Override
            public int[] previewBoxes() {
                return placementDimensionBoxes();
            }

            @Override
            public double[] previewExtras() {
                return placementExtraBoxes();
            }

            @Override
            public boolean requirement(@Nullable Level level, BlockPos placed, Direction dir, int o) {
                return checkRequirement(level, placed, dir, o);
            }

            @Override
            public int passiveCoreFaces() {
                return passiveCoreMask();
            }

            @Override
            public int passiveCoreDomainsFor(int declaredCaps) {
                return passiveCoreDomains(declaredCaps);
            }

            @Override
            public boolean variableFootprint() {
                return hasVariableFootprint();
            }

            @Override
            public String cellSound() {
                return LegacyLayouts.soundKey(block.cellSoundType());
            }
        };
    }

    /** 1.0.2 SpaceMultiblockCore.CellVisitor. */
    public interface CellVisitor extends LegacyLayouts.CellVisitor {
        static CellVisitor of(LegacyLayouts.CellVisitor visitor) {
            if (visitor instanceof CellVisitor own) return own;
            return new CellVisitor() {
                @Override
                public void cell(BlockPos pos, int mask) {
                    visitor.cell(pos, mask);
                }

                @Override
                public void cell(BlockPos pos, int mask, int roles) {
                    visitor.cell(pos, mask, roles);
                }
            };
        }
    }

    /** 1.0.2 SpaceMultiblockCore.PassiveCellVisitor. */
    @FunctionalInterface
    public interface PassiveCellVisitor extends LegacyLayouts.PassiveCellVisitor {}
}
