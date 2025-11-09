package com.example.arcanomech.magic;

import java.util.List;

import com.example.arcanomech.energy.Balance;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;


public class ArcaneWandItem extends Item implements ManaToolItem {
    private static final String MANA_KEY = "Mana";
    private static final String SPELL_KEY = "Spell";
    private static final String COOLDOWN_KEY = "Cooldown";

    public ArcaneWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (hand == Hand.OFF_HAND) {
            return TypedActionResult.pass(stack);
        }
        if (world.isClient) {
            return TypedActionResult.success(stack, true);
        }
        String spellKey = getSelectedSpell(stack);
        if (spellKey.isEmpty()) {
            user.sendMessage(Text.translatable("message.arcanomech.spell.none"), true);
            return TypedActionResult.pass(stack);
        }
        SpellId id = SpellId.of(spellKey);
        SpellContext context = new SpellContext(world, user, stack, CastingSource.WAND);
        boolean cast = SpellRuntime.cast(id, context);
        return cast ? TypedActionResult.consume(stack) : TypedActionResult.pass(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip) {
        tooltip.add(Text.translatable("tooltip.arcanomech.wand_mana", getMana(stack), getCapacity(stack)));
        String spellKey = getSelectedSpell(stack);
        Text spellName = spellKey.isEmpty() ? Text.translatable("tooltip.arcanomech.wand_spell_none") : SpellRuntime.describe(SpellId.of(spellKey).id());
        tooltip.add(Text.translatable("tooltip.arcanomech.wand_spell", spellName));
        int cooldown = getCooldownTicks(stack);
        if (cooldown <= 0) {
            tooltip.add(Text.translatable("tooltip.arcanomech.wand_cooldown_ready"));
        } else {
            tooltip.add(Text.translatable("tooltip.arcanomech.wand_cooldown_wait", String.format("%.1f", cooldown / 20.0F)));
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.CROSSBOW;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
                super.inventoryTick(stack, world, entity, slot, selected);
                if (!world.isClient && entity instanceof PlayerEntity) {
                        int cooldown = getCooldownTicks(stack);
                        if (cooldown > 0) {
                                setCooldownTicks(stack, cooldown - 1);
                           }
                    }
            }

    @Override
    public int getMana(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(MANA_KEY);
    }

    @Override
    public int getCapacity(ItemStack stack) {
        return Balance.WAND_CAPACITY;
    }

    @Override
    public int insertMana(ItemStack stack, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int mana = getMana(stack);
        int capacity = getCapacity(stack);
        int space = capacity - mana;
        if (space <= 0) {
            return 0;
        }
        int accepted = Math.min(space, amount);
        if (!simulate) {
            setMana(stack, mana + accepted);
        }
        return accepted;
    }

    @Override
    public int extractMana(ItemStack stack, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int mana = getMana(stack);
        int extracted = Math.min(mana, amount);
        if (extracted > 0) {
            setMana(stack, mana - extracted);
        }
        return extracted;
    }

    public void setMana(ItemStack stack, int value) {
        stack.getOrCreateNbt().putInt(MANA_KEY, Math.max(0, Math.min(getCapacity(stack), value)));
    }

    public void setSelectedSpell(ItemStack stack, String id) {
        stack.getOrCreateNbt().putString(SPELL_KEY, id);
    }

    public String getSelectedSpell(ItemStack stack) {
        return stack.getOrCreateNbt().getString(SPELL_KEY);
    }

    public void setCooldownTicks(ItemStack stack, int ticks) {
        stack.getOrCreateNbt().putInt(COOLDOWN_KEY, Math.max(0, ticks));
    }

    public int getCooldownTicks(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(COOLDOWN_KEY);
    }
}
