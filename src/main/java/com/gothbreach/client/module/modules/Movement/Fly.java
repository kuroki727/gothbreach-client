package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;

public class Fly extends Module {
    @Setting(name="Скорость", min=0.1, max=2.0, decimalPlaces=2)
    public double speed = 0.5;

    @Setting(name="Режим", values={"Обычный","Пакетный"})
    public String mode = "Обычный";

    @Setting(name="Вертикальная скорость", min=0.1, max=1.0, decimalPlaces=2)
    public double verticalSpeed = 0.3;

    public Fly() {
        super("Fly", "Полёт", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;

        if (mode.equals("Обычный")) {
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlySpeed((float) speed / 5f);
            if (mc.options.jumpKey.isPressed()) {
                mc.player.setVelocity(mc.player.getVelocity().x, verticalSpeed, mc.player.getVelocity().z);
            }
            if (mc.options.sneakKey.isPressed()) {
                mc.player.setVelocity(mc.player.getVelocity().x, -verticalSpeed, mc.player.getVelocity().z);
            }
        } else {
            // Пакетный режим (без abilities)
            mc.player.getAbilities().flying = false;
            double y = mc.player.getY();
            if (mc.options.jumpKey.isPressed()) y += verticalSpeed;
            if (mc.options.sneakKey.isPressed()) y -= verticalSpeed;
            mc.player.setVelocity(0, 0, 0);
            mc.player.updatePosition(mc.player.getX(), y, mc.player.getZ());
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().setFlySpeed(0.05f);
        }
    }
}
