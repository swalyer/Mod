package com.example.arcanomech.gametest;

import java.util.concurrent.atomic.AtomicInteger;

import com.example.arcanomech.ModContent;
import com.example.arcanomech.block.CrusherBlockEntity;
import com.example.arcanomech.block.ManaBatteryBlockEntity;
import com.example.arcanomech.block.ManaCableBlockEntity;
import com.example.arcanomech.energy.Balance;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestContext;
import net.minecraft.util.math.BlockPos;

public class ManaNetworkGameTests implements FabricGameTest {
    private static final BlockState BATTERY_STATE = ModContent.MANA_BATTERY.getDefaultState();
    private static final BlockState CABLE_STATE = ModContent.MANA_CABLE.getDefaultState();
    private static final BlockState CRUSHER_STATE = ModContent.CRUSHER.getDefaultState();

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void cableTransfersFullChain(GameTestContext context) {
        BlockPos start = BlockPos.ORIGIN;
        BlockPos end = start.add(6, 0, 0);
        context.setBlockState(start, BATTERY_STATE);
        for (int i = 1; i <= 5; i++) {
            context.setBlockState(start.add(i, 0, 0), CABLE_STATE);
        }
        context.setBlockState(end, BATTERY_STATE);

        context.runAtTick(2, () -> {
            ManaBatteryBlockEntity source = getBattery(context, start);
            source.setMana(Balance.BATTERY_CAPACITY);
        });

        context.runAtTick(120, () -> {
            ManaBatteryBlockEntity source = getBattery(context, start);
            ManaBatteryBlockEntity target = getBattery(context, end);
            context.assertTrue(source.getMana() == 0, "Source battery should be empty after transfer");
            context.assertTrue(target.getMana() == Balance.BATTERY_CAPACITY, "Target battery should be full");
            context.complete();
        });
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void cableInterruptionStopsFlow(GameTestContext context) {
        BlockPos start = BlockPos.ORIGIN;
        BlockPos mid = start.add(3, 0, 0);
        BlockPos end = start.add(6, 0, 0);
        context.setBlockState(start, BATTERY_STATE);
        for (int i = 1; i <= 5; i++) {
            context.setBlockState(start.add(i, 0, 0), CABLE_STATE);
        }
        context.setBlockState(end, BATTERY_STATE);

        AtomicInteger manaBeforeBreak = new AtomicInteger();

        context.runAtTick(2, () -> {
            getBattery(context, start).setMana(Balance.BATTERY_CAPACITY);
        });

        context.runAtTick(30, () -> {
            manaBeforeBreak.set(getBattery(context, end).getMana());
            context.setBlockState(mid, net.minecraft.block.Blocks.AIR.getDefaultState());
        });

        context.runAtTick(70, () -> {
            int manaAfterBreak = getBattery(context, end).getMana();
            context.assertTrue(manaAfterBreak == manaBeforeBreak.get(), "Mana should not increase while cable is missing");
            context.setBlockState(mid, CABLE_STATE);
        });

        context.runAtTick(150, () -> {
            ManaBatteryBlockEntity target = getBattery(context, end);
            context.assertTrue(target.getMana() == Balance.BATTERY_CAPACITY, "Flow should resume after cable is restored");
            context.complete();
        });
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void cableStopsWhenTargetFull(GameTestContext context) {
        BlockPos start = BlockPos.ORIGIN;
        BlockPos end = start.add(4, 0, 0);
        context.setBlockState(start, BATTERY_STATE);
        for (int i = 1; i <= 3; i++) {
            context.setBlockState(start.add(i, 0, 0), CABLE_STATE);
        }
        context.setBlockState(end, BATTERY_STATE);

        context.runAtTick(2, () -> {
            getBattery(context, start).setMana(Balance.BATTERY_CAPACITY);
            getBattery(context, end).setMana(Balance.BATTERY_CAPACITY - Balance.CABLE_IO);
        });

        context.runAtTick(80, () -> {
            ManaBatteryBlockEntity source = getBattery(context, start);
            ManaBatteryBlockEntity target = getBattery(context, end);
            context.assertTrue(target.getMana() == Balance.BATTERY_CAPACITY, "Target should cap at capacity");
            context.assertTrue(source.getMana() < Balance.BATTERY_CAPACITY, "Source should transfer some mana");
            for (int i = 1; i <= 3; i++) {
                ManaCableBlockEntity cable = getCable(context, start.add(i, 0, 0));
                context.assertTrue(cable.getMana() <= Balance.CABLE_IO, "Cable should not retain significant mana");
            }
            context.complete();
        });
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void crusherConsumesManaAndProcesses(GameTestContext context) {
        BlockPos crusherPos = BlockPos.ORIGIN;
        context.setBlockState(crusherPos, CRUSHER_STATE);

        context.runAtTick(2, () -> {
            CrusherBlockEntity crusher = getCrusher(context, crusherPos);
            crusher.setMana(4_000);
            crusher.setStack(0, new ItemStack(Items.COBBLESTONE));
        });

        context.runAtTick(220, () -> {
            CrusherBlockEntity crusher = getCrusher(context, crusherPos);
            ItemStack output = crusher.getStack(1);
            context.assertTrue(!output.isEmpty() && output.getItem() == Items.GRAVEL, "Crusher should output gravel");
            context.assertTrue(crusher.getMana() <= 0, "Crusher should spend mana");
            context.complete();
        });
    }

    private ManaBatteryBlockEntity getBattery(GameTestContext context, BlockPos pos) {
        return (ManaBatteryBlockEntity) context.getBlockEntity(pos);
    }

    private ManaCableBlockEntity getCable(GameTestContext context, BlockPos pos) {
        return (ManaCableBlockEntity) context.getBlockEntity(pos);
    }

    private CrusherBlockEntity getCrusher(GameTestContext context, BlockPos pos) {
        return (CrusherBlockEntity) context.getBlockEntity(pos);
    }
}
