package com.example.arcanomech.workbench;

import org.jetbrains.annotations.Nullable;

import com.example.arcanomech.content.ModScreenHandlers;
import com.example.arcanomech.magic.ManaToolItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class ArcaneWorkbenchScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate properties;
    @Nullable
    private final ArcaneWorkbenchBlockEntity workbench;

    public ArcaneWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, ArcaneWorkbenchBlockEntity workbench) {
        this(syncId, playerInventory, workbench, workbench, workbench.getPropertyDelegate());
    }

    public ArcaneWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, resolveBlockEntity(playerInventory, buf.readBlockPos()));
    }

    private ArcaneWorkbenchScreenHandler(int syncId,
                                         PlayerInventory playerInventory,
                                         @Nullable ArcaneWorkbenchBlockEntity workbench,
                                         Inventory inventory,
                                         PropertyDelegate properties) {
        super(ModScreenHandlers.ARCANE_WORKBENCH, syncId);
        this.workbench = workbench;
        this.inventory = inventory != null ? inventory : new SimpleInventory(ArcaneWorkbenchBlockEntity.OUTPUT_SLOT + 1);
        this.properties = properties != null ? properties : new ArrayPropertyDelegate(4);

        this.inventory.onOpen(playerInventory.player);
        addSlots(playerInventory);
        addProperties(this.properties);
    }

    private static @Nullable ArcaneWorkbenchBlockEntity resolveBlockEntity(PlayerInventory playerInventory, BlockPos pos) {
        PlayerEntity player = playerInventory.player;
        if (player == null) {
            return null;
        }
        var world = player.getWorld();
        if (world == null) {
            return null;
        }
        if (!(world.getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity be)) {
            return null;
        }
        return be;
    }

    private void addSlots(PlayerInventory playerInventory) {
        // 2x3 input grid: slots [0..5]
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                int index = row * 3 + column;
                addSlot(new Slot(inventory, index, 26 + column * 18, 17 + row * 18));
            }
        }
        // Wand + Output
        addSlot(new WandSlot(inventory, ArcaneWorkbenchBlockEntity.WAND_SLOT, 134, 53));
        addSlot(new OutputSlot(inventory, ArcaneWorkbenchBlockEntity.OUTPUT_SLOT, 134, 27));

        // Player inventory
        int baseY = 84;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, baseY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, baseY + 58));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return workbench == null || workbench.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = getSlot(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getStack();
        ItemStack copy = original.copy();

        int containerSize = inventory.size();
        int outputIndex = ArcaneWorkbenchBlockEntity.OUTPUT_SLOT;
        int wandIndex = ArcaneWorkbenchBlockEntity.WAND_SLOT;

        if (index == outputIndex) {
            // Output -> player inventory
            if (!insertItem(original, containerSize, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(original, copy);
        } else if (index >= containerSize) {
            // From player inventory
            if (original.getItem() instanceof ManaToolItem) {
                if (!insertItem(original, wandIndex, wandIndex + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!insertItem(original, 0, ArcaneWorkbenchBlockEntity.INPUT_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (index == wandIndex) {
            // Wand -> player inventory
            if (!insertItem(original, containerSize, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Input slots -> player inventory
            if (!insertItem(original, containerSize, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (original.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, original);
        return copy;
    }

    public int getProgress() {
        return properties.get(0);
    }

    public int getWorkTime() {
        return properties.get(1);
    }

    public int getMana() {
        return properties.get(2);
    }

    public int getManaCost() {
        return properties.get(3);
    }

    private static class OutputSlot extends Slot {
        public OutputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }

    private static class WandSlot extends Slot {
        public WandSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.getItem() instanceof ManaToolItem;
        }
    }
}
