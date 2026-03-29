package com.squoshi.irons_spells_js.mixin;

import io.redspace.ironsspellbooks.player.AdditionalWanderingTrades;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdditionalWanderingTrades.class)
public class AdditionalWanderingTradesMixin {
    @Inject(method = "addWanderingTrades", at = @At(value = "HEAD"), cancellable = true)
    private static void addWanderingTrades(CallbackInfo ci){
        ci.cancel();
    }
}
