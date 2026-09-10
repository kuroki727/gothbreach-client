package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public class AutoTrap extends Module {
    @Setting(name="Радиус", min=1, max=4)
    public int radius = 2;

    @Setting(name="Блок", values={"Обсидиан","Плачущий обсидиан","Эндер-сундук"})
    public String blockType = "Обсидиан";

    @Setting(name="Верхняя крышка")
    public boolean top = true;

    public AutoTrap() {
        super("AutoTrap", "Автоматически зажимает врага обсидианом", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        PlayerEntity target = getNearestPlayer();
        if (target == null) return;
        BlockPos center = target.getBlockPos();

        for (BlockPos pos : BlockPos.iterateOutwards(center, radius, radius, radius)) {
            if (pos.equals(center)) continue;
            if (!top && pos.getY() > center.getY()) continue;
            if (mc.world.getBlockState(pos).isAir()) {
                placeBlock(pos, Direction.UP);
                return;
            }
        }
    }

    private PlayerEntity getNearestPlayer() {
        return mc.world.getPlayers().stream()
            .filter(p -> p != mc.player && p.isAlive() && p.distanceTo(mc.player) < 10)
            .min((a,b) -> Double.compare(a.distanceTo(mc.player), b.distanceTo(mc.player)))
            .orElse(null);
    }

    private void placeBlock(BlockPos pos, Direction facing) {
        int slot = findBlock();
        if (slot == -1) return;
        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
            new BlockHitResult(Vec3d.ofCenter(pos), facing, pos, false));
        mc.player.getInventory().selectedSlot = prev;
    }

    private int findBlock() {
        var item = switch (blockType) {
            case "Плачущий обсидиан" -> Items.CRYING_OBSIDIAN;
            case "Эндер-сундук" -> Items.ENDER_CHEST;
            default -> Items.OBSIDIAN;
        };
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }
}
