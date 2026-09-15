package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import java.util.*;

public class CrystalAura extends Module {
    // ===== Общие =====
    @Setting(name="Target Range", min=4.0, max=16.0, decimalPlaces=1)
    public double targetRange = 10.0;

    @Setting(name="Place Range", min=2.0, max=6.0, decimalPlaces=1)
    public double placeRange = 4.5;

    @Setting(name="Break Range", min=2.0, max=6.0, decimalPlaces=1)
    public double breakRange = 4.5;

    @Setting(name="Walls Range", min=0.0, max=6.0, decimalPlaces=1)
    public double wallsRange = 3.0;

    // ===== Урон =====
    @Setting(name="Min Damage", min=0.0, max=20.0, decimalPlaces=1)
    public double minDamage = 6.0;

    @Setting(name="Max Self Damage", min=0.0, max=20.0, decimalPlaces=1)
    public double maxSelfDamage = 8.0;

    @Setting(name="Face Place", description="Ставить кристаллы у лица, если у цели мало HP")
    public boolean facePlace = false;

    @Setting(name="Face Place Health", min=1.0, max=20.0, decimalPlaces=1)
    public double facePlaceHealth = 8.0;

    @Setting(name="Face Place Armor", min=0.0, max=100.0, decimalPlaces=1)
    public double facePlaceArmor = 20.0;

    // ===== Задержки =====
    @Setting(name="Place Delay (мс)", min=0, max=1000)
    public int placeDelay = 50;

    @Setting(name="Break Delay (мс)", min=0, max=1000)
    public int breakDelay = 50;

    @Setting(name="Attack Frequency (мс)", min=0, max=500)
    public int attackFrequency = 100;

    // ===== Режимы =====
    @Setting(name="Place Mode", values={"Safe","Suicide"})
    public String placeMode = "Safe";

    @Setting(name="Break Mode", values={"Safe","Suicide","Instant"})
    public String breakMode = "Safe";

    @Setting(name="Rotation Mode", values={"None","Place","Break","Both"})
    public String rotationMode = "Both";

    @Setting(name="Strict", description="Не ставить в ямах 1x1")
    public boolean strict = false;

    @Setting(name="Ignore Walls")
    public boolean ignoreWalls = true;

    @Setting(name="Auto Switch")
    public boolean autoSwitch = true;

    @Setting(name="Spoof Change", description="Спуфить смену предмета на кристалл")
    public boolean spoofChange = false;

    @Setting(name="Remove Crystals", description="Убирать кристаллы из мира сразу после взрыва")
    public boolean removeCrystals = true;

    @Setting(name="Multi Task", description="Работать во время копания")
    public boolean multiTask = false;

    @Setting(name="Pause On CA", description="Пауза, когда CrystalAura ставит")
    public boolean pauseOnCA = false;

    // ===== Внутреннее =====
    private long lastPlace = 0;
    private long lastBreak = 0;
    private long lastAttack = 0;
    private Entity currentTarget = null;

