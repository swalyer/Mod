package com.example.arcanomech;

import java.util.function.Supplier;

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
    public static final Block MANA_BATTERY = new Block(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(3.0F, 6.0F).requiresTool());
    public static final Item ETHER_CRYSTAL = new Item(new Item.Settings());
    public static final ItemGroup GROUP = FabricItemGroup.builder(new Identifier(Arcanomech.MOD_ID, "main"))
            .icon(new Supplier<ItemStack>() {
                @Override
                public ItemStack get() {
                    return new ItemStack(ETHER_CRYSTAL);
                }
            })
            .displayName(Text.translatable("itemGroup.arcanomech.main"))
            .entries((displayContext, entries) -> {
                entries.add(ETHER_CRYSTAL);
                entries.add(MANA_BATTERY);
            })
            .build();

    private ModContent() {
    }

    public static void registerAll() {
        registerBlock("mana_battery", MANA_BATTERY, new Item.Settings());
        registerItem("ether_crystal", ETHER_CRYSTAL);
        Registry.register(Registries.ITEM_GROUP, new Identifier(Arcanomech.MOD_ID, "main"), GROUP);
    }

    private static void registerItem(@NotNull String name, @NotNull Item item) {
        Registry.register(Registries.ITEM, new Identifier(Arcanomech.MOD_ID, name), item);
    }

    private static void registerBlock(@NotNull String name, @NotNull Block block, @NotNull Item.Settings itemSettings) {
        Registry.register(Registries.BLOCK, new Identifier(Arcanomech.MOD_ID, name), block);
        Registry.register(Registries.ITEM, new Identifier(Arcanomech.MOD_ID, name), new BlockItem(block, itemSettings));
    }
}
