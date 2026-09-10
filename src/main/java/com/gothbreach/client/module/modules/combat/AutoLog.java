package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;

public class AutoLog extends Module {
    @Setting(name="Порог здоровья", min=1, max=20)
    public double healthThreshold = 4.0;

    @Setting(name="Отключение при взрыве")
    public boolean onExplosion = false;

    public AutoLog() {
        super("AutoLog", "Автоматически выходит при низком здоровье", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        if (mc.player.getHealth() <= healthThreshold) {
            mc.player.networkHandler.getConnection().disconnect(
                net.minecraft.text.Text.literal("AutoLog: низкое здоровье"));
        }
    }
}
