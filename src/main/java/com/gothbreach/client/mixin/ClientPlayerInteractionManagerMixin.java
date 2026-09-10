package com.gothbreach.client.mixin;

import com.gothbreach.client.CheatClient;
import com.gothbreach.client.module.modules.combat.Reach;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    private void onGetReachDistance(CallbackInfoReturnable<Float> cir) {
        Reach reach = (Reach) CheatClient.moduleManager.get("Reach");
        if (reach != null && reach.isEnabled()) {
            cir.setReturnValue((float) reach.reachDistance);
        }
    }

    @Inject(method = "hasExtendedReach", at = @At("HEAD"), cancellable = true)
    private void onHasExtendedReach(CallbackInfoReturnable<Boolean> cir) {
        Reach reach = (Reach) CheatClient.moduleManager.get("Reach");
        if (reach != null && reach.isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}