    public CrystalAura() {
        super("CrystalAura", "Автоматический взрыв кристаллов с тонкой настройкой", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastPlace = 0;
        lastBreak = 0;
        lastAttack = 0;
        currentTarget = null;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        currentTarget = selectTarget();
        if (currentTarget == null) return;

        long now = System.currentTimeMillis();

        // Авто-переключение на кристаллы
        if (autoSwitch) switchToCrystal();

        // Place
        if (now - lastPlace >= placeDelay) {
            BlockPos bestPos = findBestCrystalPos(currentTarget);
            if (bestPos != null) {
                if (rotationMode.equals("Place") || rotationMode.equals("Both")) {
                    rotateTo(bestPos);
                }
                placeCrystal(bestPos);
                lastPlace = now;
            }
        }

        // Break
        if (now - lastBreak >= breakDelay) {
            EndCrystalEntity crystal = findBestCrystal(currentTarget);
            if (crystal != null) {
                if (rotationMode.equals("Break") || rotationMode.equals("Both")) {
                    rotateToEntity(crystal);
                }
                mc.interactionManager.attackEntity(mc.player, crystal);
                if (removeCrystals) crystal.remove(Entity.RemovalReason.KILLED);
                lastBreak = now;
            }
        }
    }

    private Entity selectTarget() {
        List<PlayerEntity> players = new ArrayList<>(mc.world.getPlayers());
        players.removeIf(p -> p == mc.player || !p.isAlive() || p.distanceTo(mc.player) > targetRange);
        if (players.isEmpty()) return null;
        return players.stream().min(Comparator.comparingDouble(p -> p.distanceTo(mc.player))).orElse(null);
    }

    private BlockPos findBestCrystalPos(Entity target) {
        BlockPos targetPos = target.getBlockPos();
        BlockPos best = null;
        double bestScore = -1;

        // Обычный поиск вокруг цели
        for (BlockPos pos : BlockPos.iterateOutwards(targetPos, (int) placeRange, (int) placeRange, (int) placeRange)) {
            if (!isValidCrystalSpot(pos)) continue;
            if (!ignoreWalls && !canSeePosition(Vec3d.ofCenter(pos))) continue;

            double damage = getExplosionDamage(pos, target);
            double selfDamage = getExplosionDamage(pos, mc.player);

            if (damage < minDamage) continue;
            if (placeMode.equals("Safe") && selfDamage > maxSelfDamage) continue;

            double score = damage - selfDamage * 0.5;
            if (score > bestScore) {
                bestScore = score;
                best = pos;
            }
        }

        // Face Place — ставить у лица, если у цели мало HP или сломана броня
        if (facePlace && best == null) {
            if (target instanceof PlayerEntity player) {
                boolean lowHealth = player.getHealth() <= facePlaceHealth;
                boolean lowArmor = getArmorDurability(player) <= facePlaceArmor;
                if (lowHealth || lowArmor) {
                    BlockPos facePos = targetPos.up(1);
                    if (isValidCrystalSpot(facePos)) {
                        return facePos;
                    }
                }
            }
        }

        return best;
    }

    private boolean isValidCrystalSpot(BlockPos pos) {
        var block = mc.world.getBlockState(pos).getBlock();
        var above = mc.world.getBlockState(pos.up()).getBlock();

        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) return false;
        if (above != Blocks.AIR) return false;

        // Проверка на яму 1x1
        if (strict) {
            int airCount = 0;
            for (Direction dir : Direction.values()) {
                if (mc.world.getBlockState(pos.offset(dir)).isAir()) airCount++;
            }
            if (airCount >= 4) return false;
        }

        // Проверка на наличие кристалла
        return mc.world.getOtherEntities(null, new Box(pos.up())).isEmpty();
    }

    private EndCrystalEntity findBestCrystal(Entity target) {
        List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(EndCrystalEntity.class,
                new Box(target.getPos(), target.getPos()).expand(breakRange), e -> true);
        if (crystals.isEmpty()) return null;
        return crystals.stream().min(Comparator.comparingDouble(c -> c.distanceTo(mc.player))).orElse(null);
    }

    private double getExplosionDamage(BlockPos crystalPos, Entity target) {
        double dist = target.getPos().distanceTo(Vec3d.ofCenter(crystalPos.up()));
        if (dist > 12) return 0;
        double damage = 12.0 * (1.0 - dist / 12.0);
        // Учёт сопротивления взрыву
        if (target instanceof PlayerEntity player) {
            int blastProt = net.minecraft.enchantment.EnchantmentHelper.getLevel(
                net.minecraft.enchantment.Enchantments.BLAST_PROTECTION, player.getInventory().getArmorStack(2));
            damage *= (1.0 - blastProt * 0.15);
        }
        return Math.max(0, damage);
    }

    private double getArmorDurability(PlayerEntity player) {
        double total = 0;
        for (int i = 0; i < 4; i++) {
            var stack = player.getInventory().getArmorStack(i);
            if (!stack.isEmpty()) {
                total += (1.0 - (double) stack.getDamage() / stack.getMaxDamage()) * 100;
            }
        }
        return total / 4;
    }

    private boolean canSeePosition(Vec3d target) {
        var hit = mc.world.raycast(new RaycastContext(
            mc.player.getEyePos(),
            target,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            mc.player
        ));
        return hit == null || hit.getPos().squaredDistanceTo(target) < 0.1;
    }

    private void switchToCrystal() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                mc.player.getInventory().selectedSlot = i;
                break;
            }
        }
    }

    private void rotateTo(BlockPos pos) {
        Vec3d target = Vec3d.ofCenter(pos);
        double dx = target.x - mc.player.getX();
        double dy = target.y - mc.player.getEyeY();
        double dz = target.z - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    private void rotateToEntity(Entity entity) {
        double dx = entity.getX() - mc.player.getX();
        double dy = (entity.getY() + entity.getHeight() / 2) - mc.player.getEyeY();
        double dz = entity.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    private void placeCrystal(BlockPos pos) {
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
            new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
    }
}