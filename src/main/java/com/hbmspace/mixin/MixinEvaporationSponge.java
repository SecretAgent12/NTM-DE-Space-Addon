// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.mixin;

import com.hbmspace.dim.CelestialEnvironment;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.world.level.block.WetSpongeBlock;

// backport: 26.x water_evaporates environment attribute (low-pressure planets, orbit; not in a
// pressurized room). 1.21.1 asks DimensionType.ultraWarm() instead: a placed wet sponge dries.
@Mixin(WetSpongeBlock.class)
public abstract class MixinEvaporationSponge {
    @ModifyExpressionValue(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;ultraWarm()Z"))
    private boolean hbmspace$evaporates0(
            boolean ultraWarm,
            @Local(argsOnly = true) Level level,
            @Local(argsOnly = true) BlockPos pos) {
        return ultraWarm || CelestialEnvironment.waterEvaporatesServer(level, pos);
    }
}
