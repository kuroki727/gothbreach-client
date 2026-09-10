package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;

public class Sprint extends Module {
    @Setting(name="Только вперёд")
    public boolean onlyForward = true;

    @Setting(name="Игнорировать голод")
    public boolean ignoreHunger = false;

    public Sprint() {
        super("Sprint", "Автоматический спринт", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        boolean canSprint = mc.player.getHungerManager().getFoodLevel() > 6 || ignoreHunger;
        boolean moving = onlyForward ? mc.player.forwardSpeed > 0 : mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
        if (canSprint && moving && !mc.player.isSneaking() && !mc.player.horizontalCollision) {
            mc.player.setSprinting(true);
        }
    }
}
