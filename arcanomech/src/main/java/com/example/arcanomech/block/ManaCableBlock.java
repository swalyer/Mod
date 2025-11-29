package com.example.arcanomech.block;

import java.util.EnumMap;
import java.util.Map;

import com.example.arcanomech.content.ModBlockEntities;
import com.example.arcanomech.energy.IOMode;
import com.example.arcanomech.energy.ManaApi;
import com.example.arcanomech.energy.SideConfigHolder;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ManaCableBlock extends BlockWithEntity {
    private static final Map<Direction, BooleanProperty> CONNECTIONS = new EnumMap<>(Direction.class);

    static {
        CONNECTIONS.put(Direction.NORTH, Properties.NORTH);
        CONNECTIONS.put(Direction.SOUTH, Properties.SOUTH);
        CONNECTIONS.put(Direction.EAST, Properties.EAST);
        CONNECTIONS.put(Direction.WEST, Properties.WEST);
        CONNECTIONS.put(Direction.UP, Properties.UP);
        CONNECTIONS.put(Direction.DOWN, Properties.DOWN);
    }

    public ManaCableBlock() {
        super(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(1.0F, 3.0F).requiresTool());
        BlockState state = getStateManager().getDefaultState();
        for (BooleanProperty property : CONNECTIONS.values()) {
            state = state.with(property, false);
        }
        setDefaultState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        CONNECTIONS.values().forEach(builder::add);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        return updateConnections(world, pos, getDefaultState());
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ManaCableBlockEntity(pos, state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            com.example.arcanomech.energy.net.ManaNetworkManager.markDirty(world, pos);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) {
            return null;
        }
        return checkType(type, ModBlockEntities.MANA_CABLE, ManaCableBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.isSneaking()) {
            return ActionResult.PASS;
        }
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isEmpty()) {
            return ActionResult.PASS;
        }
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ManaCableBlockEntity cable) {
                player.sendMessage(Text.literal("Mana: " + cable.getMana() + "/" + cable.getCapacity()), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (world instanceof World fullWorld) {
            state = updateConnections(fullWorld, pos, state);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!world.isClient) {
            world.setBlockState(pos, updateConnections(world, pos, state), Block.NOTIFY_LISTENERS);
            com.example.arcanomech.energy.net.ManaNetworkManager.markDirty(world, pos);
        }
        super.onPlaced(world, pos, state, placer, stack);
    }

    public BlockState updateConnections(World world, BlockPos pos, BlockState state) {
        BlockState result = state;
        for (Direction direction : Direction.values()) {
            BooleanProperty property = CONNECTIONS.get(direction);
            boolean connected = shouldConnect(world, pos, direction);
            result = result.with(property, connected);
        }
        return result;
    }

    private boolean shouldConnect(World world, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SideConfigHolder holder) {
            if (holder.getSideConfig().get(direction) == IOMode.DISABLED) {
                return false;
            }
        }
        return ManaApi.findNeighborStorage(world, pos, direction).isPresent();
    }
}
