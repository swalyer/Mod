package com.example.arcanomech.commands;

import com.example.arcanomech.energy.Balance;
import com.example.arcanomech.energy.net.ManaComponent;
import com.example.arcanomech.energy.net.ManaNetworkManager;
import com.example.arcanomech.energy.net.ManaNode;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ManaInfoCommand {
    private ManaInfoCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("am")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("mana")
                        .then(CommandManager.literal("info")
                                .executes(context -> execute(context, BlockPos.ofFloored(context.getSource().getPosition())))
                                .then(CommandManager.argument("pos", net.minecraft.command.argument.BlockPosArgumentType.blockPos())
                                        .executes(context -> execute(context, net.minecraft.command.argument.BlockPosArgumentType.getLoadedBlockPos(context, "pos")))))));
    }

    private static int execute(CommandContext<ServerCommandSource> context, BlockPos pos) {
        ServerCommandSource source = context.getSource();
        if (!(source.getWorld().getBlockEntity(pos) instanceof ManaNode)) {
            source.sendFeedback(() -> Text.literal("No mana node at " + pos), false);
            return 0;
        }
        ManaComponent component = ManaNetworkManager.getComponent(source.getWorld(), pos).orElse(null);
        if (component == null) {
            source.sendFeedback(() -> Text.literal("No mana component found"), false);
            return 0;
        }
        int totalCapacity = component.getTotalCapacity();
        int totalMana = component.getTotalMana();
        double fill = component.getFillRatio() * 100.0D;
        int nodeCount = component.getNodes().size();
        ManaNode node = component.getNodes().getOrDefault(pos, null);
        int ioLimit = node != null ? node.getMaxIoPerTick() : Balance.CABLE_IO;
        int nodeCapacity = node != null ? node.getCapacity() : 0;
        int nodeStored = node != null ? node.getStored() : 0;
        double nodeFill = nodeCapacity > 0 ? (double) nodeStored / (double) nodeCapacity * 100.0D : 0.0D;
        source.sendFeedback(() -> Text.literal("Component #" + component.getId() + " nodes=" + nodeCount + " mana=" + totalMana + "/" + totalCapacity + " fill=" + String.format("%.2f", fill) + "% target=" + String.format("%.2f", fill) + "% node=" + String.format("%.2f", nodeFill) + "% io/t=" + ioLimit), false);
        return 1;
    }
}
