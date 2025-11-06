package com.example.arcanomech.block;

import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.energy.ManaStorage;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class ManaBatteryBlockEntity extends BlockEntity implements ManaStorage {
    private static final String MANA_KEY = "mana";
    private static final int CAPACITY = 10_000;

    private int mana;

    public ManaBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_BATTERY, pos, state);
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public int getCapacity() {
        return CAPACITY;
    }

    @Override
    public int insert(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int space = CAPACITY - mana;
        if (space <= 0) {
            return 0;
        }
        int accepted = Math.min(space, amount);
        if (!simulate && accepted > 0) {
            mana += accepted;
            markDirty();
            sync();
        }
        return accepted;
    }

    @Override
    public int extract(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        if (mana <= 0) {
            return 0;
        }
        int extracted = Math.min(mana, amount);
        if (!simulate && extracted > 0) {
            mana -= extracted;
            markDirty();
            sync();
        }
        return extracted;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(MANA_KEY, mana);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        mana = nbt.getInt(MANA_KEY);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    private void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }
}
