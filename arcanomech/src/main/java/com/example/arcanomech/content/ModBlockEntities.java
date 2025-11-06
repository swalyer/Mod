package com.example.arcanomech.content;

import com.example.arcanomech.Arcanomech;
import com.example.arcanomech.block.ManaBatteryBlockEntity;
import com.example.arcanomech.ModContent;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
    public static BlockEntityType<ManaBatteryBlockEntity> MANA_BATTERY;

    private ModBlockEntities() {
    }

    public static void registerAll() {
        MANA_BATTERY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Arcanomech.MOD_ID, "mana_battery"),
                FabricBlockEntityTypeBuilder.create(ManaBatteryBlockEntity::new, ModContent.MANA_BATTERY).build(null)
        );
    }
}
