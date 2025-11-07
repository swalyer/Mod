package com.example.arcanomech.client;

import com.example.arcanomech.client.workbench.ArcaneWorkbenchScreen;
import com.example.arcanomech.content.ModScreenHandlers;
import com.example.arcanomech.platform.ClientBridge;
import com.example.arcanomech.network.NetworkHandler;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ArcanomechClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // WrenchOverlayRenderer.register();
        HandledScreens.register(ModScreenHandlers.ARCANE_WORKBENCH, ArcaneWorkbenchScreen::new);
        HandledScreens.register(ModScreenHandlers.SPELL_TABLE, com.example.arcanomech.client.magic.SpellTableScreen::new);
        ClientBridge.INSTANCE = new ClientBridge.Impl() {
            @Override
            public void sendWrenchUse(BlockPos pos, Direction side, Hand hand) {
                boolean shift = Screen.hasShiftDown();
                boolean alt = Screen.hasAltDown();
                ClientPlayNetworking.send(NetworkHandler.WRENCH_PACKET, NetworkHandler.createWrenchBuf(pos, side, shift, alt, hand));
            }
        };
    }
}
