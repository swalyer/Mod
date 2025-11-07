package com.example.arcanomech.client;

import com.example.arcanomech.ModContent;
import com.example.arcanomech.energy.IOMode;
import com.example.arcanomech.energy.SideConfigHolder;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.client.render.WorldRenderer;

public final class WrenchOverlayRenderer {
    private WrenchOverlayRenderer() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
            World world = client.world;
            HitResult hitResult = client.crosshairTarget;
            if (player == null || world == null || !(hitResult instanceof BlockHitResult blockHit)) {
                return;
            }
            if (!isHoldingWrench(player)) {
                return;
            }
            BlockPos pos = blockHit.getBlockPos();
            if (!(world.getBlockEntity(pos) instanceof SideConfigHolder holder)) {
                return;
            }
            Direction side = blockHit.getSide();
            IOMode mode = holder.getSideConfig().get(side);
            float[] color = colorFor(mode);
            if (color == null) {
                return;
            }
            VertexConsumerProvider consumers = context.consumers();
            if (consumers == null) {
                return;
            }
            MatrixStack matrices = context.matrixStack();
            matrices.push();
            Vec3d camera = context.camera().getPos();
            matrices.translate(-camera.x, -camera.y, -camera.z);
            BoxData box = createBox(pos, side);
            VertexConsumer consumer = consumers.getBuffer(RenderLayer.getLines());
            WorldRenderer.drawBox(matrices, consumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color[0], color[1], color[2], 1.0F);
            matrices.pop();
        });
    }

    private static boolean isHoldingWrench(PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();
        return main.isOf(ModContent.WRENCH) || off.isOf(ModContent.WRENCH);
    }

    private static float[] colorFor(IOMode mode) {
        return switch (mode) {
            case INPUT -> new float[]{0.2F, 0.9F, 0.2F};
            case OUTPUT -> new float[]{1.0F, 0.6F, 0.0F};
            case BOTH -> new float[]{0.0F, 0.8F, 1.0F};
            case DISABLED -> new float[]{0.6F, 0.6F, 0.6F};
        };
    }

    private static BoxData createBox(BlockPos pos, Direction side) {
        double size = 0.2D;
        double half = size / 2.0D;
        Vec3d center = Vec3d.ofCenter(pos).add(Vec3d.of(side.getVector()).multiply(0.51D));
        return new BoxData(
                center.x - half,
                center.y - half,
                center.z - half,
                center.x + half,
                center.y + half,
                center.z + half
        );
    }

    private record BoxData(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    }
}
