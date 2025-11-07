package com.example.arcanomech.magic;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public interface ManaToolItem {
    void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip);

    int getMana(ItemStack stack);

    int getCapacity(ItemStack stack);

    int insertMana(ItemStack stack, int amount, boolean simulate);

    int extractMana(ItemStack stack, int amount);
}
