// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.client.model.Meshes;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.missile.EntityCombatDropPod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.backport.client.core.SubmitNodeCollector;
import com.hbm.backport.client.core.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.hbm.backport.client.core.EntityRenderState;
import com.hbm.backport.client.rendertype.RenderTypes;
import com.hbm.backport.client.core.CameraRenderState;
import net.minecraft.resources.ResourceLocation;

public class RenderCombatDropPod
        extends EntityRenderer<EntityCombatDropPod, RenderCombatDropPod.State> {

    static final ResourceLocation MESH =
            ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, "models/combat_drop_pod.obj");
    private static final ResourceLocation[] SKINS = {skin("white"), skin("red"), skin("yellow")};

    private final HFRWavefrontObject mesh = Meshes.load(MESH);

    public RenderCombatDropPod(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0F;
    }

    @Override
    protected boolean affectedByCulling(EntityCombatDropPod entity) {
        return false;
    }

    static ResourceLocation skin(String color) {
        return ResourceLocation.fromNamespaceAndPath(
                HbmSpace.MOD_ID, "textures/models/bombs/combat_drop_pod." + color + ".png");
    }

    static int skinIndex(int color) {
        return color == 1 || color == 2 ? color : 0;
    }

    static ResourceLocation skinTexture(int skinIndex) {
        return SKINS[skinIndex];
    }

    static int skinCount() {
        return SKINS.length;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityCombatDropPod entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.color = entity.getColor();
    }

    @Override
    public void submit(
            State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        collector.submitCustomGeometry(
                pose,
                RenderTypes.entityCutoutCull(SKINS[skinIndex(state.color)]),
                (p, buffer) -> mesh.render(p, buffer, light, -1));
        super.submit(state, pose, collector, camera);
    }

    public static final class State extends EntityRenderState {
        int color;
    }
}
