package com.gothbreach.client.mixin;

import com.gothbreach.client.CheatClient;
import com.gothbreach.client.module.modules.movement.NoSlow;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    // Отменяет замедление от еды, щита, лука и т.д.
    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void onIsUsingItem(CallbackInfoReturnable<Boolean> cir) {
        NoSlow noSlow = (NoSlow) CheatClient.moduleManager.get("NoSlow");
        if (noSlow != null && noSlow.items()) {
            cir.setReturnValue(false);
        }
    }

    // Отменяет замедление от приседа
    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    private void onShouldSlowDown(CallbackInfoReturnable<Boolean> cir) {
        NoSlow noSlow = (NoSlow) CheatClient.moduleManager.get("NoSlow");
        if (noSlow != null && noSlow.sneaking()) {
            cir.setReturnValue(false);
        }
    }
}