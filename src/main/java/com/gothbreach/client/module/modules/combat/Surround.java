package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public class Surround extends Module {
    @Setting(name="Радиус", min=1, max=3)
    public int radius = 1;

    @Setting(name="Блок", values={"Обсидиан","Плачущий обсидиан","Эндер-сундук"})
    public String blockType = "Обсидиан";

    @Setting(name="Поворот к блоку")
    public boolean rotate = true;

    @Setting(name="Центр", values={"Свой","Враг"})
    public String center = "Свой";

    public Surround() {
        super("Surround", "Обкладывает вас блоками для защиты", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos centerPos = getCenterPos();
        if (centerPos == null) return;

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;
            BlockPos pos = centerPos.offset(dir);
            if (mc.world.getBlockState(pos).isAir()) {
                if (placeBlock(pos, dir.getOpposite())) return;
            }
        }
    }

    private BlockPos getCenterPos() {
        if (center.equals("Враг")) {
            var target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive() && p.distanceTo(mc.player) < 8)
                .min((a,b) -> Double.compare(a.distanceTo(mc.player), b.distanceTo(mc.player)))
                .orElse(null);
            return target != null ? target.getBlockPos() : null;
        }
        return mc.player.getBlockPos();
    }

    private boolean placeBlock(BlockPos pos, Direction facing) {
        int slot = findBlockInHotbar();
        if (slot == -1) return false;
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        if (rotate) {
            // Поворот к блоку
            Vec3d target = Vec3d.ofCenter(pos);
            double dx = target.x - mc.player.getX();
            double dy = target.y - mc.player.getEyeY();
            double dz = target.z - mc.player.getZ();
            double dist = Math.sqrt(dx*dx + dz*dz);
            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
            new BlockHitResult(Vec3d.ofCenter(pos), facing, pos, false));
        mc.player.getInventory().selectedSlot = prevSlot;
        return true;
    }

    private int findBlockInHotbar() {
        var targetItem = switch (blockType) {
            case "Плачущий обсидиан" -> Items.CRYING_OBSIDIAN;
            case "Эндер-сундук" -> Items.ENDER_CHEST;
            default -> Items.OBSIDIAN;
        };
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == targetItem) return i;
        }
        return -1;
    }
}
