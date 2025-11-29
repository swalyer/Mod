package com.example.arcanomech.workbench;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.example.arcanomech.Arcanomech;
import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.energy.Balance;
import com.example.arcanomech.energy.IOMode;
import com.example.arcanomech.energy.ManaApi;
import com.example.arcanomech.energy.ManaStorage;
import com.example.arcanomech.energy.SideConfig;
import com.example.arcanomech.energy.SideConfigHolder;
import com.example.arcanomech.magic.ManaToolItem;
import com.example.arcanomech.util.ImplementedInventory;
import com.example.arcanomech.recipe.ModRecipes;
import com.example.arcanomech.recipe.WorkbenchRecipe;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.util.ItemScatterer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ArcaneWorkbenchBlockEntity extends BlockEntity implements ImplementedInventory, SidedInventory, ManaStorage,
        SideConfigHolder, ExtendedScreenHandlerFactory {
    private static final int[] TOP_SLOTS = new int[]{0, 1, 2, 3, 4, 5};
    private static final int[] SIDE_SLOTS = new int[]{0, 1, 2, 3, 4, 5};
    private static final int[] BOTTOM_SLOTS = new int[]{7};

    private static final String MANA_KEY = "mana";
    private static final String PROGRESS_KEY = "progress";
    private static final String MANA_SPENT_KEY = "manaSpent";
    private static final String RECIPE_WORK_TIME_KEY = "recipeTime";
    private static final String RECIPE_MANA_KEY = "recipeMana";
    private static final String SIDE_CONFIG_KEY = "sideCfg";

    public static final int INPUT_SLOTS = 6;
    public static final int WAND_SLOT = 6;
    public static final int OUTPUT_SLOT = 7;

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(OUTPUT_SLOT + 1, ItemStack.EMPTY);
    private final SideConfig sideConfig = SideConfig.all(IOMode.DISABLED);
    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int size() {
            return 4;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> Math.max(1, currentWorkTime);
                case 2 -> mana;
                case 3 -> currentManaCost;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> currentWorkTime = value;
                case 2 -> mana = value;
                case 3 -> currentManaCost = value;
                default -> {
                }
            }
        }
    };

    private int mana;
    private int progress;
    private int manaSpent;
    private int currentWorkTime = Balance.WORKBENCH_DEFAULT_WORK_TIME;
    private int currentManaCost = Balance.WORKBENCH_DEFAULT_MANA_PER_TICK * Balance.WORKBENCH_DEFAULT_WORK_TIME;

    public ArcaneWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_WORKBENCH, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ArcaneWorkbenchBlockEntity workbench) {
        if (world.isClient) {
            return;
        }
        workbench.tickServer(world);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == OUTPUT_SLOT) {
            return false;
        }
        if (slot == WAND_SLOT) {
            return stack.getItem() instanceof ManaToolItem;
        }
        return slot >= 0 && slot < INPUT_SLOTS;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (slot == OUTPUT_SLOT) {
            return true;
        }
        if (slot == WAND_SLOT) {
            return dir != Direction.DOWN;
        }
        return false;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        }
        if (side == Direction.UP) {
            return TOP_SLOTS;
        }
        return SIDE_SLOTS;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT) {
            return false;
        }
        if (slot == WAND_SLOT) {
            return stack.getItem() instanceof ManaToolItem;
        }
        return slot >= 0 && slot < INPUT_SLOTS;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null || world.getBlockEntity(pos) != this) {
            return false;
        }
        return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public int getCapacity() {
        return Balance.WORKBENCH_CAPACITY;
    }

    @Override
    public int insert(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int space = Balance.WORKBENCH_CAPACITY - mana;
        if (space <= 0) {
            return 0;
        }
        int accepted = Math.min(space, Math.min(amount, Balance.WORKBENCH_IO));
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
        markDirty();
        sync();
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
        nbt.putInt(MANA_SPENT_KEY, manaSpent);
        nbt.putInt(RECIPE_WORK_TIME_KEY, currentWorkTime);
        nbt.putInt(RECIPE_MANA_KEY, currentManaCost);
        Inventories.writeNbt(nbt, items);
        sideConfig.writeNbt(nbt, SIDE_CONFIG_KEY);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        mana = Math.max(0, Math.min(Balance.WORKBENCH_CAPACITY, nbt.getInt(MANA_KEY)));
        progress = Math.max(0, nbt.getInt(PROGRESS_KEY));
        manaSpent = Math.max(0, nbt.getInt(MANA_SPENT_KEY));
        currentWorkTime = Math.max(1, nbt.contains(RECIPE_WORK_TIME_KEY) ? nbt.getInt(RECIPE_WORK_TIME_KEY)
                : Balance.WORKBENCH_DEFAULT_WORK_TIME);
        currentManaCost = Math.max(1, nbt.contains(RECIPE_MANA_KEY) ? nbt.getInt(RECIPE_MANA_KEY)
                : Balance.WORKBENCH_DEFAULT_MANA_PER_TICK * Balance.WORKBENCH_DEFAULT_WORK_TIME);
        Inventories.readNbt(nbt, items);
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

    @Override
    public Text getDisplayName() {
        return Text.translatable("block." + Arcanomech.MOD_ID + ".arcane_workbench");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ArcaneWorkbenchScreenHandler(syncId, playerInventory, this);
    }

    public PropertyDelegate getPropertyDelegate() {
        return properties;
    }

    public void sendStatusOverlay(PlayerEntity player) {
        player.sendMessage(Text.translatable("tooltip." + Arcanomech.MOD_ID + ".mana_status", mana, Balance.WORKBENCH_CAPACITY), true);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    private void tickServer(World world) {
        pullMana(world);
        Optional<WorkbenchRecipe> recipeOptional = getRecipe(world);
        if (recipeOptional.isEmpty()) {
            resetProgress();
            return;
        }
        WorkbenchRecipe recipe = recipeOptional.get();
        ItemStack result = recipe.getOutput(world.getRegistryManager());
        if (!canAcceptResult(result)) {
            resetProgress();
            return;
        }
        int manaCost = recipe.getManaCost();
        int workTime = Math.max(1, recipe.getWorkTime());
        currentManaCost = manaCost;
        currentWorkTime = workTime;
        int manaPerTick = Math.max(1, Math.round((float) manaCost / workTime));
        if (mana < manaPerTick) {
            int missing = manaPerTick - mana;
            mana += extractFromWand(missing);
        }
        if (mana < manaPerTick) {
            return;
        }
        updateMana(mana - manaPerTick);
        manaSpent += manaPerTick;
        progress++;
        if (progress >= workTime && manaSpent >= manaCost) {
            craft(recipe, result);
        }
    }

    private void pullMana(World world) {
        if (mana >= Balance.WORKBENCH_CAPACITY) {
            return;
        }
        for (Direction direction : Direction.values()) {
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
            int moved = ManaApi.move(entry.storage(), this, Balance.WORKBENCH_IO);
            if (moved > 0) {
                break;
            }
        }
    }

    private Optional<WorkbenchRecipe> getRecipe(World world) {
        SimpleInventory inventory = new SimpleInventory(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            inventory.setStack(i, getStack(i));
        }
        return world.getRecipeManager().getFirstMatch(ModRecipes.WORKBENCH_RECIPE_TYPE, inventory, world);
    }

    private boolean canAcceptResult(ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = getStack(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.canCombine(output, result)) {
            return false;
        }
        return output.getCount() + result.getCount() <= output.getMaxCount();
    }

    private void craft(WorkbenchRecipe recipe, ItemStack result) {
        DefaultedList<ItemStack> remaining = recipe.getRemainder(createInputInventory());
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack current = getStack(i);
            if (!current.isEmpty()) {
                current.decrement(1);
            }
            if (i < remaining.size()) {
                ItemStack leftover = remaining.get(i);
                if (!leftover.isEmpty()) {
                    if (current.isEmpty()) {
                        setStack(i, leftover);
                    } else {
                        if (world != null) {
                            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), leftover);
                        }
                    }
                }
            }
        }
        ItemStack output = getStack(OUTPUT_SLOT);
        if (output.isEmpty()) {
            setStack(OUTPUT_SLOT, result.copy());
        } else if (ItemStack.canCombine(output, result)) {
            output.increment(result.getCount());
        }
        progress = 0;
        manaSpent = 0;
        markDirty();
    }

    private SimpleInventory createInputInventory() {
        SimpleInventory inventory = new SimpleInventory(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            inventory.setStack(i, getStack(i));
        }
        return inventory;
    }

    private int extractFromWand(int requested) {
        if (requested <= 0) {
            return 0;
        }
        ItemStack stack = getStack(WAND_SLOT);
        if (!(stack.getItem() instanceof ManaToolItem wand)) {
            return 0;
        }
        return wand.extractMana(stack, Math.min(requested, Balance.WAND_IO_STEP));
    }

    private void resetProgress() {
        boolean dirty = false;
        if (progress != 0 || manaSpent != 0) {
            progress = 0;
            manaSpent = 0;
            dirty = true;
        }
        int defaultWorkTime = Balance.WORKBENCH_DEFAULT_WORK_TIME;
        int defaultMana = Balance.WORKBENCH_DEFAULT_MANA_PER_TICK * Balance.WORKBENCH_DEFAULT_WORK_TIME;
        if (currentWorkTime != defaultWorkTime || currentManaCost != defaultMana) {
            currentWorkTime = defaultWorkTime;
            currentManaCost = defaultMana;
            dirty = true;
        }
        if (dirty) {
            markDirty();
        }
    }

    private void updateMana(int value) {
        int clamped = Math.max(0, Math.min(Balance.WORKBENCH_CAPACITY, value));
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
