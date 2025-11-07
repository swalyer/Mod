package com.example.arcanomech.block;

import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.energy.Balance;
import com.example.arcanomech.energy.IOMode;
import com.example.arcanomech.energy.ManaStorage;
import com.example.arcanomech.energy.SideConfig;
import com.example.arcanomech.energy.SideConfigHolder;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class ManaBatteryBlockEntity extends BlockEntity implements ManaStorage, SideConfigHolder {
    private static final String MANA_KEY = "mana";
    private static final String SIDE_CONFIG_KEY = "sideCfg";

    private final SideConfig sideConfig = SideConfig.all(IOMode.BOTH);
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
        return Balance.BATTERY_CAPACITY;
    }

    @Override
    public int insert(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int cappedAmount = Math.min(amount, Balance.BATTERY_IO);
        int space = Balance.BATTERY_CAPACITY - mana;
        if (space <= 0) {
            return 0;
        }
        int accepted = Math.min(space, cappedAmount);
        if (!simulate && accepted > 0) {
            updateMana(mana + accepted);
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
        int cappedAmount = Math.min(amount, Balance.BATTERY_IO);
        int extracted = Math.min(mana, cappedAmount);
        if (!simulate && extracted > 0) {
            updateMana(mana - extracted);
        }
        return extracted;
    }

    @Override
    public SideConfig getSideConfig() {
        return sideConfig;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(MANA_KEY, mana);
        sideConfig.writeNbt(nbt, SIDE_CONFIG_KEY);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        mana = Math.max(0, Math.min(Balance.BATTERY_CAPACITY, nbt.getInt(MANA_KEY)));
        sideConfig.readNbt(nbt, SIDE_CONFIG_KEY);
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

    private void updateMana(int newMana) {
        int clamped = Math.max(0, Math.min(Balance.BATTERY_CAPACITY, newMana));
        if (clamped != mana) {
            mana = clamped;
            markDirty();
            sync();
        }
    }

    private void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }
}
