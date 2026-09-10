package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import com.gothbreach.client.util.PlayerUtils;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
    @Setting(name="Режим", values={"Vanilla","Strafe"})
    public String mode = "Vanilla";

    @Setting(name="Скорость (bps)", min=1.0, max=20.0, decimalPlaces=1)
    public double vanillaSpeed = 5.6;

    @Setting(name="Strafe Speed", min=0.5, max=3.0, decimalPlaces=1)
    public double strafeSpeed = 1.6;

    @Setting(name="Timer", min=0.01, max=10.0, decimalPlaces=2)
    public double timer = 1.0;

    @Setting(name="In Liquids")
    public boolean inLiquids = false;

    @Setting(name="When Sneaking")
    public boolean whenSneaking = false;

    @Setting(name="Only On Ground")
    public boolean vanillaOnGround = false;

    public Speed() {
        super("Speed", "Ускорение передвижения", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        if (mc.player.isFallFlying() || mc.player.isClimbing() || mc.player.getVehicle() != null) return;
        if (!whenSneaking && mc.player.isSneaking()) return;
        if (vanillaOnGround && !mc.player.isOnGround() && mode.equals("Vanilla")) return;
        if (!inLiquids && (mc.player.isTouchingWater() || mc.player.isInLava())) return;

        Vec3d vel = PlayerUtils.getHorizontalVelocity(mode.equals("Vanilla") ? vanillaSpeed : strafeSpeed);
        mc.player.setVelocity(vel.x, mc.player.getVelocity().y, vel.z);
    }
}