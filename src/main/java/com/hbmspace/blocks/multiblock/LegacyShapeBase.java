// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.multiblock;

import static com.hbm.blocks.multiblock.BlockMultiblockCore.MASK_ALL;
import static com.hbm.blocks.multiblock.BlockMultiblockCore.MASK_NONE;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A 1.0.2 multiblock shape kept apart from its block: the 1.0.2 BlockMultiblockCore shape methods
 * with their 1.0.2 defaults, for blocks that cannot extend {@link SpaceMultiblockCore} (NTM:
 * Space's subclasses of core machines and its reshapes of core machines). The block hands the
 * layout of such a shape to the core from its runtimeLayout. Cell sound, variable footprint and the
 * core's passive faces are the block's own.
 */
public abstract class LegacyShapeBase implements LegacyLayouts.Shape {

    protected final BlockMultiblockCore block;

    /** 1.0.2 BlockMultiblockCore.bounding. */
    protected final List<AABB> bounding = new ArrayList<>();

    protected LegacyShapeBase(BlockMultiblockCore block) {
        this.block = block;
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

    protected void visitCells(BlockPos core, Direction facing, SpaceMultiblockCore.CellVisitor visitor) {
        MultiblockHandlerXR.visitBox(core, cellBox(), facing, visitor);
    }

    /**
     * The box the default {@link #visitCells} visits: the dimensions box, as in 1.0.2. A reshape
     * that widens a machine's dimensions without widening its cells (the turbine) keeps the old box
     * here.
     */
    protected int[] cellBox() {
        return getDimensions();
    }

    protected void visitPassiveCells(
            BlockPos core, Direction facing, SpaceMultiblockCore.PassiveCellVisitor visitor) {}

    protected final void passiveEverywhere(
            BlockPos core,
            Direction facing,
            SpaceMultiblockCore.PassiveCellVisitor visitor,
            int domains) {
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

    public boolean checkRequirement(@Nullable Level level, BlockPos placed, Direction dir, int o) {
        return LegacyLayouts.checkDimensions(level, getDimensions(), placed, dir, o);
    }

    // ---------------------------------------------------------------- Shape

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
        visitCells(core, facing, SpaceMultiblockCore.CellVisitor.of(visitor));
    }

    @Override
    public void passiveCells(BlockPos core, Direction facing, LegacyLayouts.PassiveCellVisitor visitor) {
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
        return block.passiveCoreMask();
    }

    @Override
    public int passiveCoreDomainsFor(int declaredCaps) {
        return block.passiveCoreDomains(declaredCaps);
    }

    @Override
    public boolean variableFootprint() {
        return block.hasVariableFootprint();
    }

    @Override
    public String cellSound() {
        return LegacyLayouts.soundKey(block.cellSoundType());
    }
}
