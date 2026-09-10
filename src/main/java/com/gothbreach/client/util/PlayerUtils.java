package com.gothbreach.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class PlayerUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Vec3d getHorizontalVelocity(double bps) {
        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;

        double motionX = (forward * bps * -sin) + (strafe * bps * cos);
        double motionZ = (forward * bps * cos) + (strafe * bps * sin);

        return new Vec3d(motionX, mc.player.getVelocity().y, motionZ);
    }
}