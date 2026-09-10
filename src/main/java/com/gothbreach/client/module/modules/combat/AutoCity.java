package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class AutoCity extends Module {
    @Setting(name="Радиус", min=1, max=6)
    public int radius = 2;

    @Setting(name="Только обсидиан")
    public boolean obsidianOnly = true;

    public AutoCity() {
        super("AutoCity", "Автоматически ломает блоки вокруг врага", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        PlayerEntity target = getNearestPlayer();
        if (target == null) return;

        for (BlockPos pos : BlockPos.iterateOutwards(target.getBlockPos(), radius, radius, radius)) {
            var block = mc.world.getBlockState(pos).getBlock();
            if ((obsidianOnly && block == Blocks.OBSIDIAN) || (!obsidianOnly && block != Blocks.AIR && block != Blocks.BEDROCK)) {
                mc.interactionManager.breakBlock(pos);
                mc.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }
    }

    private PlayerEntity getNearestPlayer() {
        return mc.world.getPlayers().stream()
            .filter(p -> p != mc.player && p.isAlive() && p.distanceTo(mc.player) < 10)
            .min((a,b) -> Double.compare(a.distanceTo(mc.player), b.distanceTo(mc.player)))
            .orElse(null);
    }
}
