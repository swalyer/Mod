package com.example.arcanomech;

import com.example.arcanomech.block.CrusherBlock;
import com.example.arcanomech.block.ManaBatteryBlock;
import com.example.arcanomech.block.ManaCableBlock;
import com.example.arcanomech.item.WrenchItem;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
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
    public static final Item ETHER_CRYSTAL = new Item(new Item.Settings());
    public static final Item WRENCH = new WrenchItem(new Item.Settings().maxCount(1));
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
                    })
                    .build()
    );

    private ModContent() {
    }

    public static void registerAll() {
        registerBlock("mana_battery", MANA_BATTERY, new Item.Settings());
        registerBlock("mana_cable", MANA_CABLE, new Item.Settings());
        registerBlock("crusher", CRUSHER, new Item.Settings());
        registerItem("ether_crystal", ETHER_CRYSTAL);
        registerItem("wrench", WRENCH);
    }

    private static void registerItem(@NotNull String name, @NotNull Item item) {
        Registry.register(Registries.ITEM, new Identifier(Arcanomech.MOD_ID, name), item);
    }

    private static void registerBlock(@NotNull String name, @NotNull Block block, @NotNull Item.Settings itemSettings) {
        Registry.register(Registries.BLOCK, new Identifier(Arcanomech.MOD_ID, name), block);
        Registry.register(Registries.ITEM, new Identifier(Arcanomech.MOD_ID, name), new BlockItem(block, itemSettings));
    }
}
