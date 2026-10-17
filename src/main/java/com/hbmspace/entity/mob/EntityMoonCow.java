// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.api.environment.ISuffocationImmune;
import com.hbm.items.ModItems;
import com.hbmspace.entity.MobEntitiesSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;
import com.hbmspace.backport.compat.CowCompat;

public class EntityMoonCow extends CowCompat implements ISuffocationImmune {

    public EntityMoonCow(EntityType<? extends EntityMoonCow> type, Level level) {
        super(type, level);
        moveControl = new MoonWalkControl(this);
    }

    @Override
    public @Nullable EntityMoonCow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return MobEntitiesSpace.MOON_COW.get().create(level);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return getType().getDimensions().scale(getAgeScale());
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
        return spawnReason == MobSpawnType.CHUNK_GENERATION
                || super.checkSpawnRules(level, spawnReason);
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        int looting = MobDrops.looting(level, source);
        int leather = random.nextInt(3) + random.nextInt(1 + looting);
        for (int i = 0; i < leather; i++) spawnAtLocation(new ItemStack(Items.LEATHER));
        int cheese = random.nextInt(3) + 1 + random.nextInt(1 + looting);
        for (int i = 0; i < cheese; i++)
            spawnAtLocation(new ItemStack(ModItems.CHEESE.get()));
    }
}
