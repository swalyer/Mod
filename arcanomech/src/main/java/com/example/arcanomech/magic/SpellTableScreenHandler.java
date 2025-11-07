package com.example.arcanomech.magic;

import org.jetbrains.annotations.Nullable;

import com.example.arcanomech.content.ModScreenHandlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class SpellTableScreenHandler extends ScreenHandler {
    private final SpellTableBlockEntity table;
    private final Inventory inventory;
    private final BlockPos pos;

    public SpellTableScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, getBlockEntity(playerInventory, buf.readBlockPos()));
    }

    public SpellTableScreenHandler(int syncId, PlayerInventory playerInventory, @Nullable SpellTableBlockEntity table) {
        super(ModScreenHandlers.SPELL_TABLE, syncId);
        this.table = table;
        if (table == null) {
            this.inventory = new net.minecraft.inventory.SimpleInventory(3);
            this.pos = BlockPos.ORIGIN;
        } else {
            this.inventory = table;
            this.pos = table.getPos();
        }
        checkSize(inventory, 3);
        inventory.onOpen(playerInventory.player);

        addSlot(new Slot(inventory, SpellTableBlockEntity.WAND_SLOT, 44, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof ArcaneWandItem;
            }
        });
        addSlot(new Slot(inventory, SpellTableBlockEntity.SCROLL_SLOT, 80, 17) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof SpellScrollItem;
            }
        });
        addSlot(new Slot(inventory, SpellTableBlockEntity.REAGENT_SLOT, 116, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(com.example.arcanomech.ModContent.MANA_RUNE);
            }
        });

        int m;
        for (m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        for (m = 0; m < 9; ++m) {
            addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    private static SpellTableBlockEntity getBlockEntity(PlayerInventory playerInventory, BlockPos pos) {
        if (playerInventory.player.getWorld() == null) {
            return null;
        }
        if (!(playerInventory.player.getWorld().getBlockEntity(pos) instanceof SpellTableBlockEntity table)) {
            return null;
        }
        return table;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            if (index < 3) {
                if (!insertItem(stackInSlot, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stackInSlot.getItem() instanceof ArcaneWandItem) {
                    if (!insertItem(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stackInSlot.getItem() instanceof SpellScrollItem) {
                    if (!insertItem(stackInSlot, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stackInSlot.isOf(com.example.arcanomech.ModContent.MANA_RUNE)) {
                    if (!insertItem(stackInSlot, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 30) {
                    if (!insertItem(stackInSlot, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!insertItem(stackInSlot, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return stack;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == 0 && table != null) {
            return table.inscribe(player);
        }
        return super.onButtonClick(player, id);
    }

    public BlockPos getPos() {
        return pos;
    }
}
