package com.squoshi.irons_spells_js.mixin;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHole;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlackHole.class, priority = 500)
public abstract class BlackHoleMixin extends Projectile {
    @Unique
    private boolean percentageDamage = false;

    @Unique
    private boolean killOnEnd = false;

    @Unique
    private boolean ignoreProjectileProtection = false;

    protected BlackHoleMixin(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }

    @Unique
    public void setPercentageDamage(boolean isPercentageDamage) {
        this.percentageDamage = isPercentageDamage;
    }

    @Unique
    public void setKillOnEnd(boolean kill) {
        this.killOnEnd = kill;
    }

    @Unique
    public void ignoreProjectileProtection(boolean ignore) {
        this.ignoreProjectileProtection = ignore;
    }

    @WrapOperation(method = "handleGravity", at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/damage/DamageSources;applyDamage(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z"), remap = false)
    private boolean applyPercentageDamage(Entity target, float baseAmount, DamageSource damageSource, Operation<Boolean> original) {
        if (this.percentageDamage && target instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
            baseAmount = baseAmount * livingEntity.getMaxHealth();
            return target.hurt(target.damageSources().magic(), baseAmount);
        }
        return original.call(target, baseAmount, damageSource);
    }


    @Inject(method = "handleClientEffects", at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/entity/spells/black_hole/BlackHole;discard()V"))
    private void ironsSpellsJs$killEntitiesOnEnd(Vec3 center, CallbackInfo ci) {
        if (!this.killOnEnd) return;

        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(1.0D))) {
            if (entity.distanceToSqr(center) < (double)9.0F && entity instanceof LivingEntity living && living.isAlive() && !entity.isSpectator()) {
                entity.kill();
            }
        }
    }

    @WrapOperation(method = "handleGravity", at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/entity/spells/black_hole/BlackHole;canHitEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean canHitEntity(BlackHole instance, Entity entity, Operation<Boolean> original) {
        if (this.ignoreProjectileProtection) {
            Entity owner = this.getOwner();
            return owner == null || this.leftOwner || !owner.isPassengerOfSameVehicle(entity);
        }
        return original.call(instance, entity);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveData(CompoundTag pCompound, CallbackInfo ci) {
        pCompound.putBoolean("KillOnEnd", this.killOnEnd);
        pCompound.putBoolean("PercentageDamage", this.percentageDamage);
        pCompound.putBoolean("IgnoreProjectileProtection", this.ignoreProjectileProtection);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readData(CompoundTag pCompound, CallbackInfo ci) {
        this.killOnEnd = pCompound.getBoolean("KillOnEnd");
        this.percentageDamage = pCompound.getBoolean("PercentageDamage");
        this.ignoreProjectileProtection = pCompound.getBoolean("IgnoreProjectileProtection");
    }
}
