// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbm.blocks.ModBlockEntities;
import com.hbm.blocks.machine.BlockCargoElevator;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.render.MachinePlacementOutline;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.blocks.bomb.LaunchPadRocket;
import com.hbmspace.blocks.machine.MachineRocketAssembly;
import com.hbmspace.handler.SpaceBlockItemPlacement;
import com.hbmspace.handler.SpaceMultiblockPlacement;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.hbm.backport.Nbt;
import com.hbmspace.blocks.multiblock.SpaceMultiblockCore;

public final class SpaceMultiblockItemPreview {

    private SpaceMultiblockItemPreview() {}

    public static MachinePlacementOutline.State extract(
            Level level,
            BlockItem item,
            BlockMultiblockCore block,
            BlockPlaceContext context,
            Player player) {
        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        Direction direction = SpaceMultiblockPlacement.direction(block, face, player.getYRot());
        BlockPos core = SpaceMultiblockPlacement.core(block, clicked, face, player.getYRot());
        BlockState state = block.getStateForPlacement(context);
        boolean placeable =
                context.canPlace()
                        && state != null
                        && ((SpaceBlockItemPlacement) item).hbmspace$canPlace(context, state)
                        // backport: 1.0.3 checks the space around the core (placementClear)
                        && block.placementClear(level, core, direction);
        VoxelShape shape = block.placementOutline(direction);
        var data = player.getMainHandItem().get(DataComponents.BLOCK_ENTITY_DATA);
        // backport: 1.21.1 block entity data is a CustomData whose "id" names the type
        BlockEntityType<?> type =
                data == null
                        ? null
                        : net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.get(
                                net.minecraft.resources.ResourceLocation.tryParse(
                                        data.getUnsafe().getString("id")));
        if (data == null || type == null) { // backport: 26.x BlockEntityType.onlyOpCanSetNbt() does not exist (preview only)
            return new MachinePlacementOutline.State(
                    core, shape, block.placementExtraOutlines(direction), placeable);
        }
        CompoundTag tag = data.getUnsafe();
        LongOpenHashSet added = new LongOpenHashSet();
        SpaceMultiblockCore.CellVisitor cells = (pos, mask) -> added.add(pos.asLong());
        if (block instanceof BlockCargoElevator && type == ModBlockEntities.CARGO_ELEVATOR.get()) {
            int height = Nbt.getIntOr(tag, "height", 0);
            shape = Shapes.box(-1D, 0D, -1D, 2D, (double) height + 1D, 2D);
            if (height < 0 || (long) core.getY() + height > (level.getMaxBuildHeight() - 1)) {
                return new MachinePlacementOutline.State(
                        core, shape, block.placementExtraOutlines(direction), false);
            }
            for (int y = 1; y <= height; y++) {
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++) {
                        added.add(
                                BlockPos.asLong(core.getX() + x, core.getY() + y, core.getZ() + z));
                    }
            }
        } else if (block instanceof MachineRocketAssembly
                && type == RocketryBlocksSpace.ROCKET_ASSEMBLY.get()) {
            int spire = Nbt.getIntOr(tag, "spire", 0);
            int[] platforms = Nbt.getIntArray(tag, "platforms").orElse(new int[0]);
            if ((long) core.getY() + spire > (level.getMaxBuildHeight() - 1)) {
                return new MachinePlacementOutline.State(
                        core, shape, block.placementExtraOutlines(direction), false);
            }
            for (int y : platforms) {
                if ((long) core.getY() + y < level.getMinBuildHeight()
                        || (long) core.getY() + y > (level.getMaxBuildHeight() - 1)) {
                    return new MachinePlacementOutline.State(
                            core, shape, block.placementExtraOutlines(direction), false);
                }
            }
            MachineRocketAssembly.visitRaised(core, spire, platforms, cells);
        } else if (block instanceof LaunchPadRocket
                && type == RocketryBlocksSpace.LAUNCH_PAD_ROCKET_BE.get()) {
            LaunchPadRocket.visitTower(
                    core, direction, Nbt.getIntOr(tag, "tower", 0), (level.getMaxBuildHeight() - 1), cells);
        }
        for (long packed : added) {
            BlockPos world = BlockPos.of(packed);
            if (level.isOutsideBuildHeight(world) || !level.getBlockState(world).canBeReplaced())
                placeable = false;
            shape =
                    Shapes.or(
                            shape,
                            Shapes.block()
                                    .move(
                                            world.getX() - core.getX(),
                                            world.getY() - core.getY(),
                                            world.getZ() - core.getZ()));
        }
        return new MachinePlacementOutline.State(
                core, shape, block.placementExtraOutlines(direction), placeable);
    }
}
