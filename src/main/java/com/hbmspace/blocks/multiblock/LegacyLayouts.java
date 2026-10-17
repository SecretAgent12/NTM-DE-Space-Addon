// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.multiblock;

import com.hbm.blocks.ClimbBox;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbm.blocks.multiblock.MultiblockLayout;
import com.hbm.blocks.multiblock.MultiblockMaskTable;
import com.hbm.util.Facing;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The multiblock layout of a block whose shape is written the NTM 1.0.2 way (dimensions, offset,
 * visitCells / visitPassiveCells, bounding boxes, ...; see {@link Shape}), built the way 1.0.2 baked
 * it at runtime.
 *
 * <p>backport: NTM: Space is written against 1.0.2, where a multiblock's mask table, footprint and
 * cell shapes were baked from those methods when the game started (MultiblockMaskTable.bake,
 * MultiblockFootprint.bake, MultiblockCellShapes.bake). NTM 1.0.3 moved that out of the game: the
 * layouts are baked by its data generator into multiblock_layouts.nbt, and a block may instead hand
 * its layout over at runtime (BlockMultiblockCore.runtimeLayout, as the RBMK columns do). This class
 * is the 1.0.2 baking, producing the 1.0.3 layout: one mask table seen from the south for every
 * facing (cells, roles, passive cells, the core's own and passive mask), the footprint per facing in
 * visit order, the cells placement checks beyond the footprint (checkRequirement, recorded through
 * {@link #checkSpace}), the placement preview and the cell geometry (bounding silhouettes, cell and
 * core shapes, climb boxes). The heat, soil and proxy queries become the table's cell flags.
 */
public final class LegacyLayouts {

    private LegacyLayouts() {}

    /**
     * A multiblock described the 1.0.2 way: each method answers what the 1.0.2 BlockMultiblockCore
     * method in its comment answered (named apart, as the block itself keeps those names).
     */
    public interface Shape {
        int[] dimensions(); // getDimensions

        int offset(); // getOffset

        int heightOffset(); // getHeightOffset

        int coreFaces(); // coreMask

        void cells(BlockPos core, Direction facing, CellVisitor visitor); // visitCells

        void passiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor); // visitPassiveCells

        boolean heatSource(int lx, int ly, int lz); // heatSourceAt

        boolean plantSoil(int lx, int ly, int lz); // plantSoilAt

        boolean proxyCell(int lx, int ly, int lz); // proxyCellAt

        @Nullable VoxelShape shapeAt(int lx, int ly, int lz, Direction facing, boolean open, boolean forCollision); // cellShape

        @Nullable VoxelShape outline(Direction facing); // boundingOutline

        @Nullable AABB climb(Direction facing); // climbBox

        int[] previewBoxes(); // placementDimensionBoxes

        double[] previewExtras(); // placementExtraBoxes

        boolean requirement(@Nullable Level level, BlockPos placed, Direction dir, int o); // checkRequirement

        int passiveCoreFaces(); // passiveCoreMask

        int passiveCoreDomainsFor(int declaredCaps); // passiveCoreDomains

        boolean variableFootprint(); // hasVariableFootprint

        /** The step sound of the cells, part of a cell shape's key (1.0.2 interned by it). */
        String cellSound();
    }

    /** 1.0.2 SpaceMultiblockCore.CellVisitor. */
    public interface CellVisitor extends MultiblockHandlerXR.BoxVisitor {
        void cell(BlockPos pos, int mask);

        default void cell(BlockPos pos, int mask, int roles) {
            cell(pos, mask);
        }

        @Override
        default void cell(BlockPos pos, Direction facingCore) {
            cell(pos, BlockMultiblockCore.MASK_NONE);
        }
    }

    /** 1.0.2 SpaceMultiblockCore.PassiveCellVisitor. */
    @FunctionalInterface
    public interface PassiveCellVisitor {
        void passiveCell(BlockPos pos, int faces, int domains);
    }

    // ---------------------------------------------------------------- placement checks

    private static final ThreadLocal<@Nullable LongLinkedOpenHashSet> RECORDING = new ThreadLocal<>();

    /**
     * 1.0.2 MultiblockHandlerXR.checkSpace: every cell of the rotated box around {@code origin}
     * but {@code placed} must be replaceable. While a layout is built it records the cells instead
     * (they become the layout's clearance).
     */
    public static boolean checkSpace(
            @Nullable Level level, BlockPos origin, int[] dim, BlockPos placed, Direction dir) {
        if (dim == null || dim.length != 6) return false;
        int[] rot = MultiblockHandlerXR.rotate(dim, dir);
        int ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();
        int px = placed.getX(), py = placed.getY(), pz = placed.getZ();
        LongLinkedOpenHashSet recording = RECORDING.get();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int a = ox - rot[4]; a <= ox + rot[5]; a++) {
            for (int b = oy - rot[1]; b <= oy + rot[0]; b++) {
                for (int c = oz - rot[2]; c <= oz + rot[3]; c++) {
                    if (a == px && b == py && c == pz) continue;
                    if (recording != null) recording.add(BlockPos.asLong(a, b, c));
                    else if (!level.getBlockState(pos.set(a, b, c)).canBeReplaced()) return false;
                }
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- 1.0.2 defaults

    /** 1.0.2 BlockMultiblockCore.checkRequirement: the dimensions box around the core. */
    public static boolean checkDimensions(
            @Nullable Level level, int[] dimensions, BlockPos placed, Direction dir, int o) {
        BlockPos origin = placed.offset(dir.getStepX() * o, dir.getStepY() * o, dir.getStepZ() * o);
        return checkSpace(level, origin, dimensions, placed, dir);
    }

    /** 1.0.2 BlockMultiblockCore.placementDimensionBoxes: the dimensions as one preview box. */
    public static int[] dimensionBoxes(int[] d) {
        return new int[] {d[0], d[1], d[2], d[3], d[4], d[5], 0, 0, 0};
    }

    /** 1.0.2 MultiblockHandlerXR.boundingSilhouette of the bounding boxes for a facing. */
    public static @Nullable VoxelShape boundingOutline(List<AABB> bounding, Direction facing) {
        if (bounding.isEmpty()) return null;
        Direction rot = facing.getClockWise();
        VoxelShape shape = Shapes.empty();
        for (AABB aabb : bounding) {
            shape =
                    Shapes.joinUnoptimized(
                            shape,
                            Shapes.create(Facing.rotateAabb(aabb, rot).move(0.5, 0, 0.5)),
                            BooleanOp.OR);
        }
        return shape.optimize();
    }

    /** 1.0.2 BlockMultiblockCore.boundingShape: the outline's part inside one cell. */
    public static @Nullable VoxelShape boundingShape(
            @Nullable VoxelShape outline, int lx, int ly, int lz, Direction facing) {
        if (outline == null) return null;
        return Shapes.joinUnoptimized(
                        outline.move(
                                -MultiblockMaskTable.worldX(lx, ly, lz, facing),
                                -ly,
                                -MultiblockMaskTable.worldZ(lx, ly, lz, facing)),
                        Shapes.block(),
                        BooleanOp.AND)
                .optimize();
    }

    /**
     * {@code level.getBlockState(pos).canBeReplaced()} for checkRequirement code that tests a cell
     * itself; records the cell while a layout is built.
     */
    public static boolean replaceable(@Nullable Level level, BlockPos pos) {
        LongLinkedOpenHashSet recording = RECORDING.get();
        if (recording != null) {
            recording.add(pos.asLong());
            return true;
        }
        return level.getBlockState(pos).canBeReplaced();
    }

    /** A cell sound's part of the cell shape keys: its step sound's id. */
    public static String soundKey(SoundType sound) {
        return sound.getStepSound().getLocation().toString();
    }

    // ---------------------------------------------------------------- the layout

    public static MultiblockLayout build(Shape shape, int declaredCaps) {
        MultiblockMaskTable table = table(shape, declaredCaps);
        long[][] offsets = new long[4][];
        long[][] clearance = new long[4][];
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int f = facing.get2DDataValue();
            offsets[f] = footprint(shape, facing);
            clearance[f] = clearance(shape, facing, shape.variableFootprint() ? MultiblockLayout.NO_CELLS : offsets[f]);
        }
        double[] extras = shape.previewExtras();
        MultiblockLayout.Placement placement =
                new MultiblockLayout.Placement(
                        shape.dimensions().clone(),
                        shape.offset(),
                        shape.heightOffset(),
                        shape.previewBoxes().clone(),
                        extras.clone());
        return new MultiblockLayout(
                declaredCaps,
                new MultiblockMaskTable[] {table, table, table, table},
                offsets,
                clearance,
                placement,
                geometry(shape, table));
    }

    /** 1.0.2 MultiblockMaskTable.bake, plus the heat / soil / proxy cells as flags. */
    static MultiblockMaskTable table(Shape shape, int declaredCaps) {
        MultiblockMaskTable.Builder builder = MultiblockMaskTable.builder();
        List<long[]> cells = new ArrayList<>();
        shape.cells(
                BlockPos.ZERO,
                Direction.SOUTH,
                new CellVisitor() {
                    @Override
                    public void cell(BlockPos pos, int mask) {
                        cell(pos, mask, BlockMultiblockCore.ROLE_ALL);
                    }

                    @Override
                    public void cell(BlockPos pos, int mask, int roles) {
                        builder.cell(pos.getX(), pos.getY(), pos.getZ(), mask, roles);
                        cells.add(new long[] {pos.getX(), pos.getY(), pos.getZ()});
                    }
                });
        builder.cell(0, 0, 0, shape.coreFaces(), BlockMultiblockCore.ROLE_ALL);
        cells.add(new long[] {0, 0, 0});
        shape.passiveCells(
                BlockPos.ZERO,
                Direction.SOUTH,
                (pos, faces, domains) ->
                        builder.passive(pos.getX(), pos.getY(), pos.getZ(), faces, domains));
        builder.passive(0, 0, 0, shape.passiveCoreFaces(), shape.passiveCoreDomainsFor(declaredCaps));
        LongLinkedOpenHashSet flagged = new LongLinkedOpenHashSet();
        for (long[] c : cells) {
            int lx = (int) c[0], ly = (int) c[1], lz = (int) c[2];
            if (!flagged.add(BlockPos.asLong(lx, ly, lz))) continue;
            int flags = (shape.heatSource(lx, ly, lz) ? MultiblockMaskTable.FLAG_HEAT : 0)
                    | (shape.plantSoil(lx, ly, lz) ? MultiblockMaskTable.FLAG_SOIL : 0)
                    | (shape.proxyCell(lx, ly, lz) ? MultiblockMaskTable.FLAG_PROXY : 0);
            builder.flag(lx, ly, lz, flags);
        }
        return builder.build();
    }

    /** 1.0.2 MultiblockFootprint.bake: the cells but the core, unique, in visit order. */
    static long[] footprint(Shape shape, Direction facing) {
        LongLinkedOpenHashSet unique = new LongLinkedOpenHashSet();
        shape.cells(
                BlockPos.ZERO,
                facing,
                (pos, mask) -> {
                    if (!pos.equals(BlockPos.ZERO)) unique.add(pos.asLong());
                });
        return unique.toLongArray();
    }

    /**
     * The cells checkRequirement tests but the core and, for a fixed footprint, the footprint's own
     * cells (1.0.3 placementClear tests those itself; a variable footprint is tested only here).
     */
    static long[] clearance(Shape shape, Direction facing, long[] footprint) {
        int o = -shape.offset();
        // 1.0.2 setPlacedBy: core = placed + facing * o
        BlockPos placed = new BlockPos(-facing.getStepX() * o, 0, -facing.getStepZ() * o);
        LongLinkedOpenHashSet checked = new LongLinkedOpenHashSet();
        RECORDING.set(checked);
        try {
            shape.requirement(null, placed, facing, o);
        } finally {
            RECORDING.remove();
        }
        checked.remove(BlockPos.ZERO.asLong());
        for (long cell : footprint) checked.remove(cell);
        return checked.isEmpty() ? MultiblockLayout.NO_CELLS : checked.toLongArray();
    }

    // ---------------------------------------------------------------- geometry

    /** 1.0.2 MultiblockCellShapes.bake and the core cell's shapes, as 1.0.3 cell geometry. */
    static MultiblockLayout.CellGeometry geometry(Shape shape, MultiblockMaskTable table) {
        Map<String, Integer> keys = new LinkedHashMap<>();
        List<double[][]> tupleBoxes = new ArrayList<>();
        List<double @Nullable []> climbs = new ArrayList<>();
        long[][] cells = new long[4][];
        int[][] tuples = new int[4][];
        boolean shaped = false;
        String sound = shape.cellSound();
        for (Direction facing : Direction.values()) {
            if (facing.getAxis() == Direction.Axis.Y) continue;
            int f = facing.get2DDataValue();
            AABB climb = shape.climb(facing);
            List<Long> at = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            boolean[] any = {false};
            // 1.0.2 MultiblockCellShapes.bake walks the table's cells (y, z, x)
            table.forEachLocalCell(
                    (lx, ly, lz) -> {
                        VoxelShape[] tuple = new VoxelShape[4];
                        boolean some = false;
                        for (int open = 0; open <= 1; open++) {
                            for (int coll = 0; coll <= 1; coll++) {
                                VoxelShape s = shape.shapeAt(lx, ly, lz, facing, open != 0, coll != 0);
                                if (s != null) some = true;
                                tuple[open << 1 | coll] = s;
                            }
                        }
                        AABB cellClimb =
                                climb == null
                                        ? null
                                        : carriedClimb(
                                                climb,
                                                MultiblockMaskTable.worldX(lx, ly, lz, facing),
                                                ly,
                                                MultiblockMaskTable.worldZ(lx, ly, lz, facing));
                        if (!some && cellClimb == null) return;
                        any[0] |= some;
                        for (int i = 0; i < tuple.length; i++) {
                            if (tuple[i] == null) tuple[i] = Shapes.block();
                        }
                        String key = canonical(tuple, sound, cellClimb);
                        Integer id = keys.get(key);
                        if (id == null) {
                            id = keys.size();
                            keys.put(key, id);
                            double[][] boxes = new double[4][];
                            for (int v = 0; v < 4; v++)
                                boxes[v] = MultiblockLayout.CellGeometry.boxes(tuple[v]);
                            tupleBoxes.add(boxes);
                            climbs.add(cellClimb == null ? null : box(cellClimb));
                        }
                        at.add(BlockPos.asLong(lx, ly, lz));
                        ids.add(id);
                    });
            shaped |= any[0];
            cells[f] = at.stream().mapToLong(Long::longValue).toArray();
            tuples[f] = ids.stream().mapToInt(Integer::intValue).toArray();
        }
        double[][] core = new double[16][];
        double[][] silhouettes = new double[4][];
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int f = facing.get2DDataValue();
            for (int open = 0; open <= 1; open++) {
                for (int coll = 0; coll <= 1; coll++) {
                    core[f << 2 | open << 1 | coll] =
                            MultiblockLayout.CellGeometry.boxes(
                                    shape.shapeAt(0, 0, 0, facing, open != 0, coll != 0));
                }
            }
            silhouettes[f] = MultiblockLayout.CellGeometry.boxes(shape.outline(facing));
        }
        return new MultiblockLayout.CellGeometry(
                keys.keySet().toArray(String[]::new),
                tupleBoxes.toArray(double[][][]::new),
                climbs.toArray(double[][]::new),
                shaped,
                cells,
                tuples,
                core,
                silhouettes);
    }

    private static @Nullable AABB carriedClimb(AABB climb, int wx, int wy, int wz) {
        AABB reach =
                new AABB(
                        wx - ClimbBox.REACH,
                        wy,
                        wz - ClimbBox.REACH,
                        wx + 1 + ClimbBox.REACH,
                        wy + 1,
                        wz + 1 + ClimbBox.REACH);
        if (!climb.intersects(reach)) return null;
        return climb.intersect(reach).move(-wx, -wy, -wz);
    }

    private static double[] box(AABB box) {
        return new double[] {box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ};
    }

    private static String canonical(VoxelShape[] tuple, String sound, @Nullable AABB climb) {
        StringBuilder sb = new StringBuilder();
        for (VoxelShape s : tuple) {
            for (AABB b : s.toAabbs()) box(sb, b);
            sb.append('/');
        }
        sb.append('|').append(sound).append('|');
        if (climb != null) box(sb, climb);
        return sb.toString();
    }

    private static void box(StringBuilder sb, AABB box) {
        sb.append(coordinate(box.minX)).append(',')
                .append(coordinate(box.minY)).append(',')
                .append(coordinate(box.minZ)).append(',')
                .append(coordinate(box.maxX)).append(',')
                .append(coordinate(box.maxY)).append(',')
                .append(coordinate(box.maxZ)).append(';');
    }

    private static String coordinate(double value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }
}
