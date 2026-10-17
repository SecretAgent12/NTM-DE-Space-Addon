// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.hbm.client.render.flywheel.MobObjVisual;
import com.hbm.client.render.flywheel.ObjEntityVisual;
import com.hbm.render.loader.HFRWavefrontObject;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import com.hbm.backport.client.flywheel.CutoutShaders;
import com.hbm.backport.client.flywheel.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public record ObjMobRig<T extends LivingEntity>(
        Supplier<HFRWavefrontObject> mesh,
        List<ResourceLocation> skins,
        ToIntFunction<T> skin,
        Map<String, ResourceLocation> groupSkins,
        ObjEntityVisual.Pose<T> pose,
        List<MobObjVisual.Part<T>> parts,
        float shadowRadius) {

    MobObjVisual.Spec<T> spec(int skinIndex, Predicate<T> instanced) {
        List<MobObjVisual.Part<T>> sheeted = new ArrayList<>(parts.size());
        for (MobObjVisual.Part<T> part : parts) {
            ResourceLocation own = groupSkins.get(part.name());
            sheeted.add(own == null ? part : part.material(() -> material(own)));
        }
        ResourceLocation texture = skins.get(skinIndex);
        return new MobObjVisual.Spec<>(
                mesh,
                () -> material(texture),
                List.copyOf(sheeted),
                pose,
                shadowRadius,
                1.0F,
                instanced);
    }

    private static Material material(ResourceLocation texture) {
        return SimpleMaterial.builderOf(Materials.CUTOUT)
                .texture(texture)
                .mipmap(false)
                .cutout(CutoutShaders.ONE_TENTH)
                .cardinalLightingMode(CardinalLightingMode.ENTITY)
                .backfaceCulling(false)
                .build();
    }
}
