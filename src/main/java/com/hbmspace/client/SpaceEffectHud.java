// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbm.render.util.RenderScreenOverlay;
import com.hbmspace.HbmSpace;
import com.hbmspace.potion.HbmPotionSpace;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import com.hbm.backport.client.gui.GuiGraphicsExtractor;
import com.hbm.backport.client.rendertype.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;

public final class SpaceEffectHud {

    public static final ResourceLocation TAINT_BAR =
            ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, "taint_bar");
    public static final ResourceLocation FLASHBANG =
            ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, "flashbang");
    private static final ResourceLocation FLASHBANG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, "textures/misc/overlay_flashbang.png");

    private SpaceEffectHud() {}

    public static void taintBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!active(HbmPotionSpace.NITAN.get())) return;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                RenderScreenOverlay.misc,
                graphics.guiWidth() / 2 - 92,
                graphics.guiHeight() - 41,
                146,
                18,
                81,
                12,
                256,
                256);
    }

    public static void flashbang(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!active(HbmPotionSpace.FLASHBANG.get())) return;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                FLASHBANG_TEXTURE,
                0,
                0,
                0,
                0,
                graphics.guiWidth(),
                graphics.guiHeight(),
                graphics.guiWidth(),
                graphics.guiHeight());
    }

    private static boolean active(MobEffect effect) {
        Player player = Minecraft.getInstance().player;
        return player != null
                && player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
    }
}
