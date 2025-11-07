package com.example.arcanomech.platform;

import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class ClientBridge {
    public interface Impl {
        void sendWrenchUse(BlockPos pos, Direction side, Hand hand);
    }

    public static Impl INSTANCE = (pos, side, hand) -> { };

    private ClientBridge() { }
}
