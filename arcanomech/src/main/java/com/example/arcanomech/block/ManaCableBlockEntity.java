package com.example.arcanomech.block;

import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.energy.Balance;
import com.example.arcanomech.energy.IOMode;
import com.example.arcanomech.energy.SideConfig;
import com.example.arcanomech.energy.SideConfigHolder;
import com.example.arcanomech.energy.net.ManaNetworkManager;
import com.example.arcanomech.energy.net.ManaNode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ManaCableBlockEntity extends BlockEntity implements SideConfigHolder, ManaNode {
    private static final String SIDE_CONFIG_KEY = "sideCfg";

    private final SideConfig sideConfig = SideConfig.all(IOMode.BOTH);

    public ManaCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_CABLE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ManaCableBlockEntity cable) {
        if (world.isClient) {
            return;
        }
        ManaNetworkManager.balance(world, pos);
    }

    @Override
    public int getStored() {
        return 0;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public int insert(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int extract(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int getMaxIoPerTick() {
        return Balance.CABLE_IO;
    }

    @Override
    public SideConfig getSideConfig() {
        return sideConfig;
    }

    @Override
    public void onSideConfigChanged() {
        SideConfigHolder.super.onSideConfigChanged();
        refreshConnections();
        if (world != null) {
            ManaNetworkManager.markDirty(world, pos);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        sideConfig.writeNbt(nbt, SIDE_CONFIG_KEY);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
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

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (world != null) {
            ManaNetworkManager.markDirty(world, pos);
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (world != null) {
            ManaNetworkManager.markDirty(world, pos);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world != null) {
            ManaNetworkManager.markDirty(world, pos);
        }
    }

    private void refreshConnections() {
        if (world == null) {
            return;
        }
        BlockState state = getCachedState();
        if (!(state.getBlock() instanceof ManaCableBlock block)) {
            return;
        }
        BlockState updated = block.updateConnections(world, pos, state);
        if (!updated.equals(state)) {
            world.setBlockState(pos, updated, Block.NOTIFY_LISTENERS);
        } else {
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
        world.updateNeighborsAlways(pos, state.getBlock());
    }
}
