package com.gothbreach.client.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils {
    public static float[] getRotations(Entity from, Entity to) {
        double dx = to.getX() - from.getX();
        double dy = (to.getY() + to.getEyeHeight(to.getPose())) - (from.getY() + from.getEyeHeight(from.getPose()));
        double dz = to.getZ() - from.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{yaw, pitch};
    }

    public static float[] smoothRotations(float[] current, float[] target, float speed) {
        float yawDiff = MathHelper.wrapDegrees(target[0] - current[0]);
        float pitchDiff = target[1] - current[1];
        return new float[]{
            current[0] + yawDiff * speed,
            current[1] + pitchDiff * speed
        };
    }
}
