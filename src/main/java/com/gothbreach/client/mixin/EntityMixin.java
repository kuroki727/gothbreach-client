package com.gothbreach.client.mixin;

import com.gothbreach.client.CheatClient;
import com.gothbreach.client.module.modules.movement.NoSlow;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getVelocityMultiplier", at = @At("HEAD"), cancellable = true)
    private void onGetVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        NoSlow noSlow = (NoSlow) CheatClient.moduleManager.get("NoSlow");
        if (noSlow == null || !noSlow.isEnabled()) return;
        Entity self = (Entity)(Object)this;
        if (self == CheatClient.mc.player) {
            var block = CheatClient.mc.world.getBlockState(self.getBlockPos()).getBlock();
            if (block == Blocks.SOUL_SAND && noSlow.soulSand()) { cir.setReturnValue(1.0f); return; }
            if (block == Blocks.HONEY_BLOCK && noSlow.honeyBlock()) { cir.setReturnValue(1.0f); return; }
            if (block == Blocks.SLIME_BLOCK && noSlow.slimeBlock()) { cir.setReturnValue(1.0f); return; }
            if (block == Blocks.COBWEB && noSlow.web()) { cir.setReturnValue(1.0f); return; }
        }
    }
}