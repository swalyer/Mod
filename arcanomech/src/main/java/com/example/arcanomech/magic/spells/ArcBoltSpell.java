package com.example.arcanomech.magic.spells;

import com.example.arcanomech.magic.Spell;
import com.example.arcanomech.magic.SpellConfig;
import com.example.arcanomech.magic.SpellContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class ArcBoltSpell implements Spell {
    private final Identifier id;

    public ArcBoltSpell(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public boolean cast(SpellContext context, SpellConfig config) {
        PlayerEntity caster = context.caster();
        double range = Math.max(1.0D, config.getDouble("range", 12.0D));
        Vec3d start = caster.getCameraPosVec(1.0F);
        Vec3d direction = caster.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(range));

        EntityHitResult entityResult = raycastEntity(caster, start, end, range);
        if (entityResult == null || !(entityResult.getEntity() instanceof LivingEntity target)) {
            return false;
        }
        double damage = Math.max(0.0D, config.getDouble("damage", 4.0D));
        double knockback = Math.max(0.0D, config.getDouble("knockback", 0.2D));
        target.damage(context.world().getDamageSources().indirectMagic(caster, caster), (float) damage);
        Vec3d knockbackVector = direction.normalize().multiply(knockback);
        target.addVelocity(knockbackVector.x, 0.1D, knockbackVector.z);
        target.velocityDirty = true;
        return true;
    }

    private EntityHitResult raycastEntity(PlayerEntity caster, Vec3d start, Vec3d end, double range) {
        Box box = caster.getBoundingBox().stretch(caster.getRotationVec(1.0F).multiply(range)).expand(1.0D, 1.0D, 1.0D);
        return ProjectileUtil.raycast(caster, start, end, box, entity -> canHit(caster, entity), range * range);
    }

    private boolean canHit(PlayerEntity caster, Entity entity) {
        if (!entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        if (entity == caster) {
            return false;
        }
        return entity instanceof LivingEntity && entity.canHit();
    }
}
