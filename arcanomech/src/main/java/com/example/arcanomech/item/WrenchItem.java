package com.example.arcanomech.item;

import com.example.arcanomech.energy.IOMode;
import com.example.arcanomech.energy.SideConfig;
import com.example.arcanomech.energy.SideConfigHolder;
import com.example.arcanomech.energy.SideRaycast;
import com.example.arcanomech.network.NetworkHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class WrenchItem extends Item {
    private static final String STORED_SIDE_KEY = "storedSides";

    public WrenchItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) {
            handleClientUse(context);
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }

    public static void handleUse(ServerPlayerEntity player, Hand hand, BlockPos pos, Direction side, boolean shift, boolean alt) {
        if (player == null) {
            return;
        }
        World world = player.getWorld();
        if (world == null || world.isClient) {
            return;
        }
        if (!world.isChunkLoaded(pos)) {
            return;
        }
        ItemStack stack = player.getStackInHand(hand);
        if (!(stack.getItem() instanceof WrenchItem)) {
            return;
        }
        if (!(world.getBlockEntity(pos) instanceof SideConfigHolder holder)) {
            return;
        }
        SideConfig sideConfig = holder.getSideConfig();
        if (alt && shift) {
            SideConfig stored = readStoredConfig(stack);
            if (stored != null) {
                sideConfig.copyFrom(stored);
                holder.onSideConfigChanged();
                player.sendMessage(Text.translatable("item.arcanomech.wrench.paste"), true);
            } else {
                player.sendMessage(Text.translatable("item.arcanomech.wrench.empty"), true);
            }
            return;
        }
        if (alt) {
            storeConfig(stack, sideConfig);
            player.sendMessage(Text.translatable("item.arcanomech.wrench.copied"), true);
            return;
        }
        IOMode next = sideConfig.cycle(side);
        if (shift) {
            sideConfig.setAll(next);
        }
        holder.onSideConfigChanged();
        player.sendMessage(Text.translatable("item.arcanomech.wrench.mode", Text.translatable("mode.arcanomech." + next.name().toLowerCase())), true);
    }

    private static void storeConfig(ItemStack stack, SideConfig config) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putIntArray(STORED_SIDE_KEY, config.toIdArray());
    }

    private static SideConfig readStoredConfig(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return null;
        }
        int[] raw = nbt.getIntArray(STORED_SIDE_KEY);
        if (raw.length == 0) {
            return null;
        }
        SideConfig config = SideConfig.all(IOMode.DISABLED);
        config.readFromIds(raw);
        return config;
    }

    @Environment(EnvType.CLIENT)
    private static void handleClientUse(ItemUsageContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }
        BlockPos pos = context.getBlockPos();
        BlockHitResult hit = context.getHitResult() instanceof BlockHitResult blockHit ? blockHit : null;
        Direction side = SideRaycast.pickSide(player, pos, hit);
        boolean shift = Screen.hasShiftDown();
        boolean alt = Screen.hasAltDown();
        Hand hand = context.getHand();
        ClientPlayNetworking.send(NetworkHandler.WRENCH_PACKET, NetworkHandler.createWrenchBuf(pos, side, shift, alt, hand));
    }
}
