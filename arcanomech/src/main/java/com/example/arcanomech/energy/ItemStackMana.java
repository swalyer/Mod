package com.example.arcanomech.energy;

import net.minecraft.item.ItemStack;

public final class ItemStackMana implements ManaStorage {
    private final ItemStack stack;
    private final com.example.arcanomech.magic.ManaToolItem tool;

    public ItemStackMana(ItemStack stack, com.example.arcanomech.magic.ManaToolItem tool) {
        this.stack = stack;
        this.tool = tool;
    }

    @Override public int getMana() { return tool.getMana(stack); }
    @Override public int getCapacity() { return tool.getCapacity(stack); }
    @Override public int getIoPerTick() { return com.example.arcanomech.energy.Balance.WAND_IO_STEP; }

    @Override public int insert(int amount, boolean simulate) {
        if (amount <= 0) return 0;
        int before = tool.getMana(stack);
        int cap = tool.getCapacity(stack);
        int space = Math.max(0, cap - before);
        int ins = Math.min(space, amount);
        if (!simulate && ins > 0) tool.insertMana(stack, ins, false);
        return ins;
    }

    @Override public int extract(int amount, boolean simulate) {
        if (amount <= 0) return 0;
        int have = tool.getMana(stack);
        int ext = Math.min(have, amount);
        if (!simulate && ext > 0) tool.extractMana(stack, ext);
        return ext;
    }
}
