package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class AutoTotem extends Module {
    @Setting(name="Порог здоровья", min=1, max=20, decimalPlaces=1)
    public double healthThreshold = 10.0;

    @Setting(name="Предсказывать урон")
    public boolean predictDamage = true;

    @Setting(name="Задержка между свапами (мс)", min=100, max=1000)
    public int swapDelay = 200;

    @Setting(name="Всегда держать тотем")
    public boolean alwaysTotem = true;

    private long lastSwap = 0;
    private float lastHealth = 20f;

    public AutoTotem() {
        super("AutoTotem", "Автоматически держит тотем в левой руке", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;

        float health = mc.player.getHealth();
        float predictedDamage = predictDamage ? (lastHealth - health) : 0;
        lastHealth = health;

        boolean needSwap = alwaysTotem 
            || (predictDamage && health - predictedDamage <= 0) 
            || health <= healthThreshold;

        if (needSwap && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            if (System.currentTimeMillis() - lastSwap >= swapDelay) {
                swapToTotem();
                lastSwap = System.currentTimeMillis();
            }
        }
    }

    private void swapToTotem() {
        int totemSlot = findTotemInInventory();
        if (totemSlot == -1) return;

        // Запоминаем текущий предмет в offhand
        ItemStack offhandStack = mc.player.getOffHandStack().copy();
        ItemStack totemStack = new ItemStack(Items.TOTEM_OF_UNDYING);

        // Обновляем локальный инвентарь (для мгновенного эффекта)
        mc.player.getInventory().setStack(40, totemStack.copy());
        mc.player.getInventory().setStack(totemSlot, offhandStack.copy());

        // Отправляем пакет на сервер (как в Meteor)
        int syncId = mc.player.currentScreenHandler.syncId;
        int revision = mc.player.currentScreenHandler.getRevision();
        Int2ObjectMap<ItemStack> modifiedStacks = new Int2ObjectArrayMap<>();
        modifiedStacks.put(40, totemStack);
        modifiedStacks.put(totemSlot, offhandStack);

        ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
            syncId,
            revision,
            40,                // слот offhand
            totemSlot,         // слот с тотемом
            SlotActionType.SWAP,
            totemStack.copy(),
            modifiedStacks
        );
        mc.player.networkHandler.sendPacket(packet);
    }

    private int findTotemInInventory() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }
}