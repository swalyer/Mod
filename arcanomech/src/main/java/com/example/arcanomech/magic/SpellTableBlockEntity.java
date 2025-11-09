package com.example.arcanomech.magic;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import com.example.arcanomech.ModContent;
import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.energy.Balance;
import com.example.arcanomech.energy.ManaStorage;
import com.example.arcanomech.util.ImplementedInventory;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.util.ItemScatterer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;

public class SpellTableBlockEntity extends BlockEntity implements ImplementedInventory, ExtendedScreenHandlerFactory {
    public static final int WAND_SLOT = 0;
    public static final int SCROLL_SLOT = 1;
    public static final int REAGENT_SLOT = 2;
    private static final int MAX_DISTANCE = 3;

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);

    public SpellTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPELL_TABLE, pos, state);
    }

    public void tick() {
        if (world == null || world.isClient) {
            return;
        }
        chargeNearby();
    }

    public static void tick(World world, BlockPos pos, BlockState state, SpellTableBlockEntity entity) {
        entity.tick();
    }

    private void chargeNearby() {
        ItemStack stack = getStack(WAND_SLOT);
        if (!(stack.getItem() instanceof ManaToolItem wand)) {
            return;
        }
        int capacity = wand.getCapacity(stack);
        int mana = wand.getMana(stack);
        if (mana >= capacity) {
            return;
        }
        int needed = capacity - mana;
        Mutable cursor = new Mutable();
        for (int dx = -MAX_DISTANCE; dx <= MAX_DISTANCE && needed > 0; dx++) {
            for (int dy = -1; dy <= 1 && needed > 0; dy++) {
                for (int dz = -MAX_DISTANCE; dz <= MAX_DISTANCE && needed > 0; dz++) {
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (cursor.equals(pos)) {
                        continue;
                    }
                    BlockEntity blockEntity = world.getBlockEntity(cursor);
                    if (blockEntity instanceof ManaStorage storage) {
                        int extracted = storage.extract(Math.min(needed, Balance.WAND_IO_STEP), false);
                        if (extracted > 0) {
                            int accepted = wand.insertMana(stack, extracted, false);
                            if (accepted < extracted) {
                                storage.insert(extracted - accepted, false);
                            }
                            needed -= accepted;
                            markDirty();
                        }
                    }
                }
            }
        }
    }

    public void dropInventory() {
        if (world != null) {
            ItemScatterer.spawn(world, pos, this);
        }
    }

    public boolean inscribe(PlayerEntity player) {
        ItemStack wandStack = getStack(WAND_SLOT);
        ItemStack scrollStack = getStack(SCROLL_SLOT);
        ItemStack reagentStack = getStack(REAGENT_SLOT);
        if (!(wandStack.getItem() instanceof ArcaneWandItem wand)) {
            player.sendMessage(Text.translatable("message.arcanomech.spell_table.no_wand"), true);
            return false;
        }
        if (!(scrollStack.getItem() instanceof SpellScrollItem scrollItem)) {
            player.sendMessage(Text.translatable("message.arcanomech.spell_table.no_scroll"), true);
            return false;
        }
        if (!reagentStack.isOf(ModContent.MANA_RUNE)) {
            player.sendMessage(Text.translatable("message.arcanomech.spell_table.no_rune"), true);
            return false;
        }
        SpellId id = scrollItem.getSpellId();
        wand.setSelectedSpell(wandStack, id.asString());
        wand.setCooldownTicks(wandStack, 0);
        scrollStack.decrement(1);
        reagentStack.decrement(1);
        markDirty();
        sync();
        player.sendMessage(Text.translatable("message.arcanomech.spell_table.inscribed", SpellRuntime.describe(id.id())), true);
        return true;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            case WAND_SLOT -> stack.getItem() instanceof ArcaneWandItem;
            case SCROLL_SLOT -> stack.getItem() instanceof SpellScrollItem;
            case REAGENT_SLOT -> stack.isOf(ModContent.MANA_RUNE);
            default -> false;
        };
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null) {
            return false;
        }
        if (world.getBlockEntity(pos) != this) {
            return false;
        }
        return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, items);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        sync();
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.arcanomech.spell_table");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new SpellTableScreenHandler(syncId, inv, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(getPos());
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

    public void sync() {
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }
}
