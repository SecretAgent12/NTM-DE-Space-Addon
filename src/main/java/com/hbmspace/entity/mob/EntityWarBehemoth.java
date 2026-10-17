// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.api.environment.ISuffocationImmune;
import com.hbm.sound.ModSounds;
import com.hbmspace.entity.mob.ai.BehemothGunGoal;
import com.hbmspace.entity.mob.ai.StepTowardsTargetGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;
import com.hbm.backport.compat.MonsterCompat;

public class EntityWarBehemoth extends MonsterCompat implements ISuffocationImmune {
    private static final EntityDataAccessor<Integer> TARGET =
            SynchedEntityData.defineId(EntityWarBehemoth.class, EntityDataSerializers.INT);

    private float aimPitch;
    private float aimPitchO;

    public EntityWarBehemoth(EntityType<? extends EntityWarBehemoth> type, Level level) {
        super(type, level);
        setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 50.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.STEP_HEIGHT, 5.0D);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(
                1, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
        targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Mob.class,
                        0,
                        true,
                        true,
                        target -> !(target instanceof EntityWarBehemoth)));
        goalSelector.addGoal(3, new StepTowardsTargetGoal(this, 50.0D, 0.3D, 50, 10, 0.6D));
        goalSelector.addGoal(4, new BehemothGunGoal(this, 3, 35, 30));
        targetSelector.addGoal(5, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET, 0);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        entityData.set(TARGET, target != null ? target.getId() : 0);
    }

    @Override
    public void tick() {
        if (level().isClientSide()) aimBodyYaw();
        super.tick();
        if (level().isClientSide()) {
            aimBody();
        } else {
            setDeltaMovement(getDeltaMovement().add(0.0D, -0.1D, 0.0D));
        }
    }

    // backport-fix: BF-089 a 1.21.1 LivingEntity smooths towards the server through its own lerp
    // fields, not a 26.x InterpolationHandler (getInterpolation() is null here): pull the lerp
    // target yaw the way MixinInterpolationHandler pulls the 26.x one.
    private void aimBodyYaw() {
        if (lerpSteps <= 0
                || !(level().getEntity(entityData.get(TARGET)) instanceof LivingEntity target))
            return;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double yaw = Math.atan2(dz, dx) * (180D / Math.PI) - 90D;
        double delta = (yaw - lerpYRot) % 360D;
        if (delta >= 180D) delta -= 360D;
        if (delta < -180D) delta += 360D;
        lerpYRot += delta * 0.1D;
    }

    private void aimBody() {
        aimPitchO = aimPitch;
        if (!(level().getEntity(entityData.get(TARGET)) instanceof LivingEntity target)) return;
        double dx = target.getX() - getX();
        double dy = target.getEyeY() - getEyeY();
        double dz = target.getZ() - getZ();
        double pitch = Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * -Mth.RAD_TO_DEG;
        aimPitch += (float) (Mth.wrapDegrees(pitch - aimPitch) * 0.1D);
    }

    public float aimPitch(float partialTick) {
        return Mth.lerp(partialTick, aimPitchO, aimPitch);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ThrownEgg && random.nextInt(10) == 0) {
            xpReward = 0;
            setHealth(0.0F);
            return true;
        }
        return super.hurtServer(level, source, EntityTankbot.plated(source, amount));
    }

    @Override
    protected float getFlyingSpeed() {
        return 0.0F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_CYBERCRAB.get();
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
        return spawnReason == MobSpawnType.CHUNK_GENERATION
                || super.checkSpawnRules(level, spawnReason);
    }
}
