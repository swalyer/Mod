package com.example.arcanomech;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class Arcanomech implements ModInitializer {
    public static final String MOD_ID = "arcanomech";
    public static final Logger LOGGER = LoggerFactory.getLogger("Arcanomech");

    @Override
    public void onInitialize() {
        ModContent.registerAll();
        LOGGER.info("Arcanomech loaded");
    }

    public static String id(String path) {
        return MOD_ID + ":" + Objects.requireNonNull(path);
    }
}
