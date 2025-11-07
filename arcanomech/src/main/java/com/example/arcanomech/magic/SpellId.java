package com.example.arcanomech.magic;

import net.minecraft.util.Identifier;

public record SpellId(Identifier id) {
    public SpellId {
        if (id == null) {
            throw new IllegalArgumentException("Spell identifier cannot be null");
        }
    }

    public SpellId(String value) {
        this(new Identifier(value));
    }

    public static SpellId of(String value) {
        return new SpellId(value);
    }

    public String asString() {
        return id.toString();
    }
}
