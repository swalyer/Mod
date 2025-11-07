package com.example.arcanomech.energy;

import java.util.Optional;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class ManaApi {
    private ManaApi() {
    }

    public static Optional<Neighbor> findNeighborStorage(World world, BlockPos pos, Direction side) {
        if (world == null || pos == null || side == null) {
            return Optional.empty();
        }
        BlockPos neighborPos = pos.offset(side);
        BlockEntity blockEntity = world.getBlockEntity(neighborPos);
        if (!(blockEntity instanceof ManaStorage storage)) {
            return Optional.empty();
        }
        IOMode mode = IOMode.BOTH;
        if (blockEntity instanceof SideConfigHolder holder) {
            mode = holder.getSideConfig().get(side.getOpposite());
            if (mode == IOMode.DISABLED) {
                return Optional.empty();
            }
        }
        return Optional.of(new Neighbor(storage, mode));
    }

    public static int move(ManaStorage src, ManaStorage dst, int maxAmount) {
        if (src == null || dst == null || maxAmount <= 0) {
            return 0;
        }
        int simulatedExtract = src.extract(maxAmount, true);
        if (simulatedExtract <= 0) {
            return 0;
        }
        int simulatedInsert = dst.insert(simulatedExtract, true);
        if (simulatedInsert <= 0) {
            return 0;
        }
        int moved = Math.min(simulatedExtract, simulatedInsert);
        if (moved <= 0) {
            return 0;
        }
        int actuallyExtracted = src.extract(moved, false);
        if (actuallyExtracted <= 0) {
            return 0;
        }
        int actuallyInserted = dst.insert(actuallyExtracted, false);
        if (actuallyInserted < actuallyExtracted) {
            if (actuallyInserted > 0) {
                src.insert(actuallyExtracted - actuallyInserted, false);
            }
            return actuallyInserted;
        }
        return actuallyInserted;
    }

    public record Neighbor(ManaStorage storage, IOMode mode) {
    }
}
