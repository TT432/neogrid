package io.github.tt432.neogrid.block;

import io.github.tt432.neogrid.NeoGrid;
import io.github.tt432.neogrid.block.entity.ElectricPoleBlockEntity;
import io.github.tt432.neogrid.block.entity.ElectricPoleBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class ElectricPoleBlock extends Block implements EntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private final int maxTransferRate;
    private final int connectionRange;
    private final float wireColorR;
    private final float wireColorG;
    private final float wireColorB;

    public ElectricPoleBlock(Properties properties) {
        this(properties, 1000, 10, 0.0F, 0.5F, 1.0F);
    }

    public ElectricPoleBlock(Properties properties, int maxTransferRate, int connectionRange, float wireColorR, float wireColorG, float wireColorB) {
        super(properties);
        this.maxTransferRate = maxTransferRate;
        this.connectionRange = connectionRange;
        this.wireColorR = wireColorR;
        this.wireColorG = wireColorG;
        this.wireColorB = wireColorB;
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public int getMaxTransferRate() {
        return maxTransferRate;
    }

    public int getConnectionRange() {
        return connectionRange;
    }

    public float getWireColorR() {
        return wireColorR;
    }

    public float getWireColorG() {
        return wireColorG;
    }

    public float getWireColorB() {
        return wireColorB;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            return neighborState.is(this) && neighborState.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        } else {
            return half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        if (blockpos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            preventCreativeDropFromBottomPart(level, pos, state, player);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    protected static void preventCreativeDropFromBottomPart(Level level, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf doubleblockhalf = state.getValue(HALF);
        if (doubleblockhalf == DoubleBlockHalf.UPPER) {
            BlockPos blockpos = pos.below();
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(state.getBlock()) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockState blockstate1 = blockstate.getFluidState().isSource() ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                level.setBlock(blockpos, blockstate1, 35);
                level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
            }
        }

    }
    
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return super.canSurvive(state, level, pos);
        } else {
            BlockState blockstate = level.getBlockState(pos.below());
            if (state.getBlock() != this) return super.canSurvive(state, level, pos); 
            return blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) return null;
        // Look up the BlockEntityType for this specific block variant
        var type = ElectricPoleBlockEntityTypes.getType(this);
        if (type != null) {
            return new ElectricPoleBlockEntity(type, pos, state);
        }
        // Fallback to the default type (for the vanilla neogrid pole)
        return new ElectricPoleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) return null;
        // Accept any registered ElectricPoleBlockEntity type
        var expectedType = ElectricPoleBlockEntityTypes.getType(this);
        if (expectedType == null) {
            expectedType = NeoGrid.ELECTRIC_POLE_BLOCK_ENTITY.get();
        }
        if (blockEntityType != expectedType) return null;
        return (lvl, pos, st, be) -> ElectricPoleBlockEntity.tick(lvl, pos, st, (ElectricPoleBlockEntity) be);
    }
}
