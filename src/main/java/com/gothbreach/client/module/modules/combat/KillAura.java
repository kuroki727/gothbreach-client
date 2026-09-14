package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import com.gothbreach.client.util.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class KillAura extends Module {
    // ===== Основные =====
    @Setting(name="Дальность", min=2.0, max=8.0, decimalPlaces=1)
    public double range = 4.5;

    @Setting(name="Min CPS", min=1, max=20)
    public int minCps = 8;

    @Setting(name="Max CPS", min=1, max=20)
    public int maxCps = 14;

    // ===== Цели =====
    @Setting(name="Игроки")
    public boolean players = true;

    @Setting(name="Монстры")
    public boolean monsters = false;

    @Setting(name="Животные")
    public boolean animals = false;

    @Setting(name="Приоритет", values={"Ближний","Наименьшее HP","Наибольшее HP","Ближе к прицелу","Дольше живёт"})
    public String priority = "Ближний";

    // ===== Ротация =====
    @Setting(name="Поворот к цели")
    public boolean rotate = true;

    @Setting(name="Тихий поворот")
    public boolean silentRotation = true;

    @Setting(name="Скорость поворота", min=0.1, max=1.0, decimalPlaces=2)
    public double rotationSpeed = 0.7;

    @Setting(name="Предсказание движения")
    public boolean predict = true;

    @Setting(name="Мин. предсказание", min=0.0, max=3.0, decimalPlaces=1)
    public double minPredict = 0.5;

    @Setting(name="Макс. предсказание", min=0.0, max=3.0, decimalPlaces=1)
    public double maxPredict = 1.5;

    // ===== Мульти-цель =====
    @Setting(name="Режим цели", values={"Single","Switch","Multi"})
    public String targetMode = "Single";

    @Setting(name="Макс. целей", min=1, max=5)
    public int maxTargets = 2;

    @Setting(name="Задержка переключения (мс)", min=0, max=1000)
    public int switchDelay = 300;

    // ===== Оружие =====
    @Setting(name="Авто-выбор оружия")
    public boolean autoWeapon = true;

    @Setting(name="Приоритет оружия", values={"Скорость","Урон","Разрушение щита"})
    public String weaponPriority = "Скорость";

    @Setting(name="Возврат к прежнему слоту")
    public boolean switchBack = true;

    @Setting(name="Время возврата (мс)", min=100, max=3000)
    public int releaseTime = 500;

    @Setting(name="Ждать полный кулдаун")
    public boolean waitForCooldown = true;

    @Setting(name="Порог кулдауна", min=0.5, max=1.0, decimalPlaces=2)
    public double cooldownThreshold = 0.9;

    // ===== Дополнительно =====
    @Setting(name="Автоблок")
    public boolean autoBlock = true;

    @Setting(name="Только с оружием")
    public boolean weaponOnly = false;

    @Setting(name="Проверка стены")
    public boolean rayCast = true;

    @Setting(name="Бить сквозь стены")
    public boolean throughWalls = false;

    // ===== Внутреннее =====
    private long lastAttack = 0;
    private int currentCps = 10;
    private Entity currentTarget = null;
    private long lastSwitch = 0;
    private final List<Entity> multiTargets = new ArrayList<>();
    private float silentYaw, silentPitch;

    private int previousSlot = -1;
    private int weaponSlot = -1;
    private long weaponSwitchTime = 0;
    private boolean weaponSwitched = false;

    public KillAura() {
        super("KillAura", "Автоматическая атака с гибкими настройками", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        currentCps = randomCps();
        lastAttack = 0;
        currentTarget = null;
        multiTargets.clear();
        previousSlot = -1;
        weaponSlot = -1;
        weaponSwitched = false;
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.useKey.setPressed(false);
        if (weaponSwitched && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        currentTarget = null;
        multiTargets.clear();
        weaponSwitched = false;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (System.currentTimeMillis() - lastAttack > 1000) {
            currentCps = randomCps();
        }

        if (weaponSwitched && switchBack && System.currentTimeMillis() - weaponSwitchTime > releaseTime) {
            if (previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                weaponSwitched = false;
            }
        }

        updateTargets();

        if (targetMode.equals("Multi")) {
            handleMultiAttack();
        } else {
            handleSingleAttack();
        }
    }

    private void updateTargets() {
        multiTargets.clear();
        List<Entity> candidates = collectCandidates();
        if (candidates.isEmpty()) {
            currentTarget = null;
            return;
        }

        Comparator<Entity> comparator = getComparator();
        candidates.sort(comparator);

        if (targetMode.equals("Multi")) {
            int count = Math.min(maxTargets, candidates.size());
            multiTargets.addAll(candidates.subList(0, count));
        } else {
            currentTarget = candidates.get(0);
        }
    }

    private void handleSingleAttack() {
        if (currentTarget == null || !currentTarget.isAlive()) return;
        if (mc.player.distanceTo(currentTarget) > range) return;

        if (rotate) applyRotation(currentTarget);
        tryAttack(currentTarget);
    }

    private void handleMultiAttack() {
        if (multiTargets.isEmpty()) return;
        for (Entity target : multiTargets) {
            if (!target.isAlive()) continue;
            if (mc.player.distanceTo(target) > range) continue;
            if (rotate) applyRotation(target);
            tryAttack(target);
        }
    }

    private void tryAttack(Entity target) {
        if (waitForCooldown) {
            float cooldown = mc.player.getAttackCooldownProgress(0.5f);
            if (cooldown < cooldownThreshold) return;
        }

        long now = System.currentTimeMillis();
        long delay = 1000L / Math.max(1, currentCps);
        if (now - lastAttack < delay) return;

        if (rayCast && !throughWalls && !mc.player.canSee(target)) return;

        if (autoWeapon) {
            switchToBestWeapon(target);
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;

        if (autoBlock && mc.player.getMainHandStack().getItem() instanceof SwordItem) {
            mc.options.useKey.setPressed(true);
        }
    }

    private void switchToBestWeapon(Entity target) {
        if (mc.player == null) return;

        if (!weaponSwitched) {
            previousSlot = mc.player.getInventory().selectedSlot;
        }

        int bestSlot = findBestWeapon(target);
        if (bestSlot == -1 || bestSlot == mc.player.getInventory().selectedSlot) return;

        mc.player.getInventory().selectedSlot = bestSlot;
        weaponSlot = bestSlot;
        weaponSwitchTime = System.currentTimeMillis();
        weaponSwitched = true;
    }

    private int findBestWeapon(Entity target) {
        int bestSlot = -1;
        double bestValue = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            double value = getWeaponValue(stack, target);
            if (value > bestValue) {
                bestValue = value;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private double getWeaponValue(ItemStack stack, Entity target) {
        Item item = stack.getItem();

        double baseDamage = 1.0;
        double attackSpeed = 4.0;

        if (item instanceof SwordItem sword) {
            baseDamage = sword.getAttackDamage() + 1.0;
            attackSpeed = 1.6;
        } else if (item instanceof AxeItem axe) {
            baseDamage = axe.getAttackDamage() + 1.0;
            attackSpeed = 0.8;
        } else if (item instanceof PickaxeItem pickaxe) {
            baseDamage = pickaxe.getAttackDamage() + 1.0;
            attackSpeed = 1.2;
        } else if (item instanceof ShovelItem shovel) {
            baseDamage = shovel.getAttackDamage() + 1.0;
            attackSpeed = 1.0;
        }

        int sharpness = net.minecraft.enchantment.EnchantmentHelper.getLevel(
            net.minecraft.enchantment.Enchantments.SHARPNESS, stack);
        baseDamage += sharpness * 0.5;

        if (target instanceof LivingEntity living) {
            // Нежить — Smite
            if (living.getGroup() == EntityGroup.UNDEAD) {
                int smite = net.minecraft.enchantment.EnchantmentHelper.getLevel(
                    net.minecraft.enchantment.Enchantments.SMITE, stack);
                baseDamage += smite * 2.5;
            }
            // Членистоногие — Bane of Arthropods
            if (living.getGroup() == EntityGroup.ARTHROPOD) {
                int bane = net.minecraft.enchantment.EnchantmentHelper.getLevel(
                    net.minecraft.enchantment.Enchantments.BANE_OF_ARTHROPODS, stack);
                baseDamage += bane * 2.5;
            }
        }

        if (weaponPriority.equals("Разрушение щита") && target instanceof PlayerEntity player) {
            if (player.isBlocking() && item instanceof AxeItem) {
                baseDamage *= 3.0;
            }
        }

        return switch (weaponPriority) {
            case "Урон" -> baseDamage;
            case "Скорость" -> baseDamage * attackSpeed;
            default -> baseDamage;
        };
    }

    private void applyRotation(Entity target) {
        float[] rotations = RotationUtils.getRotations(mc.player, target);

        if (predict) {
            double dist = mc.player.distanceTo(target);
            double predictFactor = MathHelper.clamp(dist / range, minPredict, maxPredict);
            rotations[0] += (float)(target.getVelocity().x * predictFactor * 20);
            rotations[1] += (float)(target.getVelocity().y * predictFactor * 20);
        }

        if (silentRotation) {
            silentYaw = rotations[0];
            silentPitch = rotations[1];
            mc.player.setYaw(silentYaw);
            mc.player.setPitch(silentPitch);
        } else {
            float yawDiff = MathHelper.wrapDegrees(rotations[0] - mc.player.getYaw());
            float pitchDiff = rotations[1] - mc.player.getPitch();
            mc.player.setYaw(mc.player.getYaw() + yawDiff * (float) rotationSpeed);
            mc.player.setPitch(mc.player.getPitch() + pitchDiff * (float) rotationSpeed);
        }
    }

    private List<Entity> collectCandidates() {
        List<Entity> list = new ArrayList<>();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !e.isAlive()) continue;
            if (mc.player.distanceTo(e) > range) continue;
            if (!isValidTarget(e)) continue;
            list.add(e);
        }
        return list;
    }

    private boolean isValidTarget(Entity e) {
        if (e instanceof PlayerEntity) return players;
        if (e instanceof Monster) return monsters;
        if (e instanceof AnimalEntity) return animals;
        return false;
    }

    private Comparator<Entity> getComparator() {
        return switch (priority) {
            case "Наименьшее HP" -> Comparator.comparingDouble(e -> ((LivingEntity) e).getHealth());
            case "Наибольшее HP" -> Comparator.comparingDouble(e -> -((LivingEntity) e).getHealth());
            case "Ближе к прицелу" -> Comparator.comparingDouble(this::angleToTarget);
            case "Дольше живёт" -> Comparator.comparingInt(e -> -e.age);
            default -> Comparator.comparingDouble(e -> mc.player.distanceTo(e));
        };
    }

    private double angleToTarget(Entity e) {
        var look = mc.player.getRotationVec(1.0f);
        var toTarget = e.getPos().add(0, e.getHeight() / 2, 0).subtract(mc.player.getEyePos()).normalize();
        return -look.dotProduct(toTarget);
    }

    private int randomCps() {
        if (maxCps <= minCps) return minCps;
        return minCps + new Random().nextInt(maxCps - minCps + 1);
    }
}