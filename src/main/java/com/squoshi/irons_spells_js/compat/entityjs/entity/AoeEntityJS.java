package com.squoshi.irons_spells_js.compat.entityjs.entity;

import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.AoeEntityJSBuilder;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IProjectileEntityJS;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AoeEntityJS extends AoeEntity implements IProjectileEntityJS {
    public AoeEntityJSBuilder builder;
    public Map<String, Object> customData = new HashMap<>();

    public AoeEntityJS(AoeEntityJSBuilder builder, EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;

    }


    @Override
    public void applyEffect(LivingEntity target) {
        if (builder.applyEffect != null) {
            ISSKJSUtils.safeCallback(builder.applyEffect, new AoeEntityJSBuilder.applyEffectContext(target, this), "Error while calling applyEffect");
        }
    }

    @Override
    public float getParticleCount() {
        if (builder.getParticleCount != null) {
            return builder.getParticleCount.apply(this).floatValue();
        }
        return 0;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        if (builder.getParticle != null) {
            return Optional.of(builder.getParticle.apply(this));
        }
        return Optional.empty();
    }

    @Override
    public BaseNonAnimatableEntityBuilder<?> getProjectileBuilder() {
        return builder;
    }
}
