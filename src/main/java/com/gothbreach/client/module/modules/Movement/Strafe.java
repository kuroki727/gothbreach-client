package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;

public class Strafe extends Module {
    @Setting(name="Скорость", min=0.1, max=2.0, decimalPlaces=2)
    public double speed = 0.2873;

    @Setting(name="Только на земле")
    public boolean onGroundOnly = true;

    public Strafe() {
        super("Strafe", "Улучшенное стрейф-движение", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        if (onGroundOnly && !mc.player.isOnGround()) return;

        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        if (forward == 0 && strafe == 0) return;

        float yaw = mc.player.getYaw();
        double direction = Math.toDegrees(Math.atan2(strafe, forward)) + yaw;
        mc.player.setVelocity(
            -Math.sin(Math.toRadians(direction)) * speed,
            mc.player.getVelocity().y,
            Math.cos(Math.toRadians(direction)) * speed
        );
    }
}
