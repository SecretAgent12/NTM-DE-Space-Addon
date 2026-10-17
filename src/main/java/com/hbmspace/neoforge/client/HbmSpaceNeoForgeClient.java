// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.neoforge.client;

import com.hbm.client.ClientRegistry;
import com.hbmspace.HbmSpace;
import com.hbmspace.client.CelestialOreTooltip;
import com.hbmspace.client.SpaceClient;
import com.hbmspace.client.SpaceEffectHud;
import com.hbmspace.client.SpaceOxygenHud;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.hbm.backport.client.particle.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import com.hbm.backport.client.gui.render.pip.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import com.hbm.backport.client.gui.GuiGraphicsExtractor;

@Mod(value = HbmSpace.MOD_ID, dist = Dist.CLIENT)
public final class HbmSpaceNeoForgeClient {
    public HbmSpaceNeoForgeClient(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent event) -> SpaceClient.init());
        // backport: the 1.21.1 programs of the sky and planet pipelines (26.x builds them per pipeline)
        modBus.addListener(com.hbmspace.client.sky.SpaceShaders::onRegisterShaders);
        // backport: 1.21.1 reload listeners carry no id and no dependencies; the core's gun client
        // setup runs first because the core registers its listener first (the addon loads after it)
        modBus.addListener(
                (net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent event) ->
                        event.registerReloadListener(
                                (ResourceManagerReloadListener)
                                        resourceManager -> SpaceClient.setupGunClient()));
        modBus.addListener(
                (FMLClientSetupEvent event) ->
                        event.enqueueWork(SpaceClient::registerFlywheelVisualizers));
        modBus.addListener(
                (RegisterMenuScreensEvent event) ->
                        SpaceClient.registerMenuScreens(event::register));
        modBus.addListener(
                (EntityRenderersEvent.RegisterRenderers event) ->
                        SpaceClient.registerBlockEntityRenderers(
                                event::registerBlockEntityRenderer));
        modBus.addListener(
                (EntityRenderersEvent.RegisterRenderers event) ->
                        SpaceClient.registerEntityRenderers(event::registerEntityRenderer));
        // backport: 26.x block tint sources on the 1.21.1 colour event, as the core does
        modBus.addListener(
                (RegisterColorHandlersEvent.Block event) ->
                        SpaceClient.registerBlockTintSources(
                                (sources, blocks) ->
                                        com.hbm.backport.client.misc.BlockTints.register(
                                                event, sources, blocks)));
        // backport: 1.21.1 has no pipeline registry (26.x RegisterRenderPipelinesEvent): the core's
        // render type shim builds the item/block pipelines itself, and the sky pipelines' shader
        // programs are registered by SpaceShaders (above).
        modBus.addListener(
                (RegisterPictureInPictureRenderersEvent event) ->
                        SpaceClient.registerPictureInPictureRenderers(event::register));
        modBus.addListener(
                (RegisterParticleProvidersEvent event) ->
                        SpaceClient.registerParticleProviders(event::registerSpecial));
        // backport: 26.x sky / fog / cloud attributes of the dimensions -> 1.21.1 special effects
        modBus.addListener(
                (net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent event) ->
                        event.register(
                                com.hbmspace.client.sky.SpaceDimensionEffects.ID,
                                new com.hbmspace.client.sky.SpaceDimensionEffects()));
        modBus.addListener(
                (RegisterParticleGroupsEvent event) ->
                        SpaceClient.registerParticleGroups(event::register));
        modBus.addListener(
                (RegisterGuiLayersEvent event) -> {
                    event.registerAbove(
                            VanillaGuiLayers.PLAYER_HEALTH,
                            SpaceEffectHud.TAINT_BAR,
                            (graphics, delta) -> {
                                Minecraft mc = Minecraft.getInstance();
                                if (!net.minecraft.client.Minecraft.getInstance().options.hideGui && mc.gameMode.canHurtPlayer())
                                    SpaceEffectHud.taintBar(GuiGraphicsExtractor.of(graphics), delta);
                            });
                    event.registerBelow(
                            VanillaGuiLayers.CHAT,
                            SpaceEffectHud.FLASHBANG,
                            (graphics, delta) -> {
                                if (!net.minecraft.client.Minecraft.getInstance().options.hideGui)
                                    SpaceEffectHud.flashbang(GuiGraphicsExtractor.of(graphics), delta);
                            });
                });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> SpaceClient.clientTick());
        CelestialFogEvents.register();
        // backport: 26.x adds 8 to the rider's camera_distance attribute while in a rocket
        // (EntityRideableRocket.addPassenger); 1.21.1 has no such attribute, the third-person
        // distance (4 x the rider's scale) is set here instead: 4 + 8, times the scale, as 26.x.
        NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent event) -> {
                    if (event.getCamera().getEntity().getVehicle()
                            instanceof com.hbmspace.entity.missile.EntityRideableRocket) {
                        event.setDistance(
                                event.getDistance()
                                        + com.hbmspace.entity.missile.EntityRideableRocket
                                                        .CAMERA_DISTANCE_BONUS
                                                * event.getEntityScalingFactor());
                    }
                });
        NeoForge.EVENT_BUS.addListener(
                (ClientPlayerNetworkEvent.LoggingOut event) -> SpaceClient.disconnected());
        NeoForge.EVENT_BUS.addListener(
                (ItemTooltipEvent event) ->
                        CelestialOreTooltip.append(event.getItemStack(), event.getToolTip()));
        NeoForge.EVENT_BUS.addListener(
                (RenderGuiLayerEvent.Pre event) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (!event.getName().equals(VanillaGuiLayers.AIR_LEVEL)
                            || mc.player == null
                            || !SpaceOxygenHud.replacesAir(mc.player)) {
                        return;
                    }
                    event.setCanceled(true);
                    var graphics = event.getGuiGraphics();
                    var hud = mc.gui;
                    hud.rightHeight +=
                            SpaceOxygenHud.render(
                                    GuiGraphicsExtractor.of(graphics),
                                    mc.player,
                                    graphics.guiWidth() / 2 + 91,
                                    graphics.guiHeight() - hud.rightHeight);
                });
    }
}
