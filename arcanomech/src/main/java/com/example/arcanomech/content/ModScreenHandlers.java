package com.example.arcanomech.content;

import com.example.arcanomech.Arcanomech;
import com.example.arcanomech.magic.SpellTableScreenHandler;
import com.example.arcanomech.workbench.ArcaneWorkbenchScreenHandler;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {
    public static ScreenHandlerType<ArcaneWorkbenchScreenHandler> ARCANE_WORKBENCH;
    public static ScreenHandlerType<SpellTableScreenHandler> SPELL_TABLE;

    private ModScreenHandlers() {
    }

    public static void registerAll() {
        ARCANE_WORKBENCH = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier(Arcanomech.MOD_ID, "arcane_workbench"),
                new ExtendedScreenHandlerType<>(ArcaneWorkbenchScreenHandler::new)
        );
        SPELL_TABLE = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier(Arcanomech.MOD_ID, "spell_table"),
                new ExtendedScreenHandlerType<>(SpellTableScreenHandler::new)
        );
    }
}
