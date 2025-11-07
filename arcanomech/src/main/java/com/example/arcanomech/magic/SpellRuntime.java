package com.example.arcanomech.magic;

import java.util.Objects;

import com.example.arcanomech.Arcanomech;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SpellRuntime {
    private static final Text NOT_ENOUGH_MANA = Text.translatable("message.arcanomech.spell.not_enough_mana");
    private static final Text ON_COOLDOWN = Text.translatable("message.arcanomech.spell.on_cooldown");
    private static final Text UNKNOWN_SPELL = Text.translatable("message.arcanomech.spell.unknown");

    private SpellRuntime() {
    }

    public static boolean cast(SpellId id, SpellContext context) {
        Objects.requireNonNull(id, "Spell id");
        Objects.requireNonNull(context, "Spell context");
        if (!context.isServer()) {
            return false;
        }
        PlayerEntity caster = context.caster();
        SpellRegistry registry = SpellRegistry.getInstance();
        Spell spell = registry.getSpell(id).orElse(null);
        SpellConfig config = registry.getConfig(id).orElse(null);
        if (spell == null || config == null) {
            caster.sendMessage(UNKNOWN_SPELL, true);
            return false;
        }
        if (Cooldowns.isOnCooldown(caster, id)) {
            caster.sendMessage(ON_COOLDOWN, true);
            return false;
        }
        ItemStack stack = context.focus();
        if (!(stack.getItem() instanceof ManaToolItem tool)) {
            Arcanomech.LOGGER.warn("Attempted to cast {} without a mana tool", id.asString());
            return false;
        }
        if (tool.getMana(stack) < config.cost()) {
            caster.sendMessage(NOT_ENOUGH_MANA, true);
            return false;
        }
        if (!spell.cast(context, config)) {
            return false;
        }
        tool.extractMana(stack, config.cost());
        if (stack.getItem() instanceof ArcaneWandItem wand) {
            wand.setCooldownTicks(stack, config.cooldown());
        }
        Cooldowns.setCooldown(caster, id, config.cooldown());
        return true;
    }

    public static Text describe(Identifier id) {
        return Text.translatable("spell." + id.getNamespace() + "." + id.getPath());
    }
}
