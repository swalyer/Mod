package com.example.arcanomech.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface ImplementedInventory extends Inventory {
    DefaultedList<ItemStack> getItems();

    static ImplementedInventory of(DefaultedList<ItemStack> items) {
        return new ImplementedInventory() {
            @Override
            public DefaultedList<ItemStack> getItems() {
                return items;
            }
        };
    }

    static ImplementedInventory ofSize(int size) {
        return of(DefaultedList.ofSize(size, ItemStack.EMPTY));
    }

    @Override
    default int size() { return getItems().size(); }

    @Override
    default boolean isEmpty() {
        for (ItemStack s : getItems()) if (!s.isEmpty()) return false;
        return true;
    }

    @Override
    default ItemStack getStack(int slot) { return getItems().get(slot); }

    @Override
    default ItemStack removeStack(int slot, int amount) {
        ItemStack res = Inventories.splitStack(getItems(), slot, amount);
        if (!res.isEmpty()) markDirty();
        return res;
    }

    @Override
    default ItemStack removeStack(int slot) {
        return Inventories.removeStack(getItems(), slot);
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    default void clear() { getItems().clear(); }

    @Override
    default void markDirty() { }

    // ВАЖНО: даём дефолт иначе интерфейс остаётся не-функциональным
    @Override
    default boolean canPlayerUse(PlayerEntity player) { return true; }
}
