package com.example.arcanomech.network;

import com.example.arcanomech.Arcanomech;
import com.example.arcanomech.item.WrenchItem;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class NetworkHandler {
    public static final Identifier WRENCH_PACKET = new Identifier(Arcanomech.MOD_ID, "wrench_click");

    private NetworkHandler() {
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(WRENCH_PACKET, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            Direction side = buf.readEnumConstant(Direction.class);
            boolean shift = buf.readBoolean();
            boolean alt = buf.readBoolean();
            Hand hand = buf.readEnumConstant(Hand.class);
            server.execute(() -> handleWrenchPacket(player, hand, pos, side, shift, alt));
        });
    }

    private static void handleWrenchPacket(ServerPlayerEntity player, Hand hand, BlockPos pos, Direction side, boolean shift, boolean alt) {
        if (player == null || player.getWorld() == null) {
            return;
        }
        if (!player.getWorld().isChunkLoaded(pos)) {
            return;
        }
        WrenchItem.handleUse(player, hand, pos, side, shift, alt);
    }

    public static PacketByteBuf createWrenchBuf(BlockPos pos, Direction side, boolean shift, boolean alt, Hand hand) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeEnumConstant(side);
        buf.writeBoolean(shift);
        buf.writeBoolean(alt);
        buf.writeEnumConstant(hand);
        return buf;
    }
}
