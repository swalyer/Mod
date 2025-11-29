package com.example.arcanomech.ritual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.example.arcanomech.Arcanomech;
import com.example.arcanomech.altar.PedestalBlockEntity;
import com.example.arcanomech.energy.net.ManaComponent;
import com.example.arcanomech.energy.net.ManaNetworkManager;
import com.example.arcanomech.energy.net.ManaNode;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class RitualRuntime {
    private RitualRuntime() {
    }

    public static boolean tryActivate(World world, BlockPos pos, PlayerEntity player, ItemStack activationItem) {
        if (world.isClient) {
            return false;
        }
        if (!(world instanceof ServerWorld serverWorld)) {
            return false;
        }
        if (activationItem.isEmpty()) {
            return false;
        }
        Item activation = activationItem.getItem();
        for (RitualDefinition definition : RitualManager.getInstance().getRituals()) {
            if (!activation.getRegistryEntry().registryKey().getValue().equals(definition.getActivationItemId())) {
                continue;
            }
            if (!world.getBlockState(pos).isOf(Registries.BLOCK.get(definition.getCenterBlockId()))) {
                continue;
            }
            List<PedestalBlockEntity> pedestals = collectPedestals(world, pos, definition);
            if (pedestals == null) {
                continue;
            }
            List<ItemStack> pedestalItems = pedestals.stream().map(pedestal -> pedestal.getStack(0)).toList();
            if (!matchesInputs(definition.getInputs(), pedestalItems)) {
                continue;
            }
            int manaCost = Math.max(0, definition.getManaCost());
            if (manaCost > 0 && !drawMana(serverWorld, pos, manaCost)) {
                Arcanomech.LOGGER.info("[Arcanomech] Ritual {} aborted at {} due to insufficient mana", definition.getId(), pos);
                player.sendMessage(net.minecraft.text.Text.literal("Not enough mana for ritual"), true);
                return true;
            }
            consumeInputs(definition.getInputs(), pedestals);
            consumeActivation(player, activationItem);
            ItemStack result = definition.getResult();
            if (!player.giveItemStack(result.copy())) {
                player.dropItem(result.copy(), false);
            }
            Arcanomech.LOGGER.info("[Arcanomech] Ritual {} completed at {}", definition.getId(), pos);
            player.sendMessage(net.minecraft.text.Text.literal("Ritual complete"), true);
            return true;
        }
        return false;
    }

    private static List<PedestalBlockEntity> collectPedestals(World world, BlockPos center, RitualDefinition definition) {
        List<PedestalBlockEntity> pedestals = new ArrayList<>();
        for (RitualDefinition.RitualPedestal entry : definition.getPedestals()) {
            BlockPos target = center.add(entry.offset());
            if (!world.getBlockState(target).isOf(net.minecraft.registry.Registries.BLOCK.get(entry.blockId()))) {
                return null;
            }
            BlockEntity blockEntity = world.getBlockEntity(target);
            if (!(blockEntity instanceof PedestalBlockEntity pedestal)) {
                return null;
            }
            pedestals.add(pedestal);
        }
        return pedestals;
    }

    private static boolean matchesInputs(List<Ingredient> required, List<ItemStack> available) {
        List<ItemStack> remaining = new ArrayList<>(available);
        for (Ingredient ingredient : required) {
            boolean matched = false;
            for (int i = 0; i < remaining.size(); i++) {
                ItemStack stack = remaining.get(i);
                if (ingredient.test(stack)) {
                    remaining.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private static void consumeInputs(List<Ingredient> required, List<PedestalBlockEntity> pedestals) {
        Map<Ingredient, Integer> counts = new HashMap<>();
        for (Ingredient ingredient : required) {
            counts.merge(ingredient, 1, Integer::sum);
        }
        for (PedestalBlockEntity pedestal : pedestals) {
            ItemStack stack = pedestal.getStack(0);
            if (stack.isEmpty()) {
                continue;
            }
            for (Map.Entry<Ingredient, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > 0 && entry.getKey().test(stack)) {
                    stack.decrement(1);
                    pedestal.markDirty();
                    counts.put(entry.getKey(), entry.getValue() - 1);
                    break;
                }
            }
        }
    }

    private static boolean drawMana(ServerWorld world, BlockPos pos, int amount) {
        Optional<ManaComponent> component = findComponent(world, pos);
        if (component.isEmpty()) {
            return false;
        }
        ManaComponent graph = component.get();
        int remaining = amount;
        for (ManaNode node : graph.getNodes().values()) {
            if (node.getCapacity() <= 0) {
                continue;
            }
            remaining -= Math.min(node.getStored(), remaining);
        }
        if (remaining > 0) {
            return false;
        }
        remaining = amount;
        Map<ManaNode, Integer> budgets = new HashMap<>();
        for (ManaNode node : graph.getNodes().values()) {
            budgets.put(node, node.getMaxIoPerTick());
        }
        while (remaining > 0) {
            boolean movedAny = false;
            for (ManaNode node : graph.getNodes().values()) {
                if (node.getCapacity() <= 0) {
                    continue;
                }
                int budget = budgets.getOrDefault(node, 0);
                if (budget <= 0) {
                    continue;
                }
                int toExtract = Math.min(Math.min(budget, node.getStored()), remaining);
                if (toExtract > 0) {
                    int extracted = node.extract(toExtract, false);
                    if (extracted > 0) {
                        budgets.put(node, Math.max(0, budget - extracted));
                        remaining -= extracted;
                        movedAny = true;
                        if (remaining <= 0) {
                            break;
                        }
                    }
                }
            }
            if (!movedAny) {
                break;
            }
        }
        return remaining <= 0;
    }

    private static Optional<ManaComponent> findComponent(ServerWorld world, BlockPos pos) {
        Set<BlockPos> checked = new HashSet<>();
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.offset(direction);
            if (!checked.add(neighbor)) {
                continue;
            }
            if (!(world.getBlockEntity(neighbor) instanceof ManaNode)) {
                continue;
            }
            Optional<ManaComponent> component = ManaNetworkManager.getComponent(world, neighbor);
            if (component.isPresent()) {
                return component;
            }
        }
        return Optional.empty();
    }

    private static void consumeActivation(PlayerEntity player, ItemStack activationItem) {
        activationItem.decrement(1);
    }
}
