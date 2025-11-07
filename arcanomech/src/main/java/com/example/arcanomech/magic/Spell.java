package com.example.arcanomech.magic;

import net.minecraft.util.Identifier;

public interface Spell {
    Identifier getId();

    boolean cast(SpellContext context, SpellConfig config);
}
