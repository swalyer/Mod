package com.example.arcanomech.magic.spells;

import com.example.arcanomech.magic.Spell;
import com.example.arcanomech.magic.SpellConfig;
import com.example.arcanomech.magic.SpellContext;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class BlinkSpell implements Spell {
    private final Identifier id;

    public BlinkSpell(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public boolean cast(SpellContext context, SpellConfig config) {
        PlayerEntity caster = context.caster();
        int range = Math.max(1, config.getInt("range", 8));
        Vec3d start = caster.getPos();
        Vec3d direction = caster.getRotationVec(1.0F).normalize();
        double bestDistance = 0;
        Vec3d bestPos = null;
        for (double distance = range; distance >= 0.5; distance -= 0.5) {
            Vec3d candidate = start.add(direction.multiply(distance));
            Box box = caster.getBoundingBox().offset(candidate.subtract(start));
            if (context.world().isSpaceEmpty(caster, box)) {
                bestDistance = distance;
                bestPos = candidate;
                break;
            }
        }
        if (bestPos == null) {
            return false;
        }
        caster.fallDistance = 0.0F;
        if (caster instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.requestTeleport(bestPos.x, bestPos.y, bestPos.z, caster.getYaw(), caster.getPitch());
        } else {
            caster.setPosition(bestPos);
        }
        return bestDistance > 0;
    }
}
