// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.AudioLoop;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Amat;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.particle.CoolingTowerParticleOptions;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.AudioWrapper;
import com.hbm.sound.ModSounds;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.GasExchange;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.hbm.backport.storage.ValueInput;
import com.hbm.backport.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.hbm.inventory.fluid.tank.FluidTankAccess;

public class BlockEntityAtmosphericEmitter extends BlockEntityMachineBase
        implements FluidTankAccess, IEnergyHandlerMK2, FluidTankEndpoint, AudioLoop, SyncUnitSchema {

    public static final long MAX_POWER = 250_000L;
    public static final long POWER_PER_MB = 10L;
    private static final float FAN_SPEED = 45F;

    @SyncField(units = 1L << 1)
    public final FluidTankNTM tank = new FluidTankNTM(50_000);

    private final FluidTankNTM[] tanks = {tank};

    @SyncField(units = 1L << 0)
    public long power;

    public float fan;
    public float prevFan;

    public BlockEntityAtmosphericEmitter(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.ATMOSPHERIC_EMITTER.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        networkPackNT(50);

        if (tank.getFill() <= 0) return;

        int toSpill = spill();
        if (power < toSpill * POWER_PER_MB) return;

        if (NTMFluidProperties.hasTrait(tank.getTankType(), FT_Amat.class)) {
            level.explode(
                    null,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5,
                    10F,
                    true,
                    Level.ExplosionInteraction.BLOCK);
            return;
        }

        tank.setFill(tank.getFill() - toSpill);
        FT_Polluting polluting =
                NTMFluidProperties.getTrait(tank.getTankType(), FT_Polluting.class);
        if (polluting != null)
            polluting.onFluidRelease(level, worldPosition, tank, toSpill, FluidReleaseType.SPILL);
        GasExchange.release((ServerLevel) level, tank.getTankType(), toSpill);
        power -= toSpill * POWER_PER_MB;
        setChanged();
    }

    private int spill() {
        return Math.max(tank.getFill() / 2, 1);
    }

    public boolean isRunning() {
        return power >= spill() * POWER_PER_MB;
    }

    @Override
    public void tickClient() {
        prevFan = fan;
        boolean running = isRunning();

        if (running) {
            fan += FAN_SPEED;
            if (fan >= 360F) {
                fan -= 360F;
                prevFan -= 360F;
            }

            if (tank.getFill() > 0
                    && level.getNearestPlayer(
                                    worldPosition.getX(),
                                    worldPosition.getY(),
                                    worldPosition.getZ(),
                                    100.0D,
                                    false)
                            != null) {
                Direction dir = BlockMultiblockCore.coreFacing(getBlockState());
                Direction rot = dir.getClockWise();
                CoolingTowerParticleOptions plume =
                        new CoolingTowerParticleOptions.Builder()
                                .setLift(0.5F)
                                .setBaseScale(0.375F)
                                .setMaxScale(3F)
                                .setLife(100 + level.getRandom().nextInt(50))
                                .setColor(BlockEntityAirPump.color(tank.getFluid()))
                                .build();
                level.addParticle(plume, true, worldPosition.getX() + 0.5 - dir.getStepX() * 0.5 + rot.getStepX() * 0.5, worldPosition.getY() + 2.5, worldPosition.getZ() + 0.5 - dir.getStepZ() * 0.5 + rot.getStepZ() * 0.5, 0, 0.2, 0);
            }
        }

        audioLoop(running, 0.25F);
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioSystem.getLoopedSound(
                ModSounds.MOTOR_LOOP.get(),
                SoundSource.BLOCKS,
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                0.25F,
                10F,
                1.0F,
                20);
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        power = p;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return tanks;
    }

    @Override
    public int tankCount() {
        return tanks.length;
    }

    @Override
    public FluidTankNTM tank(int index) {
        return tanks[index];
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("t").ifPresent(tank::deserialize);
        power = input.getLongOr("power", 0L);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        tank.serialize(output.child("t"));
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> tank.packetSerialize(output);
            default -> super.writeSyncUnit(unit, output);
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> power = input.readLong();
            case 1 -> tank.packetDeserialize(input);
            default -> super.readSyncUnit(unit, input);
        }
    }


    // backport: woven trait AudioLoop
    private AudioWrapper hbm$audio;

    public void audioLoop(boolean running, float volume) {
        hbm$audio = AudioLoop.loop(this, hbm$audio, running, volume, 0F, false);
    }

    public void audioLoop(boolean running, float volume, float pitch) {
        hbm$audio = AudioLoop.loop(this, hbm$audio, running, volume, pitch, true);
    }

    private void hbm$audioRemove() {
        audioLoop(false, 0F);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        hbm$audioRemove();
    }
}
