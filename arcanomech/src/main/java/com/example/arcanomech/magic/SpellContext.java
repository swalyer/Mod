package com.example.arcanomech.magic;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public record SpellContext(World world, PlayerEntity caster, ItemStack focus, CastingSource source) {
    public boolean isServer() {
        return world != null && !world.isClient;
    }
}
