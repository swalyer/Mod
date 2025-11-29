package com.example.arcanomech;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.content.ModScreenHandlers;
import com.example.arcanomech.debug.DebugConfig;
import com.example.arcanomech.aspects.AspectRegistry;
import com.example.arcanomech.aspects.AspectSourceManager;
import com.example.arcanomech.altar.AltarStructureManager;
import com.example.arcanomech.magic.SpellRegistry;
import com.example.arcanomech.magic.spells.ArcBoltSpell;
import com.example.arcanomech.magic.spells.BlinkSpell;
import com.example.arcanomech.magic.spells.WardSpell;
import com.example.arcanomech.network.NetworkHandler;
import com.example.arcanomech.recipe.ModRecipes;
import com.example.arcanomech.ritual.RitualManager;
import com.example.arcanomech.commands.ManaInfoCommand;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resource.ResourceType;

import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Arcanomech implements ModInitializer {
    public static final String MOD_ID = "arcanomech";
    public static final Logger LOGGER = LoggerFactory.getLogger("Arcanomech");

    @Override
    public void onInitialize() {
        ModContent.registerAll();
        ModBlockEntities.registerAll();
        ModScreenHandlers.registerAll();
        ModRecipes.registerAll();
        SpellRegistry registry = SpellRegistry.getInstance();
        registry.register(new BlinkSpell(new Identifier(MOD_ID, "blink")));
        registry.register(new ArcBoltSpell(new Identifier(MOD_ID, "arc_bolt")));
        registry.register(new WardSpell(new Identifier(MOD_ID, "ward")));
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(SpellRegistry.getInstance());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(AspectRegistry.getInstance());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(AspectSourceManager.getInstance());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(AltarStructureManager.getInstance());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(RitualManager.getInstance());
        NetworkHandler.registerServer();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
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
                            ));
            ManaInfoCommand.register(dispatcher, registryAccess);
        });
        LOGGER.info("Arcanomech loaded");
    }

    public static String id(String path) {
        return MOD_ID + ":" + Objects.requireNonNull(path);
    }
}
