package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PacketFly extends Module {
    @Setting(name="Скорость", min=0.1, max=2.0, decimalPlaces=2)
    public double speed = 0.5;

    @Setting(name="Фаза", min=0.01, max=0.5, decimalPlaces=2)
    public double phase = 0.1;

    @Setting(name="Режим", values={"Обычный","Телепорт"})
    public String mode = "Обычный";

    public PacketFly() {
        super("PacketFly", "Полёт через пакеты", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        mc.player.getAbilities().flying = false;

        double y = mc.player.getY();
        if (mc.options.jumpKey.isPressed()) y += speed;
        if (mc.options.sneakKey.isPressed()) y -= speed;

        if (mode.equals("Телепорт")) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), y, mc.player.getZ(), true));
        } else {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), y, mc.player.getZ(), true));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), y + phase, mc.player.getZ(), false));
        }
    }
}
