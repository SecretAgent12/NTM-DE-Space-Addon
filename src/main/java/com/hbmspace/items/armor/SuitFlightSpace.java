// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.armor;

import com.hbm.api.environment.Environment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class SuitFlightSpace {

    private SuitFlightSpace() {}

    public static Vec3 dnsGlide(
            Player player,
            Vec3 motion,
            boolean backpack,
            float forward,
            float strafe,
            float gravity) {
        double dx = motion.x;
        double dy = motion.y;
        double dz = motion.z;
        if (!player.isShiftKeyDown() && !player.onGround() && backpack) {
            player.fallDistance = 0;
            dy =
                    dy < -1D
                            ? dy + 0.4D
                            : dy < -0.1D ? dy + 0.2D : dy > 0.1D ? dy - 0.2D : dy < 0D ? 0D : dy;
            if (forward != 0F || strafe != 0F) {
                dx *= 1.05D;
                dz *= 1.05D;
            }
            if (forward != 0F && player.isSprinting()) {
                Vec3 look = player.getLookAngle();
                dx += look.x * 0.25D * forward;
                dz += look.z * 0.25D * forward;
            }
        }
        if (player.isShiftKeyDown() && !player.onGround() && (gravity > 0F || backpack)) dy -= 0.1D;
        return new Vec3(dx, dy, dz);
    }

    public static boolean dnsShowsThrust(ServerPlayer player, boolean backpack) {
        // backport: 26.x reads the movement keys from getLastClientInput(); a 1.21.1 server only
        // gets them while riding, so "moving" is whether the player moved this tick
        double mx = player.getX() - player.xo, mz = player.getZ() - player.zo;
        return shows(
                player.isShiftKeyDown(),
                player.onGround(),
                backpack,
                mx * mx + mz * mz > 1.0E-4,
                player.isSprinting(),
                Environment.gravity(player));
    }

    public static boolean shows(
            boolean sneaking,
            boolean onGround,
            boolean backpack,
            boolean moving,
            boolean sprinting,
            float gravity) {
        if (onGround || !backpack) return false;
        if (sneaking) return gravity == 0F;
        return moving || gravity > 0F || sprinting;
    }
}
