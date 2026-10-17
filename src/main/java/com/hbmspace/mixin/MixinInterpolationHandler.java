// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.backport.InterpolationHandler;
import com.hbmspace.entity.mob.BehemothYawTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// backport-fix: BF-089 26.x keeps the target yaw in a private InterpolationData record and this
// mixin reached it with VarHandles; DE's InterpolationHandler keeps it in its own yRot field, so
// the yaw is shadowed directly (no reflection, nothing that could name the mixin class).
@Mixin(InterpolationHandler.class)
public abstract class MixinInterpolationHandler implements BehemothYawTarget {
    @Shadow
    public abstract boolean hasActiveInterpolation();

    @Shadow private float yRot;

    @Override
    public void hbmspace$pullYaw(double targetYaw) {
        if (!hasActiveInterpolation()) return;
        double delta = (targetYaw - yRot) % 360D;
        if (delta >= 180D) delta -= 360D;
        if (delta < -180D) delta += 360D;
        yRot = (float) (yRot + delta * 0.1D);
    }
}
