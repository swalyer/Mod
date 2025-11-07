package com.example.arcanomech.workbench;

import java.util.Objects;

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
    @Nullable
    private final ArcaneWorkbenchBlockEntity workbench;
    private final Inventory inventory;
    private final PropertyDelegate properties;

    public ArcaneWorkbenchScreenHandler(int syncId, PlayerInventory inv) {
        this(syncId, inv, (ArcaneWorkbenchBlockEntity) null);
    }

    public ArcaneWorkbenchScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        this(syncId, inv, resolveBlockEntity(inv, buf.readBlockPos()));
    }

    public ArcaneWorkbenchScreenHandler(int syncId, PlayerInventory inv, @Nullable ArcaneWorkbenchBlockEntity workbench) {
        super(ModScreenHandlers.ARCANE_WORKBENCH, syncId);
        this.workbench = workbench;
        if (workbench != null) {
            this.inventory = workbench;
            this.properties = workbench.getPropertyDelegate();
        } else {
            this.inventory = new SimpleInventory(ArcaneWorkbenchBlockEntity.OUTPUT_SLOT + 1);
            this.properties = new ArrayPropertyDelegate(4);
        }
        addSlots(inv);
        addProperties(this.properties);
    }

    private static @Nullable ArcaneWorkbenchBlockEntity resolveBlockEntity(PlayerInventory inv, BlockPos pos) {
        PlayerEntity player = inv.player;
        if (player == null) {
            return null;
        }
        if (!(player.getWorld().getBlockEntity(Objects.requireNonNull(pos)) instanceof ArcaneWorkbenchBlockEntity be)) {
            return null;
        }
        return be;
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
        int y = 84;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, y + 58));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return workbench == null || workbench.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = getSlot(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getStack();
        result = original.copy();
        int containerSize = inventory.size();
        if (index == ArcaneWorkbenchBlockEntity.OUTPUT_SLOT) {
            if (!insertItem(original, containerSize, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(original, result);
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
        if (original.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTakeItem(player, original);
        return result;
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
