package dev.latvian.mods.quartzchests.block;

import dev.latvian.mods.quartzchests.block.entity.ColorType;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import dev.latvian.mods.quartzchests.gui.QuartzChestContainer;
import dev.latvian.mods.quartzchests.item.QuartzChestsItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
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
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

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
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	@Deprecated
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isToolEffective(BlockState state, ToolType tool)
	{
		return tool == ToolType.PICKAXE || tool == ToolType.AXE;
	}

	@Override
	@Deprecated
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
	{
		if (world.isRemote || player.isSpectator())
		{
			return ActionResultType.SUCCESS;
		}

		TileEntity entity = world.getTileEntity(pos);

		if (entity instanceof QuartzChestEntity)
		{
			QuartzChestEntity chest = (QuartzChestEntity) entity;
			ItemStack stack = player.getHeldItem(hand);
			Item item = stack.getItem();

			if (item instanceof NameTagItem && stack.hasDisplayName())
			{
				chest.label = stack.getDisplayName().getString();
				chest.markDirty();
				world.markAndNotifyBlock(pos, null, state, state, Constants.BlockFlags.DEFAULT_AND_RERENDER);
				return ActionResultType.SUCCESS;
			}
			else if (item instanceof DyeItem)
			{
				if (!chest.label.isEmpty() && rayTraceResult.getFace() == state.get(HORIZONTAL_FACING) && (rayTraceResult.getHitVec().y - pos.getY()) > 0.6D)
				{
					chest.colors[ColorType.TEXT.index] = 0xFF000000 | ((DyeItem) item).getDyeColor().getColorValue();
				}
				else
				{
					chest.colors[ColorType.CHEST.index] = 0xFF000000 | ((DyeItem) item).getDyeColor().getColorValue();
				}

				chest.markDirty();
				world.markAndNotifyBlock(pos, null, state, state, Constants.BlockFlags.DEFAULT_AND_RERENDER);
				return ActionResultType.SUCCESS;
			}
			else if (item == QuartzChestsItems.KEEP_INVENTORY_UPGRADE)
			{
				if (!chest.keepInventory)
				{
					chest.keepInventory = true;
					chest.markDirty();
					stack.shrink(1);
				}

				return ActionResultType.SUCCESS;
			}
			else if (item == QuartzChestsItems.GLOWING_TEXT_UPGRADE)
			{
				if (!chest.textGlow)
				{
					chest.textGlow = true;
					chest.markDirty();
					stack.shrink(1);
				}

				return ActionResultType.SUCCESS;
			}
			else if (item == QuartzChestsItems.BOLD_TEXT_UPGRADE)
			{
				if (!chest.textBold)
				{
					chest.textBold = true;
					chest.markDirty();
					stack.shrink(1);
				}

				return ActionResultType.SUCCESS;
			}
			else if (item == QuartzChestsItems.ITALIC_TEXT_UPGRADE)
			{
				if (!chest.textItalic)
				{
					chest.textItalic = true;
					chest.markDirty();
					stack.shrink(1);
				}

				return ActionResultType.SUCCESS;
			}

			NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider()
			{
				@Override
				public ITextComponent getDisplayName()
				{
					return chest.getDisplayName();
				}

				@Override
				public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player)
				{
					return new QuartzChestContainer(id, playerInventory, chest);
				}
			}, pos);
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player)
	{
		TileEntity entity = world.getTileEntity(pos);

		if (entity instanceof QuartzChestEntity)
		{
			QuartzChestEntity chest = (QuartzChestEntity) entity;

			if (!world.isRemote && player.isCreative())
			{
				CompoundNBT data = new CompoundNBT();
				chest.writeData(data);

				if (!data.isEmpty())
				{
					ItemStack stack = new ItemStack(this);
					stack.setTagInfo("BlockEntityTag", data);
					ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
					itemEntity.setDefaultPickupDelay();
					world.addEntity(itemEntity);
				}
			}
		}

		super.onBlockHarvested(world, pos, state, player);
	}

	@Override
	@Deprecated
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity entity = world.getTileEntity(pos);

			if (entity instanceof QuartzChestEntity)
			{
				QuartzChestEntity chest = (QuartzChestEntity) entity;

				if (!chest.keepInventory)
				{
					for (int i = 0; i < chest.inventory.getSlots(); i++)
					{
						InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), chest.inventory.getStackInSlot(i));
						chest.inventory.setStackInSlot(i, ItemStack.EMPTY);
					}
				}
			}

			super.onReplaced(state, world, pos, newState, isMoving);
		}
	}

	@Override
	@Deprecated
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		TileEntity entity = builder.get(LootParameters.BLOCK_ENTITY);

		if (entity instanceof QuartzChestEntity)
		{
			QuartzChestEntity chest = (QuartzChestEntity) entity;

			if (chest.keepInventory)
			{
				builder = builder.withDynamicDrop(CONTENTS, (context, callback) -> {
					for (int i = 0; i < chest.inventory.getSlots(); i++)
					{
						callback.accept(chest.inventory.getStackInSlot(i));
					}
				});
			}
		}

		return super.getDrops(state, builder);
	}

	@Override
	@Deprecated
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type)
	{
		return false;
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

		tooltip.add(new StringTextComponent("").appendSibling(new TranslationTextComponent("block.quartzchests.chest.label").applyTextStyle(TextFormatting.GRAY).appendText(": ")).appendSibling(dummy.getDisplayName().applyTextStyle(TextFormatting.YELLOW)));

		for (ColorType type : ColorType.VALUES)
		{
			tooltip.add(new StringTextComponent("").appendSibling(new TranslationTextComponent(type.translationKey).applyTextStyle(TextFormatting.GRAY).appendText(": ")).appendSibling(new StringTextComponent(String.format("#%06X", dummy.colors[type.index])).applyTextStyle(TextFormatting.YELLOW)));
		}

		tooltip.add(new StringTextComponent("").appendSibling(new TranslationTextComponent("block.quartzchests.chest.icon").applyTextStyle(TextFormatting.GRAY).appendText(": ")).appendSibling(dummy.icon.getDisplayName().deepCopy().applyTextStyle(TextFormatting.YELLOW)));

		if (dummy.keepInventory && data != null)
		{
			tooltip.add(new StringTextComponent("").appendSibling(new TranslationTextComponent("block.quartzchests.chest.slots_used", data.getList("items", Constants.NBT.TAG_COMPOUND).size(), 54).applyTextStyle(TextFormatting.DARK_GRAY)));
		}

		if (dummy.keepInventory || dummy.textGlow || dummy.textBold || dummy.textItalic)
		{
			tooltip.add(new TranslationTextComponent("block.quartzchests.chest.upgrades").applyTextStyle(TextFormatting.AQUA));

			if (dummy.keepInventory)
			{
				tooltip.add(new StringTextComponent("+ ").applyTextStyle(TextFormatting.GREEN).appendSibling(new TranslationTextComponent("item.quartzchests.keep_inventory_upgrade")));
			}

			if (dummy.textGlow)
			{
				tooltip.add(new StringTextComponent("+ ").applyTextStyle(TextFormatting.GREEN).appendSibling(new TranslationTextComponent("item.quartzchests.glowing_text_upgrade")));
			}

			if (dummy.textBold)
			{
				tooltip.add(new StringTextComponent("+ ").applyTextStyle(TextFormatting.GREEN).appendSibling(new TranslationTextComponent("item.quartzchests.bold_text_upgrade")));
			}

			if (dummy.textItalic)
			{
				tooltip.add(new StringTextComponent("+ ").applyTextStyle(TextFormatting.GREEN).appendSibling(new TranslationTextComponent("item.quartzchests.italic_text_upgrade")));
			}
		}
	}
}