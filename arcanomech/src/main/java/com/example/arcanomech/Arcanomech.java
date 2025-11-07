package com.example.arcanomech;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.arcanomech.ModContent;
import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.debug.DebugConfig;
import com.example.arcanomech.network.NetworkHandler;
import com.example.arcanomech.recipe.ModRecipes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class Arcanomech implements ModInitializer {
    public static final String MOD_ID = "arcanomech";
    public static final Logger LOGGER = LoggerFactory.getLogger("Arcanomech");

    @Override
    public void onInitialize() {
        ModContent.registerAll();
        ModBlockEntities.registerAll();
        ModRecipes.registerAll();
        NetworkHandler.registerServer();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal(MOD_ID)
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("debug")
                                .then(CommandManager.literal("on").executes(context -> {
                                    DebugConfig.setEnabled(true);
                                    context.getSource().sendFeedback(() -> Text.literal("Arcanomech debug logging enabled"), true);
                                    return 1;
                                }))
                                .then(CommandManager.literal("off").executes(context -> {
                                    DebugConfig.setEnabled(false);
                                    context.getSource().sendFeedback(() -> Text.literal("Arcanomech debug logging disabled"), true);
                                    return 1;
                                }))
                        )));
        LOGGER.info("Arcanomech loaded");
    }

    public static String id(String path) {
        return MOD_ID + ":" + Objects.requireNonNull(path);
    }
}
