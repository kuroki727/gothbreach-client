package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class HoleFill extends Module {
    @Setting(name="Радиус", min=1, max=6)
    public int radius = 3;

    @Setting(name="Блок", values={"Обсидиан","Плачущий обсидиан"})
    public String blockType = "Обсидиан";

    public HoleFill() {
        super("HoleFill", "Заполняет дыры вокруг врага", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        var target = mc.world.getPlayers().stream()
            .filter(p -> p != mc.player && p.isAlive() && p.distanceTo(mc.player) < 10)
            .min((a,b) -> Double.compare(a.distanceTo(mc.player), b.distanceTo(mc.player)))
            .orElse(null);
        if (target == null) return;

        List<BlockPos> holes = findHolesAround(target.getBlockPos());
        for (BlockPos hole : holes) {
            if (mc.world.getBlockState(hole).isAir()) {
                placeBlock(hole, Direction.UP);
                break;
            }
        }
    }

    private List<BlockPos> findHolesAround(BlockPos center) {
        List<BlockPos> holes = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = center.add(dx, 0, dz);
                if (isHole(pos)) holes.add(pos);
            }
        }
        return holes;
    }

    private boolean isHole(BlockPos pos) {
        return mc.world.getBlockState(pos).isAir() &&
               mc.world.getBlockState(pos.down()).getBlock() != Blocks.AIR &&
               mc.world.getBlockState(pos.north()).getBlock() != Blocks.AIR &&
               mc.world.getBlockState(pos.south()).getBlock() != Blocks.AIR &&
               mc.world.getBlockState(pos.east()).getBlock() != Blocks.AIR &&
               mc.world.getBlockState(pos.west()).getBlock() != Blocks.AIR;
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
        var item = blockType.equals("Плачущий обсидиан") ? Items.CRYING_OBSIDIAN : Items.OBSIDIAN;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }
}
