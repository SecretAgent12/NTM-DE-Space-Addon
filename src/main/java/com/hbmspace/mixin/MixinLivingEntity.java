// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.dim.SpaceGravity;
import com.hbmspace.extprop.SpacePlayerProps;
import com.hbmspace.potion.HbmPotionSpace;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    // backport: 1.21.1 reports removed effects one at a time (26.x: onEffectsRemoved(Collection))
    @Inject(method = "onEffectRemoved", at = @At("TAIL"))
    private void hbmspace$clearNitanBuffer(MobEffectInstance effect, CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || player.level().isClientSide()) return;
        if (effect.getEffect().value() == HbmPotionSpace.NITAN.get()) {
            SpacePlayerProps.clearNitanHealth(player);
        }
    }

    // backport: 1.21.1 causeFallDamage(float distance, float multiplier, source) (26.x: double)
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float hbmspace$fallDistance(float fallDistance) {
        return (float) SpaceGravity.scaleFall((LivingEntity) (Object) this, fallDistance);
    }
}
