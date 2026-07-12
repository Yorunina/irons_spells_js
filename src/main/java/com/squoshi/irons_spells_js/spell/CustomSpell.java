package com.squoshi.irons_spells_js.spell;

import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CustomSpell extends AbstractSpell {
    record CastContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, CastSource getCastSource, MagicData getPlayerMagicData, AbstractSpell getSpell){}
    record CastClientContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, ICastData getCastData){}

    record PreCastContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, MagicData getPlayerMagicData){}
    record PreCastClientContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, InteractionHand getHand, MagicData getPlayerMagicData){}
    record PreCastConditionsContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, MagicData getPlayerMagicData, AbstractSpell getSpell){}
    record RecastFinishedContext(ServerPlayer getServerPlayer, RecastInstance getRecastInstance, RecastResult getRecastResult, ICastDataSerializable getCastDataSerializable, AbstractSpell getSpell){}
    record DamageSourceContext(Entity getProjectile, Entity getAttacker, AbstractSpell getSpell){}
    record EffectiveCastTimeContext(int getSpellLevel, LivingEntity getEntity, AbstractSpell getSpell){}


    private final ResourceLocation spellResource;
    private final DefaultConfig defaultConfig;
    private final CastType castType;
    private final ISSKJSUtils.SoundEventHolder startSound, finishSound;
    private final Consumer<CastContext> onCast;
    private final Consumer<CastClientContext> onClientCast;
    private final Consumer<PreCastContext> onPreCast;
    private final Consumer<PreCastClientContext> onPreClientCast;
    private final boolean allowLooting;
    private final Predicate<Player> canBeCrafted;
    private final ICastDataSerializable emptyCastData;
    private final boolean needsLearning;
    private final BiFunction<Integer, LivingEntity, List<MutableComponent>> uniqueInfo;
    private final AnimationHolder castStartAnimation;
    private final AnimationHolder castFinishAnimation;
    private final Predicate<PreCastConditionsContext> preCastConditions;
    private final BiFunction<Integer, LivingEntity, Double> getRecastCount;
    private final Consumer<RecastFinishedContext> onRecastFinished;
    private final Function<DamageSourceContext, SpellDamageSource> damageSource;
    private final Function<EffectiveCastTimeContext, Double> effectiveCastTime;
    private final boolean stopSoundOnCancel;

    public CustomSpell(Builder b) {
        this.spellResource = b.spellResource;
        this.defaultConfig = new DefaultConfig()
                .setMinRarity(b.minRarity)
                .setSchoolResource(b.school)
                .setMaxLevel(b.maxLevel)
                .setCooldownSeconds(b.cooldownSeconds)
                .build();
        this.castType = b.castType;
        this.startSound = b.startSound;
        this.finishSound = b.finishSound;
        this.onCast = b.onCast;
        this.onClientCast = b.onClientCast;
        this.onPreCast = b.onPreCast;
        this.onPreClientCast = b.onPreClientCast;
        this.manaCostPerLevel = b.manaCostPerLevel;
        this.baseSpellPower = b.baseSpellPower;
        this.spellPowerPerLevel = b.spellPowerPerLevel;
        this.castTime = b.castTime;
        this.baseManaCost = b.baseManaCost;
        this.allowLooting = b.allowLooting;
        this.needsLearning = b.needsLearning;
        this.emptyCastData = b.emptyCastData;
        this.canBeCrafted = b.canBeCrafted;
        this.uniqueInfo = b.uniqueInfo;
        this.castStartAnimation = b.castStartAnimation;
        this.castFinishAnimation = b.castFinishAnimation;
        this.preCastConditions = b.preCastConditions;
        this.getRecastCount = b.getRecastCount;
        this.onRecastFinished = b.onRecastFinished;
        this.damageSource = b.damageSource;
        this.effectiveCastTime = b.effectiveCastTime;
        this.stopSoundOnCancel = b.stopSoundOnCancel;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellResource;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return castType;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return startSound != null ? Optional.ofNullable(ForgeRegistries.SOUND_EVENTS.getValue(startSound.getLocation())) : super.getCastStartSound();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return finishSound != null ? Optional.ofNullable(ForgeRegistries.SOUND_EVENTS.getValue(finishSound.getLocation())) : super.getCastFinishSound();
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (onRecastFinished != null) {
            var context = new RecastFinishedContext(serverPlayer, recastInstance, recastResult, castDataSerializable, this);
            ISSKJSUtils.safeCallback(onRecastFinished, context, "Error while calling onRecastFinished");
            return;
        }
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (onCast != null) {
            var context = new CastContext(level, spellLevel, entity, castSource, playerMagicData, this);
            ISSKJSUtils.safeCallback(onCast, context, "Error while calling onCast");
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        if (onClientCast != null) {
            var context = new CastClientContext(level, spellLevel, entity, castData);
            ISSKJSUtils.safeCallback(onClientCast, context, "Error while calling onClientCast");
        }
        super.onClientCast(level, spellLevel, entity, castData);
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (onPreCast != null) {
            var context = new PreCastContext(level, spellLevel, entity, playerMagicData);
            ISSKJSUtils.safeCallback(onPreCast, context, "Error while calling onPreCast");
        }
        super.onServerPreCast(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onClientPreCast(Level level, int spellLevel, LivingEntity entity, InteractionHand hand, MagicData playerMagicData) {
        if (onPreClientCast != null) {
            var context = new PreCastClientContext(level, spellLevel, entity, hand, playerMagicData);
            ISSKJSUtils.safeCallback(onPreClientCast, context, "Error while calling onPreClientCast");
        }
        super.onClientPreCast(level, spellLevel, entity, hand, playerMagicData);
    }

    @Override
    public boolean allowLooting() {
        return allowLooting;
    }

    @Override
    public boolean requiresLearning() {
        return needsLearning;
    }

    public ICastDataSerializable getEmptyCastData() {
        return this.emptyCastData;
    }

    @Override
    public boolean canBeCraftedBy(Player player) {
        if (canBeCrafted != null)
            return canBeCrafted.test(player);
        return true;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        if (this.uniqueInfo != null) {
            return this.uniqueInfo.apply(spellLevel, caster);
        }
        return super.getUniqueInfo(spellLevel, caster);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        if (castStartAnimation != null) {
            return castStartAnimation;
        }
        return super.getCastStartAnimation();
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        if (castFinishAnimation != null) {
            return castFinishAnimation;
        }
        return super.getCastFinishAnimation();
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (this.preCastConditions != null) {
            return this.preCastConditions.test(new PreCastConditionsContext(level, spellLevel, entity, playerMagicData, this));
        }
        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        if (getRecastCount != null) {
            return getRecastCount.apply(spellLevel, entity).intValue();
        }
        return super.getRecastCount(spellLevel, entity);
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        if (this.damageSource != null) {
            return this.damageSource.apply(new DamageSourceContext(projectile, attacker, this));
        }
        return super.getDamageSource(projectile, attacker);
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        if (this.effectiveCastTime != null) {
            return this.effectiveCastTime.apply(new EffectiveCastTimeContext(spellLevel, entity, this)).intValue();
        }
        return super.getEffectiveCastTime(spellLevel, entity);
    }

    @Override
    public boolean stopSoundOnCancel() {
        return stopSoundOnCancel;
    }


    @SuppressWarnings("unused")
    public static class Builder extends BuilderBase<CustomSpell> {
        private SpellRarity minRarity = SpellRarity.COMMON;
        private ResourceLocation school = SchoolRegistry.BLOOD_RESOURCE;
        private int maxLevel = 10;
        private int cooldownSeconds = 20;
        private CastType castType = CastType.INSTANT;
        private ISSKJSUtils.SoundEventHolder startSound = null;
        private ISSKJSUtils.SoundEventHolder finishSound = null;
        private final ResourceLocation spellResource;
        private Consumer<CastContext> onCast = null;
        private Consumer<CastClientContext> onClientCast = null;
        private Consumer<PreCastContext> onPreCast = null;
        private Consumer<PreCastClientContext> onPreClientCast = null;
        private int manaCostPerLevel = 20;
        private int baseSpellPower = 0;
        private int spellPowerPerLevel = 1;
        private int castTime = 0;
        private int baseManaCost = 40;
        private boolean allowLooting = false;
        private ICastDataSerializable emptyCastData = null;
        private Predicate<Player> canBeCrafted = null;
        private boolean needsLearning = false;
        private BiFunction<Integer,LivingEntity,List<MutableComponent>> uniqueInfo;
        private AnimationHolder castStartAnimation = null;
        private AnimationHolder castFinishAnimation = null;
        private Predicate<PreCastConditionsContext> preCastConditions = null;
        private BiFunction<Integer, LivingEntity, Double> getRecastCount = null;
        private Consumer<RecastFinishedContext> onRecastFinished = null;
        private Function<DamageSourceContext, SpellDamageSource>  damageSource = null;
        private Function<EffectiveCastTimeContext, Double> effectiveCastTime = null;
        private boolean stopSoundOnCancel = false;

        public Builder(ResourceLocation i) {
            super(i);
            this.spellResource = i;
        }

        @Info(value = """
            Sets the cast type. Can be `CONTINUOUS`, `INSTANT`, `LONG`, or `NONE`.
        """)
        public Builder setCastType(CastType type) {
            this.castType = type;
            return this;
        }

        @Info(value = """
            Sets the sound that the spell will play when it starts casting.
        """)
        public Builder setStartSound(ISSKJSUtils.SoundEventHolder soundEvent) {
            this.startSound = soundEvent;
            return this;
        }

        @Info(value = """
            Sets the recast count of the spell. The recast count is the amount of time it takes for the spell to be cast again.
        """)
        public Builder setRecastCount(BiFunction<Integer, LivingEntity, Double> getRecastCount) {
            this.getRecastCount = getRecastCount;
            return this;
        }

        @Info(value = """
            Sets the recast count of the spell. The recast count is the amount of time it takes for the spell to be cast again.
        """)
        public Builder onRecastFinished(Consumer<RecastFinishedContext> onRecastFinished) {
            this.onRecastFinished = onRecastFinished;
            return this;
        }

        @Info(value = """
            Sets the empty cast data of the spell. The empty cast data is the data that is used to store the summoned entities.
        """)
        public Builder setEmptyCastData(ICastDataSerializable emptyCastData) {
            this.emptyCastData = emptyCastData;
            return this;
        }



        @Info(value = """
            Sets the sound that the spell will play after it is done casting.
        """)
        public Builder setFinishSound(ISSKJSUtils.SoundEventHolder soundEvent) {
            this.finishSound = soundEvent;
            return this;
        }

        @Info(value = """
            Sets the rarity of the spell. Can be `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, or `LEGENDARY`.
        """)
        public Builder setMinRarity(SpellRarity rarity) {
            this.minRarity = rarity;
            return this;
        }

        @Info(value = """
            Sets the school of the spell. The different schools each are a resource location.
            
            Example: `.setSchool(SchoolRegistry.BLOOD_RESOURCE`
            Another example: `setSchool('irons_spellbooks:blood')`
        """)
        public Builder setSchool(ISSKJSUtils.SchoolHolder schoolHolder) {
            this.school = schoolHolder.getLocation();
            return this;
        }

        @Info(value = """
            Sets the max level of the spell. Goes up to `10` from `1`.
        """)
        public Builder setMaxLevel(int level) {
            this.maxLevel = level;
            return this;
        }

        @Info(value = """
            Sets the cooldown of the spell in seconds. Cannot be a decimal value for some reason.
        """)
        public Builder setCooldownSeconds(int seconds) {
            this.cooldownSeconds = seconds;
            return this;
        }

        @Info(value = """
            Sets the mana cost per the spell's level. For example, you could input `10` into this method, and each level of the spell will multiply that value by the level.
        """)
        public Builder setManaCostPerLevel(int cost) {
            this.manaCostPerLevel = cost;
            return this;
        }

        @Info(value = """
            Sets the base spell power. Can be from `1` to `10`. The spell power per level adds on to this.
        """)
        public Builder setBaseSpellPower(int power) {
            this.baseSpellPower = power;
            return this;
        }

        @Info(value = """
            Sets the spell power per level.
        """)
        public Builder setSpellPowerPerLevel(int power) {
            this.spellPowerPerLevel = power;
            return this;
        }

        @Info(value = """
            Sets the cast time. This is used for `LONG` or `CONTINUOUS` spell types.
        """)
        public Builder setCastTime(int time) {
            this.castTime = time;
            return this;
        }

        @Info(value = """
            Sets the base mana cost. The mana cost per level adds on to this.
        """)
        public Builder setBaseManaCost(int cost) {
            this.baseManaCost = cost;
            return this;
        }

        @Info(value = """
            Sets the callback for when the spell is cast. This is what the spell does when it is casted.
        """)
        public Builder onCast(Consumer<CastContext> consumer) {
            this.onCast = consumer;
            return this;
        }

        @Info(value = """
            Sets the callback for when the spell is cast on the client side. This is what the spell does when it is casted.
        """)
        public Builder onClientCast(Consumer<CastClientContext> consumer) {
            this.onClientCast = consumer;
            return this;
        }

        @Info(value = """
            Sets the callback for when the spell is about to be cast. This is what the spell does before it is casted.
        """)
        public Builder onPreCast(Consumer<PreCastContext> consumer) {
            this.onPreCast = consumer;
            return this;
        }

        @Info(value = """
            Sets the callback for when the spell is about to be cast on the client side. This is what the spell does before it is casted.
        """)
        public Builder onPreClientCast(Consumer<PreCastClientContext> consumer) {
            this.onPreClientCast = consumer;
            return this;
        }

        @Info(value = """
            Sets whether or not the spell can be looted from a loot table.
        """)
        public Builder setAllowLooting(boolean allow) {
            this.allowLooting = allow;
            return this;
        }

        @Info(value = """
            Sets whether or not the spell needs to be learned before it can be casted.
        """)
        public Builder needsLearning(boolean needs) {
            this.needsLearning = needs;
            return this;
        }

        @Info(value = """
            Sets the predicate for whether or not the spell can be crafted by a player.
        """)
        public Builder canBeCraftedBy(Predicate<Player> predicate) {
            this.canBeCrafted = predicate;
            return this;
        }

        @Info(value = """
            Sets the unique info for the spell. It is what is displayed on the spell in-game, e.g how some spells have damage values listed.
        """)
        public Builder setUniqueInfo(BiFunction<Integer, LivingEntity, List<MutableComponent>> info) {
            this.uniqueInfo = info;
            return this;
        }

        @Info(value = """
            Sets the cast start animation for the spell.
        """)
        public Builder setCastStartAnimation(String path, boolean playOnce, boolean animatesLegs) {
            var rl = path.contains(":") ? ResourceLocation.tryParse(path) : IronsSpellbooks.id(path);
            this.castStartAnimation = new AnimationHolder(rl, playOnce, animatesLegs);
            return this;
        }

        @Info(value = """
            Sets the cast finish animation for the spell.
        """)
        public Builder setCastFinishAnimation(String path, boolean playOnce, boolean animatesLegs) {
            var rl = path.contains(":") ? ResourceLocation.tryParse(path) : IronsSpellbooks.id(path);
            this.castFinishAnimation = new AnimationHolder(rl, playOnce, animatesLegs);
            return this;
        }

        @Info(value = """
            Sets the pre-cast conditions for the spell. It is a Predicate, which means it requires a boolean return value. This can be used for targeting spells and for cancelling the spell before it is casted.
            
            Example: ```js
            .checkPreCastConditions(ctx => {
                return ISSUtils.preCastTargetHelper(ctx.level, ctx.entity, ctx.playerMagicData, ctx.spell, 48, 0.35)
            })
            ```
        """)
        public Builder checkPreCastConditions(Predicate<PreCastConditionsContext> predicate) {
            this.preCastConditions = predicate;
            return this;
        }

        @Override
        public RegistryInfo<AbstractSpell> getRegistryType() {
            return IronsSpellsJSPlugin.SPELL_REGISTRY;
        }

        @Override
        public CustomSpell createObject() {
            return new CustomSpell(this);
        }


        public Builder getDamageSource(Function<DamageSourceContext, SpellDamageSource> damageSource) {
            this.damageSource = damageSource;
            return this;
        }

        public Builder getEffectiveCastTime(Function<EffectiveCastTimeContext, Double> effectiveCastTime) {
            this.effectiveCastTime = effectiveCastTime;
            return this;
        }

        @Info(value = """
            Sets whether the casting sound should stop when the spell is cancelled.
        """)
        public Builder setStopSoundOnCancel(boolean stopSoundOnCancel) {
            this.stopSoundOnCancel = stopSoundOnCancel;
            return this;
        }
    }
}