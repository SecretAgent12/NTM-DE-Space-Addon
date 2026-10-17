// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.api.client.CustomSkies;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import com.hbm.backport.client.core.SkyRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class SpaceSkies implements CustomSkies.Renderer {

    private final SkyProviderCelestial celestial = new SkyProviderCelestial();
    private final SkyProviderOrbit orbit = new SkyProviderOrbit();
    private final Map<ResourceKey<Level>, SkyProviderCelestial> bodies =
            Map.of(
                    SpaceDimensions.LAYTHE,
                    new SkyProviderLaytheSunset(),
                    SpaceDimensions.THATMO,
                    new SkyProviderThatmo());
    private @Nullable SkyProviderCelestial active;
    private @Nullable SkyDraws draws;
    private Matrix4f viewRotation = new Matrix4f();

    public static void register() {
        CustomSkies.register(new SpaceSkies());
        CelestialClouds.register();
        CelestialWeatherTint.register();
        WeatherParticleTint.register();
    }

    @Override
    public boolean extract(
            ClientLevel level, float partialTicks, Camera camera, SkyRenderState state) {
        active = null;
        // backport: 26.x state.skybox == OVERWORLD is the core's overworldSky (SkyType.NORMAL)
        if (!state.overworldSky) return false;

        SkyProviderCelestial sky;
        if (WorldProviderCelestial.of(level) != null)
            sky = bodies.getOrDefault(level.dimension(), celestial);
        else if (CelestialBody.inOrbit(level)) sky = orbit;
        else return false;

        active = sky;
        viewRotation = SkyFrame.viewRotation(camera);
        sky.extract(level, partialTicks, camera, state);
        return true;
    }

    // backport: 1.21.1 CustomSkies.Renderer.render gets the LevelRenderer, with fog set up and the
    // frame's projection set; SkyDraws and VanillaSky draw on the model-view matrix, as 26.x's sky pass
    // has it, so the view rotation (SkyFrame) goes on it here
    @Override
    public void render(LevelRenderer levelRenderer, SkyRenderState state) {
        if (draws == null) draws = new SkyDraws();
        org.joml.Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(viewRotation);
        RenderSystem.applyModelViewMatrix();
        try {
            Objects.requireNonNull(active).render(new VanillaSky(levelRenderer), state, draws);
            draws.endFrame();
        } finally {
            stack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            // backport: 1.21.1 renderSky leaves this state behind and the terrain after it relies on
            // it (RenderType write masks only switch depth writes off, never on): without depthMask(true)
            // the world is drawn without depth writes and caves show through the ground
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
        }
    }

    @Override
    public void close() {
        if (draws != null) {
            draws.close();
            draws = null;
        }
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;

        for (CelestialBody body : CelestialBody.getAllBodies()) {
            HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits =
                    SolarSystemWorldSavedData.getClientTraits(body.name);
            if (traits != null) {
                for (CelestialBodyTrait trait : new ArrayList<>(traits.values())) {
                    trait.update(true, body);
                    if (trait instanceof CBT_Invasion invasion
                            && invasion.isInvading
                            && mc.level.getRandom().nextInt(Math.max(1, 5 - invasion.wave)) == 0) {
                        Meteors.addMeteor();
                    }
                }
            }
        }

        Meteors.clientTick();
        DunaStormClient.tick(mc);
        EveFlashClient.tick(mc);
    }
}
