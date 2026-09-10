package com.gothbreach.client.mixin;

import com.gothbreach.client.CheatClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void onIsUsingItem(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof PlayerEntity player &&
            player == CheatClient.mc.player &&
            CheatClient.moduleManager.get("NoSlow").isEnabled()) {
            // Не отменяем использование, но в других методах убираем замедление
        }
    }
}
