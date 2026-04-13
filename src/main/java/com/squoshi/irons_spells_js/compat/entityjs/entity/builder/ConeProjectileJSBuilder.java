package com.squoshi.irons_spells_js.compat.entityjs.entity.builder;

import com.squoshi.irons_spells_js.compat.entityjs.entity.ConeProjectileJS;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.EntityHitResult;

import java.util.function.Consumer;

public class ConeProjectileJSBuilder extends ProjectileEntityBuilder<ConeProjectileJS> {
    public record onHitEntityContext(EntityHitResult getEntityHitResult, AbstractConeProjectile getEntity){}
    public transient Consumer<AbstractConeProjectile> spawnParticles;
    public transient Consumer<onHitEntityContext> onConeHitEntity;
    public ConeProjectileJSBuilder(ResourceLocation i) {
        super(i);
    }

    public ConeProjectileJSBuilder spawnParticles(Consumer<AbstractConeProjectile> spawnParticles) {
        this.spawnParticles = spawnParticles;
        return this;
    }

    public ConeProjectileJSBuilder onConeHitEntity(Consumer<onHitEntityContext> onConeHitEntity) {
        this.onConeHitEntity = onConeHitEntity;
        return this;
    }

    @Override
    public EntityType.EntityFactory<ConeProjectileJS> factory() {
        return (type, level) -> new ConeProjectileJS(this, type, level);
    }

}
