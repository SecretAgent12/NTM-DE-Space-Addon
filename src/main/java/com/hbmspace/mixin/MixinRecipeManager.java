// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.google.gson.JsonElement;
import com.hbmspace.inventory.recipes.BedrockFragmentRecipes;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager extends ContextAwareReloadListener {
    // backport-fix: include the Space fragment recipes when the server loads crafting recipes.
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"),
            require = 1)
    private void hbmspace$addFragmentRecipes(
            Map<ResourceLocation, JsonElement> recipes,
            ResourceManager manager,
            ProfilerFiller profiler,
            CallbackInfo ci) {
        BedrockFragmentRecipes.add(recipes, getContext());
    }
}
