package com.example.arcanomech.energy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class SideRaycast {
    private SideRaycast() {
    }

    public static Direction pickSide(PlayerEntity player, BlockPos pos, BlockHitResult hit) {
        if (hit != null) {
            return hit.getSide();
        }
        if (player == null) {
            return Direction.NORTH;
        }
        Vec3d eyePos = player.getEyePos();
        Vec3d center = Vec3d.ofCenter(pos);
        Vec3d diff = center.subtract(eyePos);
        return Direction.getFacing(diff.x, diff.y, diff.z);
    }
}
