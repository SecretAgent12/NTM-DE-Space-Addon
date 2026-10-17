// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import com.hbm.backport.storage.ValueInput;
import com.hbm.backport.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import com.hbmspace.backport.compat.WaterAnimalCompat;

public class EntityScuttlecrab extends WaterAnimalCompat implements IEntityEnumMulti {

    public enum Scuttlecrab {
        TROPICAL,
        CLAYINFUSED,
    }

    private static final EntityDataAccessor<Byte> TYPE =
            SynchedEntityData.defineId(EntityScuttlecrab.class, EntityDataSerializers.BYTE);

    private float crabDirection = 1.0F;
    private int switchTimer;

    public EntityScuttlecrab(EntityType<? extends EntityScuttlecrab> type, Level level) {
        super(type, level);
        if (!level.isClientSide()) {
            entityData.set(
                    TYPE,
                    (byte)
                            (random.nextInt(8) == 0
                                            ? Scuttlecrab.CLAYINFUSED
                                            : Scuttlecrab.TROPICAL)
                                    .ordinal());
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, Math.sqrt(0.07D));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new RandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, (byte) 0);
    }

    @Override
    public Scuttlecrab getEnum() {
        return Scuttlecrab.values()[entityData.get(TYPE)];
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("type", entityData.get(TYPE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(TYPE, input.getByteOr("type", entityData.get(TYPE)));
    }

    // backport: 26.x shouldTravelInFluid(fluid) = false: walks on the sea floor instead of swimming.
    // 1.21.1 has no such hook; travel() below is the crab's own movement either way.

    @Override
    public void travel(Vec3 input) {
        switchTimer--;
        if (switchTimer <= 0) {
            crabDirection *= -1.0F;
            switchTimer = 20 + random.nextInt(80);
        }
        super.travel(new Vec3(input.z * crabDirection, input.y, input.x));
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        MobDrops.dropFew(level, this, source, Items.CLAY_BALL);
    }
}
