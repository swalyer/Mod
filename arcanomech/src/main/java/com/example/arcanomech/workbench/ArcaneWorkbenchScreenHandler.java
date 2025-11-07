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

    private ArcaneWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, @Nullable ArcaneWorkbenchBlockEntity workbench,
                                         Inventory inventory, PropertyDelegate properties) {
        super(ModScreenHandlers.ARCANE_WORKBENCH, syncId);
        this.workbench = workbench;
        this.inventory = inventory;
        this.properties = properties;
        inventory.onOpen(playerInventory.player);
        addSlots(playerInventory);
        addProperties(properties);
    }

    private void addSlots(PlayerInventory playerInventory) {
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                int index = row * 3 + column;
                addSlot(new Slot(inventory, index, 26 + column * 18, 17 + row * 18));
            }
        }
        addSlot(new WandSlot(inventory, ArcaneWorkbenchBlockEntity.WAND_SLOT, 134, 53));
        addSlot(new OutputSlot(inventory, ArcaneWorkbenchBlockEntity.OUTPUT_SLOT, 134, 27));

        int playerStartY = 84;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, playerStartY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, playerStartY + 58));
        }
    }

    private static ArcaneWorkbenchBlockEntity resolveBlockEntity(PlayerInventory playerInventory, BlockPos pos) {
        if (playerInventory.player == null) {
            return null;
        }
        var world = playerInventory.player.getWorld();
        if (world == null) {
            return null;
        }
        if (!(world.getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity blockEntity)) {
            return null;
        }
        return blockEntity;
    }

    private ArcaneWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, @Nullable ArcaneWorkbenchBlockEntity workbench) {
        this(syncId, playerInventory, workbench, workbench != null ? workbench : new SimpleInventory(ArcaneWorkbenchBlockEntity.OUTPUT_SLOT + 1),
                workbench != null ? workbench.getPropertyDelegate() : new ArrayPropertyDelegate(4));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return workbench == null || workbench.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = getSlot(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getStack();
        newStack = original.copy();
        int containerSize = inventory.size();
        if (index == ArcaneWorkbenchBlockEntity.OUTPUT_SLOT) {
            if (!insertItem(original, containerSize, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(original, newStack);
        } else if (index >= containerSize) {
            if (original.getItem() instanceof ManaToolItem) {
                if (!insertItem(original, ArcaneWorkbenchBlockEntity.WAND_SLOT, ArcaneWorkbenchBlockEntity.WAND_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(original, 0, ArcaneWorkbenchBlockEntity.INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index == ArcaneWorkbenchBlockEntity.WAND_SLOT) {
            if (!insertItem(original, containerSize, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!insertItem(original, containerSize, slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        if (original.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        if (original.getCount() == newStack.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTakeItem(player, original);
        return newStack;
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

    private class WandSlot extends Slot {
        public WandSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.getItem() instanceof ManaToolItem;
        }
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
}
