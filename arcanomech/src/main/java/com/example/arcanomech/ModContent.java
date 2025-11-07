package com.example.arcanomech;

import com.example.arcanomech.block.CrusherBlock;
import com.example.arcanomech.block.ManaBatteryBlock;
import com.example.arcanomech.block.ManaCableBlock;
import com.example.arcanomech.aspects.AspectCarrierItem;
import com.example.arcanomech.altar.AltarCoreBlock;
import com.example.arcanomech.altar.PedestalBlock;
import com.example.arcanomech.magic.ArcaneWandItem;
import com.example.arcanomech.magic.SpellId;
import com.example.arcanomech.magic.SpellScrollItem;
import com.example.arcanomech.magic.table.SpellTableBlock;
import com.example.arcanomech.workbench.ArcaneWorkbenchBlock;
import com.example.arcanomech.item.WrenchItem;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModContent {
    public static final Block MANA_BATTERY = new ManaBatteryBlock();
    public static final Block MANA_CABLE = new ManaCableBlock();
    public static final Block CRUSHER = new CrusherBlock();
    public static final Block ARCANE_WORKBENCH = new ArcaneWorkbenchBlock();
    public static final Block ARCANE_WORKBENCH = new ArcaneWorkbenchBlock();
    public static final Block SPELL_TABLE = new SpellTableBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).strength(2.5F));
    public static final Block ALTAR_CORE = new AltarCoreBlock(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(3.0F));
    public static final Block ALTAR_PEDESTAL = new PedestalBlock(FabricBlockSettings.create().mapColor(MapColor.QUARTZ).strength(2.0F));
    public static final Item ETHER_CRYSTAL = new Item(new Item.Settings());
    public static final Item WRENCH = new WrenchItem(new Item.Settings().maxCount(1));
    public static final Item ARCANE_WAND = new ArcaneWandItem(new Item.Settings().maxCount(1));
    public static final Item ARCANE_WAND = new ArcaneWandItem(new Item.Settings().maxCount(1));
    public static final Item ARCANE_WAND_T2 = new ArcaneWandItem(new Item.Settings().maxCount(1));
    public static final Item BLANK_RUNE = new Item(new Item.Settings());
    public static final Item MANA_RUNE = new Item(new Item.Settings());
    public static final Item SPELL_SCROLL_BLINK = new SpellScrollItem(new Item.Settings().maxCount(1), SpellId.of(Arcanomech.id("blink")));
    public static final Item SPELL_SCROLL_ARC_BOLT = new SpellScrollItem(new Item.Settings().maxCount(1), SpellId.of(Arcanomech.id("arc_bolt")));
    public static final Item SPELL_SCROLL_WARD = new SpellScrollItem(new Item.Settings().maxCount(1), SpellId.of(Arcanomech.id("ward")));
    public static final AspectCarrierItem ASPECT_SHARD = new AspectCarrierItem(new Item.Settings(), 1);
    public static final AspectCarrierItem ASPECT_PHIAL = new AspectCarrierItem(new Item.Settings().maxCount(1), 1);
    public static final AspectCarrierItem ASPECT_CRYSTAL = new AspectCarrierItem(new Item.Settings().maxCount(16), 8);
    public static final ItemGroup GROUP = Registry.register(
            Registries.ITEM_GROUP,
            new Identifier(Arcanomech.MOD_ID, "main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ETHER_CRYSTAL))
                    .displayName(Text.translatable("itemGroup.arcanomech.main"))
                    .entries((displayContext, entries) -> {
                        entries.add(ETHER_CRYSTAL);
                        entries.add(MANA_BATTERY);
                        entries.add(MANA_CABLE);
                        entries.add(CRUSHER);
                        entries.add(WRENCH);
                        entries.add(ARCANE_WORKBENCH);
                        entries.add(SPELL_TABLE);
                        entries.add(ALTAR_CORE);
                        entries.add(ALTAR_PEDESTAL);
                        entries.add(ARCANE_WAND);
                        entries.add(ARCANE_WAND_T2);
                        entries.add(BLANK_RUNE);
                        entries.add(MANA_RUNE);
                        entries.add(SPELL_SCROLL_BLINK);
                        entries.add(SPELL_SCROLL_ARC_BOLT);
                        entries.add(SPELL_SCROLL_WARD);
                        entries.add(ASPECT_SHARD);
                        entries.add(ASPECT_PHIAL);
                        entries.add(ASPECT_CRYSTAL);
                    })
                    .build()
    );

    private ModContent() {
    }

    public static void registerAll() {
        registerBlock("mana_battery", MANA_BATTERY, new Item.Settings());
        registerBlock("mana_cable", MANA_CABLE, new Item.Settings());
        registerBlock("crusher", CRUSHER, new Item.Settings());
        registerBlock("arcane_workbench", ARCANE_WORKBENCH, new Item.Settings());
        registerBlock("spell_table", SPELL_TABLE, new Item.Settings());
        registerBlock("altar_core", ALTAR_CORE, new Item.Settings());
        registerBlock("altar_pedestal", ALTAR_PEDESTAL, new Item.Settings());
        registerItem("ether_crystal", ETHER_CRYSTAL);
        registerItem("wrench", WRENCH);
        registerItem("arcane_wand", ARCANE_WAND);
        registerItem("arcane_wand_t2", ARCANE_WAND_T2);
        registerItem("blank_rune", BLANK_RUNE);
        registerItem("mana_rune", MANA_RUNE);
        registerItem("spell_scroll_blink", SPELL_SCROLL_BLINK);
        registerItem("spell_scroll_arc_bolt", SPELL_SCROLL_ARC_BOLT);
        registerItem("spell_scroll_ward", SPELL_SCROLL_WARD);
        registerItem("aspect_shard", ASPECT_SHARD);
        registerItem("aspect_phial", ASPECT_PHIAL);
        registerItem("aspect_crystal", ASPECT_CRYSTAL);
    }

    private static void registerItem(@NotNull String name, @NotNull Item item) {
        Registry.register(Registries.ITEM, new Identifier(Arcanomech.MOD_ID, name), item);
    }

    private static void registerBlock(@NotNull String name, @NotNull Block block, @NotNull Item.Settings itemSettings) {
        Registry.register(Registries.BLOCK, new Identifier(Arcanomech.MOD_ID, name), block);
        Registry.register(Registries.ITEM, new Identifier(Arcanomech.MOD_ID, name), new BlockItem(block, itemSettings));
    }
}
