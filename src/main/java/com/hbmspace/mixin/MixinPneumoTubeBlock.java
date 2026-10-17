// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.network.pneumatic.BlockEntityPneumoTube;
import com.hbm.blocks.network.pneumatic.PneumoTubeBlock;
import com.hbmspace.handler.PneumoIdentifierSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import com.hbm.backport.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.world.level.material.Fluid;
import com.hbm.backport.compat.BlockCompat;

@Mixin(PneumoTubeBlock.class)
public abstract class MixinPneumoTubeBlock extends BlockCompat {
    protected MixinPneumoTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult backport$useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        return PneumoIdentifierSelection.handle(stack, level, pos, player)
                ? InteractionResult.SUCCESS
                : super.backport$useItemOn(stack, state, level, pos, player, hand, hit);
    }

    // backport: 1.0.3 resolves a compressor's air faces in a private canConnectToAir(level,
    // neighbour, side, state) that asks for air; NTM: Space's compressors work with the gas in
    // their tank, so that is the fluid asked for (the tube is the neighbour's neighbour on side)
    @ModifyArg(
            method =
                    "canConnectToAir(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;"
                            + "Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/uninos/graph/PhysicalGraph;fluidContact(Lnet/minecraft/world/level/LevelReader;"
                                            + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;"
                                            + "Lnet/minecraft/world/level/material/Fluid;)Z"),
            index = 3)
    private static Fluid hbmspace$typedNozzle(
            LevelReader level, BlockPos pos, Direction side, Fluid fluid) {
        if (level.getBlockEntity(pos.relative(side)) instanceof BlockEntityPneumoTube tube) {
            Fluid gas = tube.compair.getFluid();
            if (gas != null) return gas;
        }
        return fluid;
    }
}
