package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;

public class Step extends Module {
    @Setting(name="Высота", min=0.6, max=4.0, decimalPlaces=1)
    public double stepHeight = 4.0;

    @Setting(name="Режим", values={"Обычный","Пакетный"})
    public String mode = "Обычный";

    public Step() {
        super("Step", "Автоматически забирается на блоки", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;

        if (mode.equals("Обычный")) {
            if (mc.player.horizontalCollision) {
                mc.player.setStepHeight((float) stepHeight);
            } else {
                mc.player.setStepHeight(0.6f);
            }
        } else {
            // Пакетный режим
            if (mc.player.horizontalCollision && mc.player.isOnGround()) {
                mc.player.setVelocity(mc.player.getVelocity().x, stepHeight, mc.player.getVelocity().z);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setStepHeight(0.6f);
    }
}
