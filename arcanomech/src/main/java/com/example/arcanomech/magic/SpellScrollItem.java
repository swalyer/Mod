package com.example.arcanomech.magic;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class SpellScrollItem extends Item {
    private final SpellId spellId;

    public SpellScrollItem(Settings settings, SpellId spellId) {
        super(settings);
        this.spellId = spellId;
    }

    public SpellId getSpellId() {
        return spellId;
    }

    @Override
    public void appendTooltip(net.minecraft.item.ItemStack stack,
                              @org.jetbrains.annotations.Nullable net.minecraft.world.World world,
                              java.util.List<net.minecraft.text.Text> tooltip,
                              net.minecraft.client.item.TooltipContext context) {
        tooltip.add(net.minecraft.text.Text.translatable(
                "tooltip.arcanomech.spell_scroll",
                com.example.arcanomech.magic.SpellRuntime.describe(spellId.id())
        ));
    }
}
