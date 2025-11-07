package com.example.arcanomech.energy;

import net.minecraft.block.entity.BlockEntity;

public interface SideConfigHolder {
    SideConfig getSideConfig();

    default void onSideConfigChanged() {
        if (this instanceof BlockEntity blockEntity) {
            blockEntity.markDirty();
            if (blockEntity.getWorld() != null && !blockEntity.getWorld().isClient) {
                blockEntity.getWorld().updateListeners(blockEntity.getPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), 3);
            }
        }
    }
}
