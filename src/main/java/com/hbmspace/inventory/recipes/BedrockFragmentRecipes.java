// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.special.Autogen;
import com.hbmspace.HbmSpace;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

import static com.hbm.inventory.material.MaterialShapes.*;

public final class BedrockFragmentRecipes {
    private static final List<MaterialShapes> OUTPUT_FORMS = List.of(DUST, GEM, CRYSTAL, INGOT, BILLET, ORE);

    private BedrockFragmentRecipes() {}

    public static void add(Map<ResourceLocation, JsonElement> recipes, ICondition.IContext context) {
        for (NTMMaterial material : Mats.orderedList) {
            var fragment = Autogen.handle(FRAGMENT, material);
            if (fragment == null || !fragment.id().getNamespace().equals(HbmSpace.MOD_ID)) continue;

            var id = ResourceLocation.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "fragment_packing/" + material.tagPath);
            if (recipes.containsKey(id) || context.getTag(material.tag(FRAGMENT)).isEmpty())
                continue;

            for (MaterialShapes form : OUTPUT_FORMS) {
                var outputs = context.getTag(material.tag(form));
                if (outputs.isEmpty()) continue;
                var result = BuiltInRegistries.ITEM.getKey(outputs.iterator().next().value());
                recipes.put(id, recipe(material, form, result));
                break;
            }
        }
    }

    private static JsonObject recipe(NTMMaterial material, MaterialShapes form, ResourceLocation output) {
        JsonObject key = new JsonObject();
        key.addProperty("#", "#" + material.tag(FRAGMENT).location());
        JsonArray pattern = new JsonArray();
        pattern.add("###");
        pattern.add("###");
        if (form != BILLET) pattern.add("###");
        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());

        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "hbm:crafting_shaped");
        recipe.add("key", key);
        recipe.add("pattern", pattern);
        recipe.add("result", result);
        return recipe;
    }
}
