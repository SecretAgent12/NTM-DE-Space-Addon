// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbmspace.entity.MobEntitiesSpace;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import com.hbm.backport.storage.ValueInput;
import com.hbm.backport.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import com.hbm.backport.compat.SquidCompat;

public class EntityDepthSquid extends SquidCompat implements IEntityEnumMulti {

    public enum DepthSquid {
        AQUA,
        BLACK,
        ORANGE,
        OURPLE,
        RED,
        SILVER,
        VICIOUS,
    }

    private static final EntityDataAccessor<Byte> TYPE =
            SynchedEntityData.defineId(EntityDepthSquid.class, EntityDataSerializers.BYTE);

    public EntityDepthSquid(EntityType<? extends EntityDepthSquid> type, Level level) {
        super(type, level);
        if (!level.isClientSide())
            entityData.set(TYPE, (byte) random.nextInt(DepthSquid.values().length));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, (byte) 0);
    }

    @Override
    public DepthSquid getEnum() {
        return DepthSquid.values()[entityData.get(TYPE)];
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

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
        return getY() > 45.0D && getY() < 63.0D && super.checkSpawnRules(level, spawnReason);
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        int sacs = random.nextInt(3 + MobDrops.looting(level, source)) + 1;
        for (int i = 0; i < sacs; i++) spawnAtLocation(new ItemStack(Items.INK_SAC));
    }

    // backport: 26.x squids are ageable water creatures that breed; a 1.21.1 squid is a plain
    // WaterAnimal, so there is no offspring (getBreedOffspring) to make

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnReason,
            @Nullable SpawnGroupData groupData) {
        return super.finalizeSpawn(
                level,
                difficulty,
                spawnReason,
                groupData != null ? groupData : new AgeableMob.AgeableMobGroupData(false));
    }
}
