package com.example.arcanomech.magic.spells;

import com.example.arcanomech.magic.Spell;
import com.example.arcanomech.magic.SpellConfig;
import com.example.arcanomech.magic.SpellContext;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;

public final class WardSpell implements Spell {
    private final Identifier id;

    public WardSpell(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public boolean cast(SpellContext context, SpellConfig config) {
        int duration = Math.max(1, config.getInt("duration", 200));
        int absorption = Math.max(0, config.getInt("absorption", 4));
        if (absorption <= 0) {
            return false;
        }
        context.caster().addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, duration, Math.max(0, (absorption / 2) - 1), false, true));
        return true;
    }
}
