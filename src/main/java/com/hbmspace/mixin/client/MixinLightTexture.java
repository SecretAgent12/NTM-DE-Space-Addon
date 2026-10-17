// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.mixin.client;

import com.hbmspace.client.sky.CelestialLightmapTint;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** backport: Ike / Laythe light map tint on the 1.21.1 CPU light map (see CelestialLightmapTint). */
@Mixin(LightTexture.class)
public abstract class MixinLightTexture {

    @Shadow @Final private NativeImage lightPixels;
    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "updateLightTexture",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V"))
    private void hbmspace$celestialTint(float partialTicks, CallbackInfo ci) {
        if (minecraft.level != null) CelestialLightmapTint.apply(minecraft.level, lightPixels);
    }
}
