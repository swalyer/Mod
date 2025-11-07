package com.example.arcanomech.content;

import com.example.arcanomech.Arcanomech;
import com.example.arcanomech.ModContent;
import com.example.arcanomech.block.CrusherBlockEntity;
import com.example.arcanomech.block.ManaBatteryBlockEntity;
import com.example.arcanomech.block.ManaCableBlockEntity;
import com.example.arcanomech.workbench.ArcaneWorkbenchBlockEntity;
import com.example.arcanomech.magic.SpellTableBlockEntity;
import com.example.arcanomech.workbench.ArcaneWorkbenchBlockEntity;
import com.example.arcanomech.altar.AltarCoreBlockEntity;
import com.example.arcanomech.altar.PedestalBlockEntity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
    public static BlockEntityType<ManaBatteryBlockEntity> MANA_BATTERY;
    public static BlockEntityType<ManaCableBlockEntity> MANA_CABLE;
    public static BlockEntityType<CrusherBlockEntity> CRUSHER;
    public static BlockEntityType<ArcaneWorkbenchBlockEntity> ARCANE_WORKBENCH;
    public static BlockEntityType<SpellTableBlockEntity> SPELL_TABLE;
    public static BlockEntityType<AltarCoreBlockEntity> ALTAR_CORE;
    public static BlockEntityType<PedestalBlockEntity> ALTAR_PEDESTAL;

    private ModBlockEntities() {
    }

    public static void registerAll() {
        MANA_BATTERY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Arcanomech.MOD_ID, "mana_battery"),
                FabricBlockEntityTypeBuilder.create(ManaBatteryBlockEntity::new, ModContent.MANA_BATTERY).build(null)
        );
        MANA_CABLE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Arcanomech.MOD_ID, "mana_cable"),
                FabricBlockEntityTypeBuilder.create(ManaCableBlockEntity::new, ModContent.MANA_CABLE).build(null)
        );
        CRUSHER = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Arcanomech.MOD_ID, "crusher"),
                FabricBlockEntityTypeBuilder.create(CrusherBlockEntity::new, ModContent.CRUSHER).build(null)
        );
        ARCANE_WORKBENCH = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Arcanomech.MOD_ID, "arcane_workbench"),
                FabricBlockEntityTypeBuilder.create(ArcaneWorkbenchBlockEntity::new, ModContent.ARCANE_WORKBENCH).build(null)
        );
        SPELL_TABLE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Arcanomech.MOD_ID, "spell_table"),
                FabricBlockEntityTypeBuilder.create(SpellTableBlockEntity::new, ModContent.SPELL_TABLE).build(null)
        );
        ALTAR_CORE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Arcanomech.MOD_ID, "altar_core"),
                FabricBlockEntityTypeBuilder.create(AltarCoreBlockEntity::new, ModContent.ALTAR_CORE).build(null)
        );
        ALTAR_PEDESTAL = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Arcanomech.MOD_ID, "altar_pedestal"),
                FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new, ModContent.ALTAR_PEDESTAL).build(null)
        );
    }
}
