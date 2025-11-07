package com.example.arcanomech.client;

import net.fabricmc.api.ClientModInitializer;

public class ArcanomechClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WrenchOverlayRenderer.register();
    }
}
