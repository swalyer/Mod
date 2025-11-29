package com.example.arcanomech.ritual;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class RitualDefinition {
    private final Identifier id;
    private final Identifier centerBlockId;
    private final List<RitualPedestal> pedestals;
    private final List<Ingredient> inputs;
    private final Identifier activationItemId;
    private final ItemStack result;
    private final int manaCost;

    public RitualDefinition(Identifier id, Identifier centerBlockId, List<RitualPedestal> pedestals, List<Ingredient> inputs, Identifier activationItemId, ItemStack result, int manaCost) {
        this.id = id;
        this.centerBlockId = centerBlockId;
        this.pedestals = List.copyOf(pedestals);
        this.inputs = List.copyOf(inputs);
        this.activationItemId = activationItemId;
        this.result = result.copy();
        this.manaCost = manaCost;
    }

    public Identifier getId() {
        return id;
    }

    public Identifier getCenterBlockId() {
        return centerBlockId;
    }

    public List<RitualPedestal> getPedestals() {
        return pedestals;
    }

    public List<Ingredient> getInputs() {
        return inputs;
    }

    public Identifier getActivationItemId() {
        return activationItemId;
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public int getManaCost() {
        return manaCost;
    }

    public record RitualPedestal(BlockPos offset, Identifier blockId) {
    }
}
