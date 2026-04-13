package com.squoshi.irons_spells_js.compat.entityjs.entity.builder;

import com.squoshi.irons_spells_js.compat.entityjs.entity.AoeEntityJS;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;
import java.util.function.Function;

public class AoeEntityJSBuilder extends ProjectileEntityBuilder<AoeEntityJS> {
    public record applyEffectContext(LivingEntity getTarget, AoeEntity getEntity){}
    public transient Consumer<applyEffectContext> applyEffect;
    public transient Function<AoeEntity, ParticleOptions> getParticle;
    public transient Function<AoeEntity, Double> getParticleCount;

    public AoeEntityJSBuilder(ResourceLocation i) {
        super(i);
    }


    public AoeEntityJSBuilder applyEffect(Consumer<applyEffectContext> applyEffect) {
        this.applyEffect = applyEffect;
        return this;
    }

    public AoeEntityJSBuilder getParticleCount(Function<AoeEntity, Double> getParticleCount) {
        this.getParticleCount = getParticleCount;
        return this;
    }

    public AoeEntityJSBuilder getParticle(Function<AoeEntity, ParticleOptions> getParticle) {
        this.getParticle = getParticle;
        return this;
    }

    @Override
    public EntityType.EntityFactory<AoeEntityJS> factory() {
        return (type, level) -> new AoeEntityJS(this, type, level);
    }
}
