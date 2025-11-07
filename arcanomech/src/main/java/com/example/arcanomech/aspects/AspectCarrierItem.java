package com.example.arcanomech.aspects;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class AspectCarrierItem extends Item {
    private static final String ASPECT_KEY = "Aspect";
    private final int capacity;

    public AspectCarrierItem(Settings settings, int capacity) {
        super(settings);
        this.capacity = capacity;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip) {
        Identifier aspectId = getAspectId(stack);
        if (aspectId != null) {
            tooltip.add(Text.translatable("tooltip.arcanomech.aspect", Text.translatable("aspect." + aspectId.getNamespace() + "." + aspectId.getPath())).formatted(Formatting.GRAY));
        }
        tooltip.add(Text.translatable("tooltip.arcanomech.aspect_units", capacity).formatted(Formatting.DARK_AQUA));
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        Identifier aspectId = getAspectId(stack);
        if (aspectId != null) {
            return super.getTranslationKey(stack) + "." + aspectId.getNamespace() + "_" + aspectId.getPath();
        }
        return super.getTranslationKey(stack);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (isIn(group)) {
            for (Aspect aspect : AspectRegistry.getInstance().getAspects()) {
                stacks.add(withAspect(aspect.id()));
            }
        }
    }

    public ItemStack withAspect(Identifier id) {
        ItemStack stack = new ItemStack(this);
        setAspect(stack, id);
        return stack;
    }

    public void setAspect(ItemStack stack, Identifier id) {
        if (id != null) {
            stack.getOrCreateNbt().putString(ASPECT_KEY, id.toString());
        }
    }

    public Identifier getAspectId(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains(ASPECT_KEY)) {
            return new Identifier(stack.getNbt().getString(ASPECT_KEY));
        }
        return null;
    }

    public int getCapacityUnits() {
        return capacity;
    }
}
