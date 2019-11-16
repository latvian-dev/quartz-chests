package dev.latvian.mods.quartzchests.block;

import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class QuartzChestBlock extends HorizontalBlock
{
	private static final VoxelShape SHAPE = Block.makeCuboidShape(1, 0, 1, 15, 14, 15);

	public QuartzChestBlock(Properties properties)
	{
		super(properties);
		setDefaultState(stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH).with(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new QuartzChestEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
		return getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite()).with(BlockStateProperties.WATERLOGGED, ifluidstate.getFluid() == Fluids.WATER);
	}

	@Override
	@Deprecated
	public IFluidState getFluidState(BlockState state)
	{
		return state.get(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}

	@Override
	@Deprecated
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos)
	{
		if (state.get(BlockStateProperties.WATERLOGGED))
		{
			world.getPendingFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.updatePostPlacement(state, facing, facingState, world, pos, facingPos);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(HORIZONTAL_FACING, BlockStateProperties.WATERLOGGED);
	}

	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
}