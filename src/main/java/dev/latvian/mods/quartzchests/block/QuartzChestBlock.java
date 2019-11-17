package dev.latvian.mods.quartzchests.block;

import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class QuartzChestBlock extends HorizontalBlock
{
	private static final VoxelShape SHAPE = Block.makeCuboidShape(1, 0, 1, 15, 14, 15);
	public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
	private QuartzChestEntity dummy;

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
	@Deprecated
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	@Deprecated
	public ItemStack getItem(IBlockReader world, BlockPos pos, BlockState state)
	{
		ItemStack stack = new ItemStack(this);

		TileEntity entity = world.getTileEntity(pos);

		if (entity instanceof QuartzChestEntity)
		{
			QuartzChestEntity chest = (QuartzChestEntity) entity;
			//stack.setTagInfo("", new IntNBT(chest.));
		}

		return stack;
	}

	@Override
	@Deprecated
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
	{
		if (worldIn.isRemote || player.isSpectator())
		{
			return true;
		}

		TileEntity entity = worldIn.getTileEntity(pos);

		if (entity instanceof QuartzChestEntity)
		{
			QuartzChestEntity chest = (QuartzChestEntity) entity;
			ItemStack item = player.getHeldItem(hand);

			if (item.getItem() instanceof NameTagItem && item.hasDisplayName())
			{
				chest.label = item.getDisplayName().getString();
			}
			else if (item.getItem() instanceof DyeItem)
			{
				if (hit.getFace().getAxis().isHorizontal() && (hit.getHitVec().y - pos.getY()) > 0.6D)
				{
					chest.textColor = 0xFF000000 | ((DyeItem) item.getItem()).getDyeColor().getColorValue();
				}
				else
				{
					chest.color = 0xFF000000 | ((DyeItem) item.getItem()).getDyeColor().getColorValue();
				}
			}
			else
			{
				chest.icon = item;
			}

			entity.markDirty();
			worldIn.markAndNotifyBlock(pos, null, state, state, Constants.BlockFlags.DEFAULT_AND_RERENDER);
		}

		return true;
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		TileEntity entity = worldIn.getTileEntity(pos);
		if (entity instanceof QuartzChestEntity)
		{
			QuartzChestEntity chest = (QuartzChestEntity) entity;

			if (!worldIn.isRemote && player.isCreative())
			{
				CompoundNBT data = new CompoundNBT();
				chest.writeData(data);

				if (!data.isEmpty())
				{
					ItemStack stack = new ItemStack(this);
					stack.setTagInfo("BlockEntityTag", data);
					ItemEntity itemEntity = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
					itemEntity.setDefaultPickupDelay();
					worldIn.addEntity(itemEntity);
				}
			}
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	@Deprecated
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		TileEntity entity = builder.get(LootParameters.BLOCK_ENTITY);

		if (entity instanceof QuartzChestEntity)
		{
			QuartzChestEntity chest = (QuartzChestEntity) entity;

			builder = builder.withDynamicDrop(CONTENTS, (context, callback) -> {
				for (int i = 0; i < chest.inventory.getSlots(); i++)
				{
					callback.accept(chest.inventory.getStackInSlot(i));
				}
			});
		}

		return super.getDrops(state, builder);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		super.addInformation(stack, world, tooltip, flag);

		if (dummy == null)
		{
			dummy = new QuartzChestEntity();
		}

		CompoundNBT data = stack.getChildTag("BlockEntityTag");
		dummy.readData(data == null ? new CompoundNBT() : data);

		tooltip.add(new StringTextComponent("").appendSibling(new TranslationTextComponent("block.quartzchests.chest.label").applyTextStyle(TextFormatting.GRAY).appendText(": ")).appendSibling(new StringTextComponent(dummy.label).applyTextStyle(TextFormatting.YELLOW)));
		tooltip.add(new StringTextComponent("").appendSibling(new TranslationTextComponent("block.quartzchests.chest.color").applyTextStyle(TextFormatting.GRAY).appendText(": ")).appendSibling(new StringTextComponent(String.format("#%06X", dummy.color)).applyTextStyle(TextFormatting.YELLOW)));
		tooltip.add(new StringTextComponent("").appendSibling(new TranslationTextComponent("block.quartzchests.chest.text_color").applyTextStyle(TextFormatting.GRAY).appendText(": ")).appendSibling(new StringTextComponent(String.format("#%06X", dummy.textColor)).applyTextStyle(TextFormatting.YELLOW)));
		tooltip.add(new StringTextComponent("").appendSibling(new TranslationTextComponent("block.quartzchests.chest.icon").applyTextStyle(TextFormatting.GRAY).appendText(": ")).appendSibling(dummy.icon.getDisplayName().deepCopy().applyTextStyle(TextFormatting.YELLOW)));
	}
}