package com.gothbreach.client.module.modules.combat;

import com.gothbreach.client.module.Module;
import com.gothbreach.client.module.Category;
import com.gothbreach.client.event.events.TickEvent;
import com.gothbreach.client.event.EventHandler;
import com.gothbreach.client.setting.Setting;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class AutoArmor extends Module {
    @Setting(name="Задержка (мс)", min=100, max=2000)
    public int swapDelay = 300;

    @Setting(name="Приоритет: защита")
    public boolean prioritizeProtection = true;

    @Setting(name="Учитывать зачарования")
    public boolean considerEnchantments = true;

    private long lastAction = 0;

    public AutoArmor() {
        super("AutoArmor", "Автоматически надевает лучшую броню", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;

        if (System.currentTimeMillis() - lastAction < swapDelay) return;

        for (int i = 0; i < 4; i++) {
            ItemStack current = mc.player.getInventory().getArmorStack(i);
            if (current.isEmpty() || !isBestArmor(current, i)) {
                int bestSlot = findBestArmor(i);
                if (bestSlot != -1) {
                    swapArmor(i, bestSlot);
                    // Увеличиваем задержку после обмена, чтобы сервер успел подтвердить
                    lastAction = System.currentTimeMillis() + 1000;
                    return;
                }
            }
        }
    }

    private boolean isBestArmor(ItemStack stack, int armorIndex) {
        int currentValue = getArmorValue(stack, armorIndex);
        for (int i = 0; i < 36; i++) {
            ItemStack invStack = mc.player.getInventory().getStack(i);
            if (invStack.getItem() instanceof ArmorItem armor && armor.getSlotType().getEntitySlotId() == armorIndex) {
                if (getArmorValue(invStack, armorIndex) > currentValue) return false;
            }
        }
        return true;
    }

    private int findBestArmor(int armorIndex) {
        int bestSlot = -1;
        int bestValue = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ArmorItem armor && armor.getSlotType().getEntitySlotId() == armorIndex) {
                int value = getArmorValue(stack, armorIndex);
                if (value > bestValue) {
                    bestValue = value;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    private void swapArmor(int armorIndex, int sourceSlot) {
        int destSlot = 36 + armorIndex; // 36=ботинки, 37=штаны, 38=грудь, 39=голова
        int syncId = mc.player.currentScreenHandler.syncId;
        int revision = mc.player.currentScreenHandler.getRevision();

        ItemStack sourceStack = mc.player.getInventory().getStack(sourceSlot).copy();
        ItemStack destStack = mc.player.getInventory().getArmorStack(armorIndex).copy();

        // Отправляем три пакета PICKUP (как в Meteor)
        sendPickupPacket(syncId, revision, sourceSlot, sourceStack);
        sendPickupPacket(syncId, revision, destSlot, destStack);
        sendPickupPacket(syncId, revision, sourceSlot, destStack);
    }

    private void sendPickupPacket(int syncId, int revision, int slot, ItemStack stack) {
        Int2ObjectMap<ItemStack> modifiedStacks = new Int2ObjectArrayMap<>();
        ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
            syncId,
            revision,
            slot,
            0, // button = 0 для левого клика
            SlotActionType.PICKUP,
            stack.copy(),
            modifiedStacks
        );
        mc.player.networkHandler.sendPacket(packet);
    }

    private int getArmorValue(ItemStack stack, int armorIndex) {
        if (stack.getItem() instanceof ArmorItem armor) {
            int base = prioritizeProtection ? armor.getProtection() : armor.getMaterial().getDurability(armor.getType());
            if (considerEnchantments) {
                int protLevel = net.minecraft.enchantment.EnchantmentHelper.getLevel(
                    net.minecraft.enchantment.Enchantments.PROTECTION, stack);
                base += protLevel * 2;
            }
            return base;
        }
        return -1;
    }
}