package com.squoshi.irons_spells_js.compat.entityjs;

import com.squoshi.irons_spells_js.IronsSpellsJSMod;
import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.AoeEntityJSBuilder;
import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.ConeProjectileJSBuilder;
import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellCastingMobJSBuilder;
import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellProjectileJSBuilder;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;

public class EntityJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        RegistryInfo.ENTITY_TYPE.addType(IronsSpellsJSMod.MODID + ":spellcasting", SpellCastingMobJSBuilder.class, SpellCastingMobJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType(IronsSpellsJSMod.MODID + ":spell_projectile", SpellProjectileJSBuilder.class, SpellProjectileJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType(IronsSpellsJSMod.MODID + ":aoe_entity", AoeEntityJSBuilder.class, AoeEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType(IronsSpellsJSMod.MODID + ":cone_projectile", ConeProjectileJSBuilder.class, ConeProjectileJSBuilder::new);
    }
}