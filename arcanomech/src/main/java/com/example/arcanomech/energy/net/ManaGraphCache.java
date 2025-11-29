package com.example.arcanomech.energy.net;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.example.arcanomech.Arcanomech;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ManaGraphCache {
    private static final int CACHE_TTL = 10;
    private static final Direction[] DIRECTIONS = Direction.values();
    private final Map<BlockPos, ManaComponent> components = new HashMap<>();
    private final Map<BlockPos, Long> expirations = new HashMap<>();
    private final AtomicLong componentIds = new AtomicLong();

    public Optional<ManaComponent> getComponent(ServerWorld world, BlockPos origin) {
        long currentTime = world.getTime();
        if (components.containsKey(origin)) {
            long expiry = expirations.getOrDefault(origin, 0L);
            if (currentTime <= expiry) {
                return Optional.ofNullable(components.get(origin));
            }
        }
        Optional<ManaComponent> rebuilt = rebuild(world, origin);
        rebuilt.ifPresent(component -> {
            long expiry = currentTime + CACHE_TTL;
            for (BlockPos pos : component.getPositions()) {
                components.put(pos, component);
                expirations.put(pos, expiry);
            }
        });
        return rebuilt;
    }

    public void markDirty(BlockPos pos) {
        if (components.containsKey(pos)) {
            ManaComponent component = components.remove(pos);
            if (component != null) {
                for (BlockPos entry : component.getPositions()) {
                    components.remove(entry);
                    expirations.remove(entry);
                }
            }
        }
    }

    private Optional<ManaComponent> rebuild(ServerWorld world, BlockPos origin) {
        Map<BlockPos, ManaNode> nodes = new HashMap<>();
        Map<BlockPos, Set<BlockPos>> adjacency = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            if (visited.contains(pos)) {
                continue;
            }
            visited.add(pos);
            Optional<ManaNode> node = findNode(world, pos);
            if (node.isEmpty()) {
                continue;
            }
            nodes.put(pos, node.get());
            for (Direction direction : DIRECTIONS) {
                BlockPos neighbor = pos.offset(direction);
                Optional<ManaNode> neighborNode = findNode(world, neighbor);
                if (neighborNode.isEmpty()) {
                    continue;
                }
                adjacency.computeIfAbsent(pos, key -> new HashSet<>()).add(neighbor);
                adjacency.computeIfAbsent(neighbor, key -> new HashSet<>()).add(pos);
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        if (nodes.isEmpty()) {
            return Optional.empty();
        }
        int totalCapacity = 0;
        int totalMana = 0;
        for (ManaNode node : nodes.values()) {
            if (node.getCapacity() > 0) {
                totalCapacity += node.getCapacity();
                totalMana += node.getStored();
            }
        }
        long id = componentIds.incrementAndGet();
        double ratio = totalCapacity > 0 ? (double) totalMana / (double) totalCapacity : 0.0D;
        Arcanomech.LOGGER.info("[Arcanomech] Mana component #{} rebuilt with {} nodes at {} (total mana {}, capacity {}, fill {:.2f})", id, nodes.size(), origin, totalMana, totalCapacity, ratio);
        return Optional.of(new ManaComponent(id, nodes, adjacency, totalCapacity, totalMana));
    }

    private Optional<ManaNode> findNode(ServerWorld world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ManaNode manaNode) {
            return Optional.of(manaNode);
        }
        return Optional.empty();
    }
}
