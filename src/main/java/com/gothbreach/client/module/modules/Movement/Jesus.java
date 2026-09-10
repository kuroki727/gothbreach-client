package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.block.Blocks;

public class Jesus extends Module {
    @Setting(name="Скорость подъёма", min=0.05, max=0.5, decimalPlaces=2)
    public double upwardSpeed = 0.1;

    @Setting(name="Режим", values={"Обычный","Пакетный"})
    public String mode = "Обычный";

    public Jesus() {
        super("Jesus", "Хождение по воде", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        var block = mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock();

        if (block == Blocks.WATER || block == Blocks.LAVA) {
            if (mode.equals("Обычный")) {
                mc.player.setVelocity(mc.player.getVelocity().x, upwardSpeed, mc.player.getVelocity().z);
            } else {
                // Пакетный: телепорт наверх воды
                double y = mc.player.getY();
                while (mc.world.getBlockState(new net.minecraft.util.math.BlockPos(mc.player.getBlockX(), (int)y, mc.player.getBlockZ())).getBlock() == Blocks.WATER) {
                    y += 0.1;
                }
                mc.player.updatePosition(mc.player.getX(), y, mc.player.getZ());
            }
        }
    }
}
