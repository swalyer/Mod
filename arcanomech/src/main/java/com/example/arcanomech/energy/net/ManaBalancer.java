package com.example.arcanomech.energy.net;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.arcanomech.energy.Balance;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class ManaBalancer {
    private ManaBalancer() {
    }

    public static void balance(ServerWorld world, BlockPos origin, ManaGraphCache cache) {
        cache.getComponent(world, origin).ifPresent(component -> balanceComponent(component));
    }

    private static void balanceComponent(ManaComponent component) {
        Map<BlockPos, ManaNode> nodes = component.getNodes();
        Map<BlockPos, Integer> delta = new HashMap<>();
        Map<BlockPos, Integer> ioBudget = new HashMap<>();
        Map<EdgeKey, Integer> edgeBudget = new HashMap<>();
        int totalCapacity = 0;
        int totalStored = 0;
        for (Map.Entry<BlockPos, ManaNode> entry : nodes.entrySet()) {
            ManaNode node = entry.getValue();
            if (node.getCapacity() > 0) {
                totalCapacity += node.getCapacity();
                totalStored += node.getStored();
            }
            ioBudget.put(entry.getKey(), node.getMaxIoPerTick());
        }
        if (totalCapacity <= 0 || totalStored <= 0) {
            return;
        }
        double ratio = (double) totalStored / (double) totalCapacity;
        for (Map.Entry<BlockPos, ManaNode> entry : nodes.entrySet()) {
            ManaNode node = entry.getValue();
            int target = node.getCapacity() > 0 ? (int) Math.floor(node.getCapacity() * ratio) : 0;
            delta.put(entry.getKey(), node.getStored() - target);
        }
        List<BlockPos> sinks = new ArrayList<>();
        for (Map.Entry<BlockPos, Integer> entry : delta.entrySet()) {
            if (entry.getValue() < -1) {
                sinks.add(entry.getKey());
            }
        }
        for (BlockPos sinkPos : sinks) {
            while (delta.get(sinkPos) < -1) {
                Path path = findPath(component, sinkPos, delta, ioBudget, edgeBudget);
                if (path == null) {
                    break;
                }
                int sinkNeed = -delta.get(sinkPos);
                int sinkBudget = ioBudget.getOrDefault(sinkPos, 0);
                int sourceBudget = ioBudget.getOrDefault(path.source(), 0);
                int pathNodeBudget = path.minNodeBudget(ioBudget);
                int pathEdgeBudget = path.minEdgeBudget(edgeBudget);
                int amount = Math.min(Math.min(sinkNeed, Math.min(sourceBudget, sinkBudget)), Math.min(pathNodeBudget, pathEdgeBudget));
                if (amount <= 0) {
                    break;
                }
                ManaNode sourceNode = nodes.get(path.source());
                ManaNode sinkNode = nodes.get(sinkPos);
                int extracted = sourceNode.extract(amount, false);
                int accepted = sinkNode.insert(extracted, false);
                if (accepted <= 0) {
                    break;
                }
                delta.put(path.source(), delta.get(path.source()) - accepted);
                delta.put(sinkPos, delta.get(sinkPos) + accepted);
                applyBudgets(path, ioBudget, edgeBudget, accepted);
            }
        }
    }

    private static Path findPath(ManaComponent component, BlockPos sink, Map<BlockPos, Integer> delta, Map<BlockPos, Integer> ioBudget, Map<EdgeKey, Integer> edgeBudget) {
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos, BlockPos> previous = new HashMap<>();
        queue.add(sink);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            if (!current.equals(sink) && delta.getOrDefault(current, 0) > 1 && ioBudget.getOrDefault(current, 0) > 0) {
                return buildPath(current, sink, previous);
            }
            for (BlockPos neighbor : component.getNeighbors(current)) {
                if (ioBudget.getOrDefault(neighbor, 0) <= 0) {
                    continue;
                }
                if (edgeBudget.getOrDefault(new EdgeKey(current, neighbor), Balance.CABLE_IO) <= 0) {
                    continue;
                }
                if (!previous.containsKey(neighbor)) {
                    previous.put(neighbor, current);
                }
                queue.add(neighbor);
            }
        }
        return null;
    }

    private static Path buildPath(BlockPos source, BlockPos sink, Map<BlockPos, BlockPos> previous) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos current = source;
        while (current != null && !current.equals(sink)) {
            positions.add(current);
            current = previous.get(current);
        }
        positions.add(sink);
        return new Path(source, positions);
    }

    private static void applyBudgets(Path path, Map<BlockPos, Integer> ioBudget, Map<EdgeKey, Integer> edgeBudget, int amount) {
        for (BlockPos pos : path.positions()) {
            int remaining = Math.max(0, ioBudget.getOrDefault(pos, 0) - amount);
            ioBudget.put(pos, remaining);
        }
        List<BlockPos> positions = path.positions();
        for (int i = 0; i < positions.size() - 1; i++) {
            BlockPos a = positions.get(i);
            BlockPos b = positions.get(i + 1);
            EdgeKey key = new EdgeKey(a, b);
            int remaining = Math.max(0, edgeBudget.getOrDefault(key, Balance.CABLE_IO) - amount);
            edgeBudget.put(key, remaining);
        }
    }

    private record EdgeKey(BlockPos a, BlockPos b) {
        private EdgeKey(BlockPos a, BlockPos b) {
            this.a = a.asLong() <= b.asLong() ? a : b;
            this.b = a.asLong() <= b.asLong() ? b : a;
        }
    }

    private record Path(BlockPos source, List<BlockPos> positions) {
        private int minNodeBudget(Map<BlockPos, Integer> ioBudget) {
            int limit = Integer.MAX_VALUE;
            for (BlockPos pos : positions) {
                limit = Math.min(limit, ioBudget.getOrDefault(pos, 0));
            }
            return limit;
        }

        private int minEdgeBudget(Map<EdgeKey, Integer> edgeBudget) {
            int limit = Integer.MAX_VALUE;
            for (int i = 0; i < positions.size() - 1; i++) {
                EdgeKey key = new EdgeKey(positions.get(i), positions.get(i + 1));
                limit = Math.min(limit, edgeBudget.getOrDefault(key, Balance.CABLE_IO));
            }
            return limit;
        }
    }
}
