package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Burrow extends Module {
    @Setting(name="Блок", values={"Обсидиан","Эндер-сундук"})
    public String blockType = "Обсидиан";

    @Setting(name="Мгновенный")
    public boolean instant = false;

    public Burrow() {
        super("Burrow", "Закапывает вас в блок", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos pos = mc.player.getBlockPos();
        if (!mc.world.getBlockState(pos).isAir()) return;

        int slot = findBlock();
        if (slot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        if (instant) {
            // Быстрое закапывание
            mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 0.5, mc.player.getZ());
        }

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
            new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
        mc.player.getInventory().selectedSlot = prev;
    }

    private int findBlock() {
        var item = blockType.equals("Эндер-сундук") ? Items.ENDER_CHEST : Items.OBSIDIAN;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }
}
