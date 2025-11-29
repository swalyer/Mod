package com.example.arcanomech.energy.net;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.math.BlockPos;

public class ManaComponent {
    private final long id;
    private final Set<BlockPos> positions;
    private final Map<BlockPos, ManaNode> nodes;
    private final Map<BlockPos, Set<BlockPos>> adjacency;
    private final int totalCapacity;

    public ManaComponent(long id, Map<BlockPos, ManaNode> nodes, Map<BlockPos, Set<BlockPos>> adjacency, int totalCapacity, int totalMana) {
        this.id = id;
        this.positions = Collections.unmodifiableSet(new HashSet<>(nodes.keySet()));
        this.nodes = Collections.unmodifiableMap(new HashMap<>(nodes));
        Map<BlockPos, Set<BlockPos>> neighbors = new HashMap<>();
        for (Map.Entry<BlockPos, Set<BlockPos>> entry : adjacency.entrySet()) {
            neighbors.put(entry.getKey(), Collections.unmodifiableSet(new HashSet<>(entry.getValue())));
        }
        this.adjacency = Collections.unmodifiableMap(neighbors);
        this.totalCapacity = totalCapacity;
    }

    public long getId() {
        return id;
    }

    public Set<BlockPos> getPositions() {
        return positions;
    }

    public Map<BlockPos, ManaNode> getNodes() {
        return nodes;
    }

    public Set<BlockPos> getNeighbors(BlockPos pos) {
        return adjacency.getOrDefault(pos, Collections.emptySet());
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public int getTotalMana() {
        int total = 0;
        for (ManaNode node : nodes.values()) {
            if (node.getCapacity() > 0) {
                total += node.getStored();
            }
        }
        return total;
    }

    public double getFillRatio() {
        return totalCapacity > 0 ? (double) getTotalMana() / (double) totalCapacity : 0.0D;
    }
}
