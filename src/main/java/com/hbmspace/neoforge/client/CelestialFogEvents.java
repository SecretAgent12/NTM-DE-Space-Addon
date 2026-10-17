// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.neoforge.client;

import com.hbm.api.client.ShaderPacks;
import com.hbmspace.client.sky.CelestialFogTint;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The thick air of the planets (Eve, Duna's dust storms, Laythe's water): 1.7.10 switched the fog to
 * GL_EXP with {@link CelestialFogTint#density} (ModEventHandlerRenderer.thickenFog, ahead of soot and
 * of the water/lava fog, after blindness). 26.x patches an exponential term into the terrain shaders.
 *
 * <p>backport: 1.21.1 has only linear fog, so the exponential fog is stood in for by a linear one
 * from the eye out to where exp(-density * d) is 95 % fog (3 / density), radial like 1.7.10's
 * NV_fog_distance. In clear air it never reaches past the vanilla render distance edge. Skipped under
 * a shader pack, as in 26.x.
 */
public final class CelestialFogEvents {

    private static float vanillaFar;

    private CelestialFogEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(
                EventPriority.HIGHEST, true, ViewportEvent.RenderFog.class, event ->
                        vanillaFar = event.getFarPlaneDistance());
        NeoForge.EVENT_BUS.addListener(
                EventPriority.LOWEST, true, ViewportEvent.RenderFog.class,
                CelestialFogEvents::onRenderFog);
    }

    private static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || ShaderPacks.inUse()) return;
        if (event.getCamera().getEntity() instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS)))
            return;
        float density = CelestialFogTint.density(level, event.getCamera());
        if (density <= 0F) return;
        float end = 3F / density;
        if (event.getType() == FogType.NONE) end = Math.min(end, vanillaFar);
        event.setNearPlaneDistance(0F);
        event.setFarPlaneDistance(end);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true); // NeoForge applies the new distances only when cancelled
    }
}
