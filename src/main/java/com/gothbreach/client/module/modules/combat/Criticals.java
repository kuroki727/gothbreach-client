package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {
    @Setting(name="Режим", values={"Пакетный","Обычный"})
    public String mode = "Пакетный";

    @Setting(name="Высота подскока", min=0.05, max=0.5, decimalPlaces=2)
    public double jumpHeight = 0.11;

    public Criticals() {
        super("Criticals", "Все удары становятся критическими", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;

        if (mode.equals("Пакетный")) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), mc.player.getY() + jumpHeight, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
        } else {
            mc.player.setVelocity(mc.player.getVelocity().x, jumpHeight, mc.player.getVelocity().z);
        }
    }
}
