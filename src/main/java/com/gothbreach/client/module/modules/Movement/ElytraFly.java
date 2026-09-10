package com.gothbreach.client.module.modules.movement;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {
    @Setting(name="Режим", values={"Управление","Буст","Авто"})
    public String mode = "Управление";

    @Setting(name="Горизонтальная скорость", min=0.5, max=10.0, decimalPlaces=1)
    public double horizontalSpeed = 2.0;

    @Setting(name="Вертикальная скорость", min=0.1, max=5.0, decimalPlaces=1)
    public double verticalSpeed = 1.0;

    @Setting(name="Множитель буста", min=1.0, max=10.0, decimalPlaces=1)
    public double boostMultiplier = 3.0;

    @Setting(name="Авто-взлёт")
    public boolean autoTakeOff = true;

    @Setting(name="Анти-урон от падения")
    public boolean antiFallDamage = true;

    @Setting(name="Стабилизация")
    public boolean stabilize = true;

    @Setting(name="Авто-надевание элитр")
    public boolean autoSwitchElytra = true;

    @Setting(name="Максимальная скорость", min=1.0, max=20.0, decimalPlaces=1)
    public double maxSpeed = 8.0;

    public ElytraFly() {
        super("ElytraFly", "Управление полётом на элитрах", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        // ничего не делаем с гравитацией при включении
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setNoGravity(false);
            if (antiFallDamage) mc.player.fallDistance = 0;
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Автонадевание элитр
        if (autoSwitchElytra && !isWearingElytra() && hasElytraInInventory()) {
            equipElytra();
        }

        if (!isWearingElytra()) {
            // если элитры сняты, выключаем noGravity
            if (mc.player.hasNoGravity()) mc.player.setNoGravity(false);
            return;
        }

        // Автовзлёт: только если игрок на земле и жмёт прыжок
        if (autoTakeOff && mc.player.isOnGround() && mc.options.jumpKey.isPressed()) {
            mc.player.jump();
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (!mc.player.isFallFlying()) {
            // если ещё не летит, снимаем noGravity
            if (mc.player.hasNoGravity()) mc.player.setNoGravity(false);
            return;
        }

        // Только теперь включаем noGravity (в полёте)
        if (!mc.player.hasNoGravity()) mc.player.setNoGravity(true);

        if (antiFallDamage) mc.player.fallDistance = 0;

        switch (mode) {
            case "Буст" -> boostMode();
            case "Авто" -> autoMode();
            default -> controlMode();
        }

        // Зависание: если не нажаты Space/Shift, вертикальная скорость = 0
        if (!mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed()) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, 0, vel.z);
        }

        // Ограничение максимальной скорости
        Vec3d vel = mc.player.getVelocity();
        if (vel.length() > maxSpeed) {
            mc.player.setVelocity(vel.normalize().multiply(maxSpeed));
        }
    }

    private void controlMode() {
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;

        float yaw = mc.player.getYaw();
        double sin = Math.sin(Math.toRadians(yaw));
        double cos = Math.cos(Math.toRadians(yaw));

        // Правильная формула движения
        double motionX = forward * horizontalSpeed * -sin + strafe * horizontalSpeed * cos;
        double motionZ = forward * horizontalSpeed * cos + strafe * horizontalSpeed * sin;

        double motionY = 0;
        if (mc.options.jumpKey.isPressed()) motionY = verticalSpeed;
        else if (mc.options.sneakKey.isPressed()) motionY = -verticalSpeed;

        mc.player.setVelocity(motionX, motionY, motionZ);
    }

    private void boostMode() {
        Vec3d lookVec = mc.player.getRotationVec(1.0f);
        double speed = horizontalSpeed * boostMultiplier;
        Vec3d newVel = lookVec.multiply(speed);
        if (mc.options.jumpKey.isPressed()) {
            newVel = newVel.add(0, verticalSpeed * boostMultiplier, 0);
        } else if (mc.options.sneakKey.isPressed()) {
            newVel = newVel.add(0, -verticalSpeed * boostMultiplier, 0);
        }
        mc.player.setVelocity(newVel);
    }

    private void autoMode() {
        Vec3d lookVec = mc.player.getRotationVec(1.0f);
        double speed = horizontalSpeed;
        Vec3d newVel = new Vec3d(lookVec.x * speed, 0, lookVec.z * speed);
        if (mc.player.getY() < 80) {
            newVel = newVel.add(0, verticalSpeed, 0);
        } else if (mc.player.getY() > 120) {
            newVel = newVel.add(0, -verticalSpeed, 0);
        }
        mc.player.setVelocity(newVel);
    }

    private boolean isWearingElytra() {
        ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        return chest.getItem() == Items.ELYTRA;
    }

    private boolean hasElytraInInventory() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ELYTRA) return true;
        }
        return false;
    }

    private void equipElytra() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ELYTRA) {
                mc.interactionManager.clickSlot(0, 6, i, net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
                break;
            }
        }
    }
}