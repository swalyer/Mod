package com.example.arcanomech.block;

import java.util.Optional;

import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.energy.Balance;
import com.example.arcanomech.energy.IOMode;
import com.example.arcanomech.energy.ManaStorage;
import com.example.arcanomech.energy.SideConfig;
import com.example.arcanomech.energy.SideConfigHolder;
import com.example.arcanomech.recipe.CrusherRecipe;
import com.example.arcanomech.recipe.ModRecipes;
import com.example.arcanomech.util.ImplementedInventory;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CrusherBlockEntity extends BlockEntity implements ManaStorage, SideConfigHolder, ImplementedInventory, SidedInventory {
    private static final int[] INPUT_SLOTS = new int[]{0};
    private static final int[] OUTPUT_SLOTS = new int[]{1};
    private static final String MANA_KEY = "mana";
    private static final String PROGRESS_KEY = "progress";
    private static final String SIDE_CONFIG_KEY = "sideCfg";
    private static final String RECIPE_TIME_KEY = "recipeTime";

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final SideConfig sideConfig = SideConfig.all(IOMode.INPUT);
    private int mana;
    private int progress;
    private int currentRecipeTime = Balance.CRUSHER_WORK_TIME;

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CrusherBlockEntity crusher) {
        if (world.isClient) {
            return;
        }
        crusher.tickServer(world);
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public int getCapacity() {
        return Balance.CRUSHER_CAPACITY;
    }

    @Override
    public int insert(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int cappedAmount = Math.min(amount, Balance.CRUSHER_IO);
        int space = Balance.CRUSHER_CAPACITY - mana;
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
        return 0;
    }

    @Override
    public SideConfig getSideConfig() {
        return sideConfig;
    }

    @Override
    public void onSideConfigChanged() {
        SideConfigHolder.super.onSideConfigChanged();
        sync();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == 0) {
            return true;
        }
        return false;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return OUTPUT_SLOTS;
        }
        return INPUT_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot == 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 1;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null || world.getBlockEntity(pos) != this) {
            return false;
        }
        return player.squaredDistanceTo((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        sync();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(MANA_KEY, mana);
        nbt.putInt(PROGRESS_KEY, progress);
        nbt.putInt(RECIPE_TIME_KEY, currentRecipeTime);
        sideConfig.writeNbt(nbt, SIDE_CONFIG_KEY);
        Inventories.writeNbt(nbt, items);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        mana = Math.max(0, Math.min(Balance.CRUSHER_CAPACITY, nbt.getInt(MANA_KEY)));
        progress = Math.max(0, nbt.getInt(PROGRESS_KEY));
        currentRecipeTime = nbt.contains(RECIPE_TIME_KEY) ? nbt.getInt(RECIPE_TIME_KEY) : Balance.CRUSHER_WORK_TIME;
        sideConfig.readNbt(nbt, SIDE_CONFIG_KEY);
        Inventories.readNbt(nbt, items);
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

    public int getProgress() {
        return progress;
    }

    public int getRequiredProgress() {
        return currentRecipeTime;
    }

    public void setMana(int amount) {
        updateMana(amount);
    }

    private void tickServer(World world) {
        Optional<CrusherRecipe> optional = getCurrentRecipe(world);
        if (optional.isEmpty()) {
            resetProgress();
            return;
        }
        CrusherRecipe recipe = optional.get();
        ItemStack result = recipe.getOutput(world.getRegistryManager());
        if (!canAcceptResult(result)) {
            resetProgress();
            return;
        }
        currentRecipeTime = recipe.getWorkTime();
        progress++;
        if (progress >= currentRecipeTime) {
            craft(recipe, result);
            progress = 0;
        }
    }

    private Optional<CrusherRecipe> getCurrentRecipe(World world) {
        SimpleInventory inventory = new SimpleInventory(1);
        inventory.setStack(0, getStack(0));
        return world.getRecipeManager().getFirstMatch(ModRecipes.CRUSHER_RECIPE_TYPE, inventory, world);
    }

    private boolean canAcceptResult(ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = getStack(1);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.canCombine(output, result)) {
            return false;
        }
        return output.getCount() + result.getCount() <= output.getMaxCount();
    }

    private void craft(CrusherRecipe recipe, ItemStack result) {
        ItemStack input = getStack(0);
        if (!input.isEmpty()) {
            input.decrement(1);
        }
        ItemStack output = getStack(1);
        if (output.isEmpty()) {
            setStack(1, result.copy());
        } else if (ItemStack.canCombine(output, result)) {
            output.increment(result.getCount());
        }
        markDirty();
    }

    private void resetProgress() {
        boolean dirty = false;
        if (progress > 0) {
            progress = 0;
            dirty = true;
        }
        if (currentRecipeTime != Balance.CRUSHER_WORK_TIME) {
            currentRecipeTime = Balance.CRUSHER_WORK_TIME;
            dirty = true;
        }
        if (dirty) {
            markDirty();
        }
    }

    private void updateMana(int newMana) {
        int clamped = Math.max(0, Math.min(Balance.CRUSHER_CAPACITY, newMana));
        if (clamped != mana) {
            mana = clamped;
            markDirty();
        }
    }

    private void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }
}
