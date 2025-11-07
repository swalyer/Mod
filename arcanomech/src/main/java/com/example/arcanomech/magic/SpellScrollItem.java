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
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip) {
        tooltip.add(Text.translatable("tooltip.arcanomech.spell_scroll", SpellRuntime.describe(spellId.id())));
    }
}
