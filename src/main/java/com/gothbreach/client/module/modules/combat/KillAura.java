package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import com.gothbreach.client.util.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class KillAura extends Module {
    @Setting(name="Дальность", min=3, max=8, decimalPlaces=1)
    public double range = 4.5;
    @Setting(name="Задержка (мс)", min=100, max=2000)
    public int attackDelay = 500;
    @Setting(name="Автоблок")
    public boolean autoBlock = true;
    @Setting(name="Поворот к цели")
    public boolean rotate = true;
    @Setting(name="Скорость поворота", min=0.1, max=1.0, decimalPlaces=2)
    public double rotationSpeed = 0.6;
    @Setting(name="Игроки")
    public boolean players = true;
    @Setting(name="Монстры")
    public boolean monsters = true;
    @Setting(name="Животные")
    public boolean animals = false;
    @Setting(name="Приоритет", values={"Ближний","Наименьшее HP"})
    public String priority = "Ближний";

    private long lastAttack = 0;
    private float currentYaw = 0, currentPitch = 0;

    public KillAura() {
        super("KillAura", "Автоматическая атака", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        Entity target = selectTarget();
        if (target == null) return;

        if (rotate) {
            float[] rot = RotationUtils.getRotations(mc.player, target);
            float yawDiff = MathHelper.wrapDegrees(rot[0] - currentYaw);
            float pitchDiff = rot[1] - currentPitch;
            currentYaw += yawDiff * (float) rotationSpeed;
            currentPitch += pitchDiff * (float) rotationSpeed;
            mc.player.setYaw(currentYaw);
            mc.player.setPitch(currentPitch);
        }

        if (System.currentTimeMillis() - lastAttack >= attackDelay) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAttack = System.currentTimeMillis();
            if (autoBlock && mc.player.getMainHandStack().getItem() instanceof SwordItem) {
                mc.options.useKey.setPressed(true);
            }
        }
    }

    private Entity selectTarget() {
        List<Entity> candidates = new ArrayList<>();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !e.isAlive() || e.distanceTo(mc.player) > range) continue;
            boolean valid = false;
            if (players && e instanceof PlayerEntity) valid = true;
            if (monsters && e instanceof Monster) valid = true;
            if (animals && e instanceof AnimalEntity) valid = true;
            if (valid) candidates.add(e);
        }
        if (candidates.isEmpty()) return null;
        if (priority.equals("Наименьшее HP")) {
            return candidates.stream().min(Comparator.comparingDouble(e -> ((LivingEntity)e).getHealth())).orElse(null);
        }
        return candidates.stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc.player))).orElse(null);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.useKey.setPressed(false);
    }
}