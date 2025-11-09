package com.example.arcanomech.aspects;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class AspectCarrierItem extends Item {
    private static final String ASPECT_KEY = "Aspect";
    private final int capacity;

    public AspectCarrierItem(Settings settings, int capacity) {
        super(settings);
        this.capacity = capacity;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Identifier aspectId = getAspectId(stack);
        if (aspectId != null) {
            tooltip.add(Text.translatable(
                    "tooltip.arcanomech.aspect",
                    Text.translatable("aspect." + aspectId.getNamespace() + "." + aspectId.getPath())
            ).formatted(Formatting.GRAY));
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

    @Nullable
    public Identifier getAspectId(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(ASPECT_KEY)) {
            return new Identifier(nbt.getString(ASPECT_KEY));
        }
        return null;
    }

    public int getCapacityUnits() {
        return capacity;
    }
}
