package com.example.arcanomech.block;

import com.example.arcanomech.ModContent;
import com.example.arcanomech.energy.Balance;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ManaBatteryBlock extends BlockWithEntity {
    private static final int TRANSFER_PER_USE = Balance.BATTERY_IO;

    public ManaBatteryBlock() {
        super(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(3.0F, 6.0F).requiresTool());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ManaBatteryBlockEntity(pos, state);
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ManaBatteryBlockEntity battery)) {
            return ActionResult.PASS;
        }

        boolean isCrystal = stack.isOf(ModContent.ETHER_CRYSTAL);
        if (isCrystal) {
            if (!world.isClient) {
                int inserted = battery.insert(TRANSFER_PER_USE, false);
                if (inserted > 0) {
                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    player.sendMessage(Text.literal("Mana: " + battery.getMana() + "/" + battery.getCapacity()), true);
                } else {
                    player.sendMessage(Text.literal("Mana Battery is full"), true);
                }
            }
            return ActionResult.SUCCESS;
        }

        boolean emptyHand = stack.isEmpty();
        if (player.isSneaking() && emptyHand) {
            if (!world.isClient) {
                player.sendMessage(Text.literal("Mana: " + battery.getMana() + "/" + battery.getCapacity()), true);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public <T extends BlockEntity> net.minecraft.block.entity.BlockEntityTicker<T> getTicker(
            net.minecraft.world.World world, BlockState state, net.minecraft.block.entity.BlockEntityType<T> type) {
        return world.isClient ? null : (w, p, s, t) -> {
            if (t instanceof com.example.arcanomech.energy.BatteryBlockEntity be) {
                com.example.arcanomech.energy.BatteryBlockEntity.tick(w, p, s, be);
            }
        };
    }

}
