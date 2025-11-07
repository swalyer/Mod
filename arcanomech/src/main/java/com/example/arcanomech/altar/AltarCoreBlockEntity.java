package com.example.arcanomech.altar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.arcanomech.aspects.AspectCarrierItem;
import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.energy.Balance;
import com.example.arcanomech.energy.ManaApi;
import com.example.arcanomech.energy.ManaStorage;
import com.example.arcanomech.util.ImplementedInventory;
import com.example.arcanomech.recipe.ModRecipes;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemScatterer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class AltarCoreBlockEntity extends BlockEntity implements ImplementedInventory, ManaStorage {
    public static final int CENTER_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private int mana;
    private int progress;
    private int manaSpent;
    private int manaPerTick;
    private AltarRecipe currentRecipe;
    private final List<BlockPos> ingredientPedestals = new ArrayList<>();
    private final List<BlockPos> aspectPedestals = new ArrayList<>();

    public AltarCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR_CORE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, AltarCoreBlockEntity altar) {
        if (world.isClient) {
            return;
        }
        altar.pullMana(world);
        if (altar.currentRecipe == null) {
            altar.tryStart(world);
        } else {
            altar.continueRitual(world);
        }
    }

    private void tryStart(World world) {
        if (!getStack(OUTPUT_SLOT).isEmpty()) {
            return;
        }
        ItemStack center = getStack(CENTER_SLOT);
        if (center.isEmpty()) {
            return;
        }
        List<PedestalBlockEntity> pedestals = collectPedestals(world);
        if (pedestals.isEmpty() || !structureValid(pedestals)) {
            return;
        }
        List<ItemStack> pedestalItems = pedestals.stream().map(p -> p.getStack(0)).toList();
        AltarRecipe.AltarRecipeInventory inventory = new AltarRecipe.AltarRecipeInventory(center.copy(), pedestalItems);
        RecipeManager manager = world.getRecipeManager();
        Optional<AltarRecipe> match = manager.getFirstMatch(ModRecipes.ALTAR_RECIPE_TYPE, inventory, world);
        if (match.isEmpty()) {
            return;
        }
        AltarRecipe recipe = match.get();
        List<BlockPos> matchedPedestals = matchPedestals(recipe, pedestals);
        if (matchedPedestals == null) {
            return;
        }
        Map<Identifier, Integer> aspectRequirements = new HashMap<>(recipe.getAspects());
        if (!hasRequiredAspects(aspectRequirements, pedestals, matchedPedestals)) {
            return;
        }
        currentRecipe = recipe;
        ingredientPedestals.clear();
        ingredientPedestals.addAll(matchedPedestals);
        aspectPedestals.clear();
        for (PedestalBlockEntity pedestal : pedestals) {
            if (!ingredientPedestals.contains(pedestal.getPos())) {
                aspectPedestals.add(pedestal.getPos());
            }
        }
        manaPerTick = Math.max(1, Math.round((float) recipe.getManaCost() / recipe.getWorkTime()));
        progress = 0;
        manaSpent = 0;
        markDirty();
    }

    private void continueRitual(World world) {
        if (currentRecipe == null) {
            return;
        }
        if (!currentRecipe.getCenter().test(getStack(CENTER_SLOT))) {
            reset();
            return;
        }
        if (!pedestalsIntact(world)) {
            reset();
            return;
        }
        if (!getStack(OUTPUT_SLOT).isEmpty() && !ItemStack.canCombine(getStack(OUTPUT_SLOT), currentRecipe.getResult())) {
            return;
        }
        if (mana < manaPerTick) {
            return;
        }
        mana -= manaPerTick;
        manaSpent += manaPerTick;
        progress++;
        if (currentRecipe.getBaseStability() <= 0 && world.random.nextInt(200) == 0) {
            reset();
            return;
        }
        if (progress >= currentRecipe.getWorkTime() && manaSpent >= currentRecipe.getManaCost()) {
            finish(world);
        }
        markDirty();
    }

    private void finish(World world) {
        consumeCenter();
        consumeIngredients(world);
        consumeAspects(world);
        ItemStack result = currentRecipe.getResult();
        ItemStack output = getStack(OUTPUT_SLOT);
        if (output.isEmpty()) {
            setStack(OUTPUT_SLOT, result.copy());
        } else if (ItemStack.canCombine(output, result)) {
            output.increment(result.getCount());
        }
        reset();
        markDirty();
    }

    private void consumeCenter() {
        ItemStack center = getStack(CENTER_SLOT);
        if (!center.isEmpty()) {
            center.decrement(1);
        }
    }

    private void consumeIngredients(World world) {
        for (BlockPos pos : ingredientPedestals) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PedestalBlockEntity pedestal) {
                ItemStack stack = pedestal.getStack(0);
                if (!stack.isEmpty()) {
                    stack.decrement(1);
                    pedestal.markDirty();
                }
            }
        }
    }

    private void consumeAspects(World world) {
        if (currentRecipe == null) {
            return;
        }
        Map<Identifier, Integer> remaining = new HashMap<>(currentRecipe.getAspects());
        for (BlockPos pos : aspectPedestals) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof PedestalBlockEntity pedestal)) {
                continue;
            }
            ItemStack stack = pedestal.getStack(0);
            if (stack.getItem() instanceof AspectCarrierItem carrier) {
                Identifier aspectId = carrier.getAspectId(stack);
                if (aspectId != null && remaining.containsKey(aspectId)) {
                    int needed = remaining.get(aspectId);
                    int provided = carrier.getCapacityUnits() * stack.getCount();
                    if (provided >= needed) {
                        pedestal.setStack(0, ItemStack.EMPTY);
                        remaining.remove(aspectId);
                    } else {
                        pedestal.setStack(0, ItemStack.EMPTY);
                        remaining.put(aspectId, needed - provided);
                    }
                }
            }
        }
    }

    private boolean pedestalsIntact(World world) {
        for (BlockPos pos : ingredientPedestals) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof PedestalBlockEntity pedestal)) {
                return false;
            }
            if (pedestal.getStack(0).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRequiredAspects(Map<Identifier, Integer> requirements, List<PedestalBlockEntity> pedestals, List<BlockPos> ingredientPositions) {
        for (PedestalBlockEntity pedestal : pedestals) {
            if (ingredientPositions.contains(pedestal.getPos())) {
                continue;
            }
            ItemStack stack = pedestal.getStack(0);
            if (stack.getItem() instanceof AspectCarrierItem carrier) {
                Identifier aspectId = carrier.getAspectId(stack);
                if (aspectId != null && requirements.containsKey(aspectId)) {
                    int provided = carrier.getCapacityUnits() * stack.getCount();
                    requirements.merge(aspectId, -provided, Integer::sum);
                }
            }
        }
        return requirements.values().stream().allMatch(value -> value <= 0);
    }

    private List<BlockPos> matchPedestals(AltarRecipe recipe, List<PedestalBlockEntity> pedestals) {
        List<PedestalBlockEntity> available = new ArrayList<>(pedestals);
        List<BlockPos> matched = new ArrayList<>();
        for (Ingredient ingredient : recipe.getPedestalInputs()) {
            boolean found = false;
            for (int i = 0; i < available.size(); i++) {
                PedestalBlockEntity pedestal = available.get(i);
                if (ingredient.test(pedestal.getStack(0))) {
                    matched.add(pedestal.getPos());
                    available.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }
        }
        return matched;
    }

    private List<PedestalBlockEntity> collectPedestals(World world) {
        List<PedestalBlockEntity> pedestals = new ArrayList<>();
        Mutable cursor = new Mutable();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                cursor.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                BlockEntity blockEntity = world.getBlockEntity(cursor);
                if (blockEntity instanceof PedestalBlockEntity pedestal) {
                    pedestals.add(pedestal);
                }
            }
        }
        return pedestals;
    }

    private boolean structureValid(List<PedestalBlockEntity> pedestals) {
        List<BlockPos> offsets = pedestals.stream().map(p -> p.getPos().subtract(pos)).toList();
        for (AltarStructure structure : AltarStructureManager.getInstance().getStructures()) {
            if (offsets.size() < structure.minPedestals() || offsets.size() > structure.maxPedestals()) {
                continue;
            }
            boolean radiusOk = offsets.stream().allMatch(offset -> Math.round(Math.sqrt(offset.getX() * offset.getX() + offset.getZ() * offset.getZ())) == structure.radius());
            if (!radiusOk) {
                continue;
            }
            if (isRadiallySymmetric(offsets)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRadiallySymmetric(List<BlockPos> offsets) {
        for (BlockPos offset : offsets) {
            BlockPos opposite = new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
            if (offsets.stream().noneMatch(pos -> pos.equals(opposite))) {
                return false;
            }
        }
        return true;
    }

    private void pullMana(World world) {
        if (mana >= Balance.ALTAR_CAPACITY) {
            return;
        }
        for (Direction direction : Direction.values()) {
            Optional<ManaApi.Neighbor> neighbor = ManaApi.findNeighborStorage(world, pos, direction);
            if (neighbor.isEmpty()) {
                continue;
            }
            ManaApi.Neighbor entry = neighbor.get();
            if (!entry.mode().allowsOutput()) {
                continue;
            }
            int moved = ManaApi.move(entry.storage(), this, Balance.ALTAR_IO);
            if (moved > 0) {
                break;
            }
        }
    }

    private void reset() {
        currentRecipe = null;
        progress = 0;
        manaSpent = 0;
        ingredientPedestals.clear();
        aspectPedestals.clear();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public int getCapacity() {
        return Balance.ALTAR_CAPACITY;
    }

    @Override
    public int insert(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int space = Balance.ALTAR_CAPACITY - mana;
        if (space <= 0) {
            return 0;
        }
        int accepted = Math.min(space, Math.min(amount, Balance.ALTAR_IO));
        if (!simulate) {
            mana += accepted;
            markDirty();
        }
        return accepted;
    }

    @Override
    public int extract(int amount, boolean simulate) {
        return 0;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Mana", mana);
        nbt.putInt("Progress", progress);
        nbt.putInt("Spent", manaSpent);
        Inventories.writeNbt(nbt, items);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        mana = nbt.getInt("Mana");
        progress = nbt.getInt("Progress");
        manaSpent = nbt.getInt("Spent");
        Inventories.readNbt(nbt, items);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public void dropInventory() {
        if (world != null) {
            ItemScatterer.spawn(world, pos, this);
        }
    }

    public boolean canPlayerUse(PlayerEntity player) {
        return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}
