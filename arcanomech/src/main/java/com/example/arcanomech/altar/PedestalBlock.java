package com.example.arcanomech.altar;

import org.jetbrains.annotations.Nullable;

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

public class PedestalBlock extends BlockWithEntity {
    public PedestalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof PedestalBlockEntity pedestal)) {
            return ActionResult.PASS;
        }
        ItemStack held = player.getStackInHand(hand);
        ItemStack stored = pedestal.getStack(0);
        if (stored.isEmpty() && !held.isEmpty()) {
            pedestal.setStack(0, held.split(1));
            pedestal.markDirty();
            return ActionResult.CONSUME;
        }
        if (!stored.isEmpty()) {
            ItemStack removed = pedestal.removeStack(0);
            if (!player.giveItemStack(removed)) {
                player.dropItem(removed, false);
            }
            pedestal.markDirty();
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PedestalBlockEntity pedestal) {
                pedestal.dropInventory();
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}
