// SPDX-FileCopyrightText: 2026 SecretAgent12
// SPDX-License-Identifier: LGPL-3.0-only
// Additional terms under GPL-3.0 section 7 apply: see NOTICE.md

package com.hbmspace.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * backport: 26.x draws the sky and the clouds with the frame's view rotation on the model-view
 * matrix and its projection set; 1.21.1 passes them to LevelRenderer.renderSky / renderClouds, and
 * the core's sky and cloud hooks do not hand them on. They are the camera's rotation (the matrix
 * 1.21.1's GameRenderer builds and passes as the frustum matrix) and the projection the
 * GameRenderer has set for the frame.
 */
final class SkyFrame {

    private SkyFrame() {}

    /** 1.21.1 GameRenderer.renderLevel's frustum matrix for this camera. */
    static Matrix4f viewRotation(Camera camera) {
        return new Matrix4f().rotation(camera.rotation().conjugate(new Quaternionf()));
    }

    static Matrix4f viewRotation() {
        return viewRotation(Minecraft.getInstance().gameRenderer.getMainCamera());
    }

    static Matrix4f projection() {
        return new Matrix4f(RenderSystem.getProjectionMatrix());
    }
}
