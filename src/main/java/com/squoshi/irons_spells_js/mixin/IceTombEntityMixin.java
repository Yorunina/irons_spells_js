package com.squoshi.irons_spells_js.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.ironsspellbooks.entity.spells.ice_tomb.IceTombEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(IceTombEntity.class)
public abstract class IceTombEntityMixin {
    @Shadow
    private float health;

    @Unique
    private float damageAmplifier = 2;

    @Unique
    public void setHealth(float health) {
        this.health = health;
    }

    @Unique
    public float getHealth() {
        return this.health;
    }

    @Unique
    public void setDamageAmplifier(float damageAmplifier) {
        this.damageAmplifier = damageAmplifier;
    }

    @Unique
    public float getDamageAmplifier() {
        return this.damageAmplifier;
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"), cancellable = true, remap = false)
    private void dieInject(DamageSource damageSource, float amount, CallbackInfo ci, @Local(name = "entities") List<Entity> entities) {
        for (Entity entity : entities) {
            entity.hurt(damageSource, amount * damageAmplifier);
        }
        ci.cancel();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveData(CompoundTag pCompound, CallbackInfo ci) {
        pCompound.putFloat("DamageAmplifier", this.damageAmplifier);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readData(CompoundTag pCompound, CallbackInfo ci) {
        this.damageAmplifier = pCompound.getFloat("DamageAmplifier");
    }
}
