// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.mixin.client;

import com.hbm.backport.client.itemmodel.ClientItems;
import com.hbmspace.items.weapon.MissileEditsSpace;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * backport: NTM: Space restyles some of the core's missile parts by pointing their item_model
 * component at its own item definitions. 1.21.1 has no item_model component and the core reads
 * item definitions by item id, so the restyled parts take NTM: Space's definitions once they are
 * read.
 */
@Mixin(ClientItems.class)
public abstract class MixinClientItems {
    @Shadow private static volatile Map<ResourceLocation, ClientItems.Definition> definitions;

    @Inject(method = "onRegisterAdditional", at = @At("TAIL"))
    private static void hbmspace$restyledParts(
            ModelEvent.RegisterAdditional event, CallbackInfo ci) {
        Map<ResourceLocation, ClientItems.Definition> defs = definitions;
        for (Map.Entry<ResourceLocation, ResourceLocation> e :
                MissileEditsSpace.restyledDefinitions().entrySet()) {
            ClientItems.Definition restyled = defs.get(e.getValue());
            if (restyled != null) defs.put(e.getKey(), restyled);
        }
    }
}
