package com.example.arcanomech.client;

import com.example.arcanomech.client.magic.SpellTableScreen;
import com.example.arcanomech.client.workbench.ArcaneWorkbenchScreen;
import com.example.arcanomech.content.ModScreenHandlers;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ArcanomechClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WrenchOverlayRenderer.register();
        HandledScreens.register(ModScreenHandlers.ARCANE_WORKBENCH, ArcaneWorkbenchScreen::new);
        HandledScreens.register(ModScreenHandlers.SPELL_TABLE, SpellTableScreen::new);
    }
}
