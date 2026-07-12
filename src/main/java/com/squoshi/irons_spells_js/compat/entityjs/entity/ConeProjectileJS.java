package com.squoshi.irons_spells_js.compat.entityjs.entity;

import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.ConeProjectileJSBuilder;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IProjectileEntityJS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.HashMap;
import java.util.Map;

public class ConeProjectileJS extends AbstractConeProjectile implements IProjectileEntityJS {
    public ConeProjectileJSBuilder builder;
    public Map<String, Object> customData = new HashMap<>();

    public ConeProjectileJS(ConeProjectileJSBuilder builder, EntityType<? extends AbstractConeProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
    }

    @Override
    public BaseNonAnimatableEntityBuilder<?> getProjectileBuilder() {
        return builder;
    }

    public float getDamage() {
        return damage;
    }

    @Override
    public void spawnParticles() {
        if (builder.spawnParticles != null) {
            ISSKJSUtils.safeCallback(builder.spawnParticles, this, "Error while calling spawnParticles");
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (builder.onConeHitEntity != null) {
            ISSKJSUtils.safeCallback(builder.onConeHitEntity, new ConeProjectileJSBuilder.onHitEntityContext(entityHitResult, this), "Error while calling onConeHitEntity");
        }
    }
}
