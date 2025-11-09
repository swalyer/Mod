package com.example.arcanomech.energy;

import com.example.arcanomech.content.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BatteryBlockEntity extends BlockEntity implements ManaStorage {
    private int mana;
    private final int capacity;
    private final int ioPerTick;

    public BatteryBlockEntity(BlockPos pos, BlockState state, int capacity, int ioPerTick) {
        super(ModBlockEntities.MANA_BATTERY, pos, state);
        this.capacity = capacity;
        this.ioPerTick = ioPerTick;
    }

    public static void tick(net.minecraft.world.World world, BlockPos pos, BlockState state, BatteryBlockEntity be) {
        if (world.isClient) return;
        boolean changed = false;
        for (Direction d : Direction.values()) {
            BlockPos np = pos.offset(d);
            BlockEntity nbe = world.getBlockEntity(np);
            if (!(nbe instanceof ManaStorage other)) continue;
            long a = pos.asLong();
            long b = np.asLong();
            if (a < b) {
                int moved = ManaOps.equalize(be, other, Math.min(be.getIoPerTick(), other.getIoPerTick()), 1);
                if (moved > 0) changed = true;
            }
        }
        if (changed) {
            be.markDirty();
            world.updateListeners(pos, state, state, 3);
        }
    }

    @Override public int getMana() { return mana; }
    @Override public int getCapacity() { return capacity; }
    @Override public int getIoPerTick() { return ioPerTick; }

    @Override public int insert(int amount, boolean simulate) {
        if (amount <= 0) return 0;
        int space = capacity - mana;
        if (space <= 0) return 0;
        int ins = Math.min(space, amount);
        if (!simulate) mana += ins;
        return ins;
    }

    @Override public int extract(int amount, boolean simulate) {
        if (amount <= 0) return 0;
        int ext = Math.min(mana, amount);
        if (!simulate) mana -= ext;
        return ext;
    }
}
