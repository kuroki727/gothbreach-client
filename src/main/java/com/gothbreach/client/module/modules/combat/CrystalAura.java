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
import java.util.*;

public class CrystalAura extends Module {
    @Setting(name="Дальность", min=2, max=8, decimalPlaces=1)
    public double range = 6.0;
    @Setting(name="Задержка (мс)", min=100, max=2000)
    public int attackDelay = 500;
    @Setting(name="Авто-переключение")
    public boolean autoSwitch = true;
    @Setting(name="Предсказание движения")
    public boolean predictMovement = true;
    @Setting(name="Минимальный урон", min=0, max=36, decimalPlaces=1)
    public double minDamage = 6.0;
    @Setting(name="Защита от своего урона")
    public boolean antiSelfDamage = true;
    @Setting(name="Максимальный свой урон", min=0, max=20, decimalPlaces=1)
    public double maxSelfDamage = 8.0;
    @Setting(name="Приоритет цели", values={"Ближний","Наименьшее HP"})
    public String priority = "Ближний";

    private long lastAttack = 0;

    public CrystalAura() {
        super("CrystalAura", "Автоматический взрыв кристаллов", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        Entity target = selectTarget();
        if (target == null) return;
        BlockPos bestPos = findBestCrystalPos(target);
        if (bestPos == null) return;
        long now = System.currentTimeMillis();
        if (now - lastAttack >= attackDelay) {
            if (autoSwitch) switchToCrystal();
            placeCrystal(bestPos);
            explodeCrystal(bestPos);
            lastAttack = now;
        }
    }

    private Entity selectTarget() {
        List<PlayerEntity> players = new ArrayList<>(mc.world.getPlayers());
        players.removeIf(p -> p == mc.player || !p.isAlive() || p.distanceTo(mc.player) > range);
        if (players.isEmpty()) return null;
        if (priority.equals("Наименьшее HP")) {
            return players.stream().min(Comparator.comparingDouble(PlayerEntity::getHealth)).orElse(null);
        }
        return players.stream().min(Comparator.comparingDouble(p -> p.distanceTo(mc.player))).orElse(null);
    }

    private BlockPos findBestCrystalPos(Entity target) {
        BlockPos targetPos = target.getBlockPos();
        BlockPos best = null;
        double bestScore = -1;
        for (BlockPos pos : BlockPos.iterateOutwards(targetPos, 3, 3, 3)) {
            if (!isValidCrystalSpot(pos)) continue;
            double damage = getExplosionDamage(pos, target);
            double selfDamage = antiSelfDamage ? getExplosionDamage(pos, mc.player) : 0;
            if (damage < minDamage || selfDamage > maxSelfDamage) continue;
            double score = damage - selfDamage * 0.5;
            if (score > bestScore) {
                bestScore = score;
                best = pos;
            }
        }
        return best;
    }

    private boolean isValidCrystalSpot(BlockPos pos) {
        var block = mc.world.getBlockState(pos).getBlock();
        var above = mc.world.getBlockState(pos.up()).getBlock();
        return (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) && above == Blocks.AIR &&
               mc.world.getOtherEntities(null, new Box(pos.up())).isEmpty();
    }

    // Упрощённый расчёт урона
    private double getExplosionDamage(BlockPos crystalPos, Entity target) {
        double dist = target.getPos().distanceTo(Vec3d.ofCenter(crystalPos.up()));
        if (dist > 10) return 0;
        double damage = 12.0 * (1.0 - dist / 10.0);
        return Math.max(0, damage);
    }

    private void switchToCrystal() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                mc.player.getInventory().selectedSlot = i;
                break;
            }
        }
    }

    private void placeCrystal(BlockPos pos) {
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
            new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
    }

    private void explodeCrystal(BlockPos pos) {
        for (Entity e : mc.world.getOtherEntities(null, new Box(pos.up()).expand(1))) {
            if (e instanceof EndCrystalEntity) {
                mc.interactionManager.attackEntity(mc.player, e);
                break;
            }
        }
    }
}
