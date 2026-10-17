// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** backport: 26.x LivingEntity.isJumping(); on 1.21.1 the flag is the protected field. */
@Mixin(LivingEntity.class)
public interface AccessorLivingEntity {

    @Accessor("jumping")
    boolean hbmspace$isJumping();
}
