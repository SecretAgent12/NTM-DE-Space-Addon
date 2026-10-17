// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.weapon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.hbm.backport.compat.ItemCompat;

public class BSword extends ItemCompat {

    private static final float SQUID_BLAST = 2.0F;

    public BSword(Properties properties) {
        super(properties);
    }

    @Override
    public void backport$hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        if (victim instanceof Squid && victim.level() instanceof ServerLevel level) {
            level.explode(
                    attacker,
                    victim.getX(),
                    victim.getY(),
                    victim.getZ(),
                    SQUID_BLAST,
                    Level.ExplosionInteraction.BLOCK);
        }
        super.backport$hurtEnemy(stack, victim, attacker);
    }
}
