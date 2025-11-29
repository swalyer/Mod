package com.example.arcanomech.altar;

import org.jetbrains.annotations.Nullable;

import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.ritual.RitualRuntime;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AltarCoreBlock extends BlockWithEntity {
    public AltarCoreBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AltarCoreBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof AltarCoreBlockEntity altar)) {
            return ActionResult.PASS;
        }
        ItemStack output = altar.getStack(AltarCoreBlockEntity.OUTPUT_SLOT);
        if (!output.isEmpty()) {
            ItemStack removed = altar.removeStack(AltarCoreBlockEntity.OUTPUT_SLOT);
            if (!player.giveItemStack(removed)) {
                player.dropItem(removed, false);
            }
            altar.markDirty();
            return ActionResult.CONSUME;
        }
        ItemStack held = player.getStackInHand(hand);
        if (RitualRuntime.tryActivate(world, pos, player, held)) {
            return ActionResult.CONSUME;
        }
        ItemStack center = altar.getStack(AltarCoreBlockEntity.CENTER_SLOT);
        if (center.isEmpty() && !held.isEmpty()) {
            altar.setStack(AltarCoreBlockEntity.CENTER_SLOT, held.split(1));
            altar.markDirty();
            return ActionResult.CONSUME;
        }
        if (!center.isEmpty()) {
            ItemStack removed = altar.removeStack(AltarCoreBlockEntity.CENTER_SLOT);
            if (!player.giveItemStack(removed)) {
                player.dropItem(removed, false);
            }
            altar.markDirty();
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AltarCoreBlockEntity altar) {
                altar.dropInventory();
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.block.entity.BlockEntityTicker<T> getTicker(World world, BlockState state, net.minecraft.block.entity.BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlockEntities.ALTAR_CORE, AltarCoreBlockEntity::tick);
    }
}
