package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoPearl extends Module {
    @Setting(name="Порог высоты", min=1, max=200)
    public int yThreshold = 5;

    @Setting(name="Только при падении")
    public boolean onlyFalling = true;

    public AutoPearl() {
        super("AutoPearl", "Кидает жемчуг при падении", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        boolean falling = mc.player.getVelocity().y < -0.5;
        if (mc.player.getY() < yThreshold && (!onlyFalling || falling)) {
            int slot = findPearl();
            if (slot != -1) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = slot;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = prev;
            }
        }
    }

    private int findPearl() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) return i;
        }
        return -1;
    }
}
