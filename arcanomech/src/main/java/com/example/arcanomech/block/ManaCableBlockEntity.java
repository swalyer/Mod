package com.example.arcanomech.block;

import java.util.Optional;

import com.example.arcanomech.Arcanomech;
import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.debug.DebugConfig;
import com.example.arcanomech.energy.Balance;
import com.example.arcanomech.energy.IOMode;
import com.example.arcanomech.energy.ManaApi;
import com.example.arcanomech.energy.ManaStorage;
import com.example.arcanomech.energy.SideConfig;
import com.example.arcanomech.energy.SideConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ManaCableBlockEntity extends BlockEntity implements ManaStorage, SideConfigHolder {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final String MANA_KEY = "mana";
    private static final String SIDE_CONFIG_KEY = "sideCfg";

    private final SideConfig sideConfig = SideConfig.all(IOMode.BOTH);
    private int mana;
    private int pullIndex;
    private int pushIndex;
    private int debugTicks;
    private int debugMoved;

    public ManaCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_CABLE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ManaCableBlockEntity cable) {
        if (world.isClient) {
            return;
        }
        cable.tickServer(world);
    }

    @Override
    public void setWorld(net.minecraft.world.World world) {
        super.setWorld(world);
        if (!world.isClient) {
            // init once on server if needed
        }
    }


    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public int getCapacity() {
        return Balance.CABLE_CAPACITY;
    }

    @Override
    public int insert(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int cappedAmount = Math.min(amount, Balance.CABLE_IO);
        int space = Balance.CABLE_CAPACITY - mana;
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
        int cappedAmount = Math.min(amount, Balance.CABLE_IO);
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
    public void onSideConfigChanged() {
        SideConfigHolder.super.onSideConfigChanged();
        refreshConnections();
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
        mana = Math.max(0, Math.min(Balance.CABLE_CAPACITY, nbt.getInt(MANA_KEY)));
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

    private void tickServer(World world) {
        pull(world);
        push(world);
        flushDebug(world);
    }

    private void pull(World world) {
        int space = Balance.CABLE_CAPACITY - mana;
        if (space <= 0) {
            return;
        }
        int remaining = Math.min(space, Balance.CABLE_IO);
        for (int i = 0; i < DIRECTIONS.length && remaining > 0; i++) {
            Direction direction = DIRECTIONS[(pullIndex + i) % DIRECTIONS.length];
            if (!sideConfig.get(direction).allowsInput()) {
                continue;
            }
            Optional<ManaApi.Neighbor> neighbor = ManaApi.findNeighborStorage(world, pos, direction);
            if (neighbor.isEmpty()) {
                continue;
            }
            ManaApi.Neighbor entry = neighbor.get();
            if (!entry.mode().allowsOutput()) {
                continue;
            }
            int moved = ManaApi.move(entry.storage(), this, Math.min(remaining, Balance.CABLE_IO));
            if (moved > 0) {
                remaining -= moved;
                recordTransfer(moved);
            }
        }
        pullIndex = (pullIndex + 1) % DIRECTIONS.length;
    }

    private void push(World world) {
        if (mana <= 0) {
            return;
        }
        int available = Math.min(mana, Balance.CABLE_IO);
        for (int i = 0; i < DIRECTIONS.length && available > 0; i++) {
            Direction direction = DIRECTIONS[(pushIndex + i) % DIRECTIONS.length];
            if (!sideConfig.get(direction).allowsOutput()) {
                continue;
            }
            Optional<ManaApi.Neighbor> neighbor = ManaApi.findNeighborStorage(world, pos, direction);
            if (neighbor.isEmpty()) {
                continue;
            }
            ManaApi.Neighbor entry = neighbor.get();
            if (!entry.mode().allowsInput()) {
                continue;
            }
            ManaStorage target = entry.storage();
            int capacity = target.getCapacity();
            if (capacity <= 0) {
                continue;
            }
            double cableFill = (double) mana / Balance.CABLE_CAPACITY;
            double targetFill = (double) target.getMana() / capacity;
            if (targetFill >= cableFill) {
                continue;
            }
            int moved = ManaApi.move(this, target, Math.min(available, Balance.CABLE_IO));
            if (moved > 0) {
                available -= moved;
                recordTransfer(moved);
            }
        }
        pushIndex = (pushIndex + 1) % DIRECTIONS.length;
    }

    private void updateMana(int newMana) {
        int clamped = Math.max(0, Math.min(Balance.CABLE_CAPACITY, newMana));
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

    private void recordTransfer(int amount) {
        if (amount <= 0 || !DebugConfig.isEnabled()) {
            return;
        }
        debugMoved += amount;
    }

    private void flushDebug(World world) {
        if (!DebugConfig.isEnabled()) {
            debugTicks = 0;
            debugMoved = 0;
            return;
        }
        debugTicks++;
        if (debugTicks >= 20) {
            if (debugMoved > 0) {
                Arcanomech.LOGGER.info("[Mana Debug] Cable {} moved {} mU", pos, debugMoved);
            }
            debugTicks = 0;
            debugMoved = 0;
        }
    }
}
