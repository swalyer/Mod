package com.example.arcanomech.energy.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.example.arcanomech.Arcanomech;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ManaNetworkManager {
    private static final Map<ServerWorld, ManaGraphCache> CACHES = new HashMap<>();

    private ManaNetworkManager() {
    }

    public static ManaGraphCache getCache(ServerWorld world) {
        return CACHES.computeIfAbsent(world, key -> new ManaGraphCache());
    }

    public static void markDirty(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        getCache(serverWorld).markDirty(pos);
    }

    public static void balance(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        ManaBalancer.balance(serverWorld, pos, getCache(serverWorld));
    }

    public static Optional<ManaComponent> getComponent(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return Optional.empty();
        }
        return getCache(serverWorld).getComponent(serverWorld, pos);
    }

    public static void dropWorld(ServerWorld world) {
        ManaGraphCache cache = CACHES.remove(world);
        if (cache != null) {
            Arcanomech.LOGGER.info("[Arcanomech] Cleared mana cache for world {}", world.getRegistryKey().getValue());
        }
    }
}
