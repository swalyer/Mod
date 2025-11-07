package com.example.arcanomech.magic;

import net.minecraft.item.ItemStack;

public interface ManaToolItem {
    int getMana(ItemStack stack);

    int getCapacity(ItemStack stack);

    int insertMana(ItemStack stack, int amount, boolean simulate);

    int extractMana(ItemStack stack, int amount);
}
