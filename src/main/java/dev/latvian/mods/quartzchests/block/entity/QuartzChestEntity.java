package dev.latvian.mods.quartzchests.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class QuartzChestEntity extends TileEntity implements INameable, ITickableTileEntity
{
	private boolean isDirty, sendUpdate;
	public double lidAngle;
	public double prevLidAngle;
	public String label;
	public int[] colors;
	public ItemStack icon;
	public ItemStackHandler inventory;
	private final LazyOptional<IItemHandler> inventoryCap;
	public int openContainers;
	public boolean textGlow;
	public boolean textBold;
	public boolean textItalic;
	public boolean keepInventory;

	public QuartzChestEntity()
	{
		super(QuartzChestsBlockEntities.CHEST.get());
		isDirty = sendUpdate = false;
		label = "";
		colors = new int[ColorType.VALUES.length];

		for (ColorType type : ColorType.VALUES)
		{
			colors[type.index] = type.defaultColor;
		}

		icon = ItemStack.EMPTY;

		inventory = new ItemStackHandler(54)
		{
			@Override
			protected void onContentsChanged(int slot)
			{
				isDirty = true;
			}
		};

		inventoryCap = LazyOptional.of(() -> inventory);
		openContainers = 0;
		textGlow = false;
		textBold = false;
		textItalic = false;
		keepInventory = false;
	}

	public void writeVisualData(CompoundNBT nbt)
	{
		if (!label.isEmpty())
		{
			nbt.putString("label", label);
		}

		for (ColorType type : ColorType.VALUES)
		{
			colors[type.index] &= 0xFFFFFF;

			if (colors[type.index] != type.defaultColor)
			{
				nbt.putInt(type.nbt, colors[type.index]);
			}
		}

		if (!icon.isEmpty())
		{
			nbt.put("icon", icon.write(new CompoundNBT()));
		}

		if (textGlow)
		{
			nbt.putBoolean("text_glow", true);
		}

		if (textBold)
		{
			nbt.putBoolean("text_bold", true);
		}

		if (textItalic)
		{
			nbt.putBoolean("text_italic", true);
		}

		if (keepInventory)
		{
			nbt.putBoolean("keep_inventory", true);
		}
	}

	public void writeData(CompoundNBT nbt)
	{
		writeVisualData(nbt);

		ListNBT items = inventory.serializeNBT().getList("Items", Constants.NBT.TAG_COMPOUND);

		if (!items.isEmpty())
		{
			nbt.put("items", items);
		}
	}

	public void readVisualData(CompoundNBT nbt)
	{
		label = nbt.getString("label");

		for (ColorType type : ColorType.VALUES)
		{
			colors[type.index] = type.defaultColor;

			if (nbt.contains(type.nbt))
			{
				colors[type.index] = 0xFFFFFF & nbt.getInt(type.nbt);
			}
		}

		icon = nbt.contains("icon") ? ItemStack.read(nbt.getCompound("icon")) : ItemStack.EMPTY;
		textGlow = nbt.getBoolean("text_glow");
		textBold = nbt.getBoolean("text_bold");
		textItalic = nbt.getBoolean("text_italic");
		keepInventory = nbt.getBoolean("keep_inventory");
	}

	public void readData(CompoundNBT nbt)
	{
		readVisualData(nbt);
		ListNBT items = nbt.getList("items", Constants.NBT.TAG_COMPOUND);
		inventory.setSize(54);
		CompoundNBT invNBT = new CompoundNBT();
		invNBT.put("Items", items);
		inventory.deserializeNBT(invNBT);
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt)
	{
		writeData(nbt);
		return super.write(nbt);
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt)
	{
		super.read(state, nbt);
		readData(nbt);
	}

	@Override
	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		CompoundNBT nbt = new CompoundNBT();
		writeVisualData(nbt);
		if (openContainers > 0)
		{
			nbt.putShort("open_containers", (short) openContainers);
		}
		return new SUpdateTileEntityPacket(pos, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		readVisualData(pkt.getNbtCompound());
		openContainers = pkt.getNbtCompound().getShort("open_containers");
	}

	@Override
	public CompoundNBT getUpdateTag()
	{
		return write(new CompoundNBT());
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT nbt)
	{
		read(state, nbt);
	}

	@Override
	public ITextComponent getName()
	{
		return label.isEmpty() ? new TranslationTextComponent("block.quartzchests.chest.label.unnamed") : new StringTextComponent(label);
	}

	@Override
	public boolean hasCustomName()
	{
		return !label.isEmpty();
	}

	@Override
	@Nullable
	public ITextComponent getCustomName()
	{
		return label.isEmpty() ? null : new StringTextComponent(label);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, final @Nullable Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return inventoryCap.cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public void markDirty()
	{
		sendUpdate = true;
		isDirty = true;
	}

	public void containerOpened()
	{
		openContainers++;
		sendUpdate = true;
	}

	public void containerClosed()
	{
		openContainers--;

		if (openContainers < 0)
		{
			openContainers = 0;
		}

		sendUpdate = true;
	}

	@Override
	public void tick()
	{
		prevLidAngle = lidAngle;

		if (openContainers > 0 && lidAngle == 0D)
		{
			world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
		}

		if (openContainers == 0 && lidAngle > 0D || openContainers > 0 && lidAngle < 1D)
		{
			double a = lidAngle;

			if (openContainers > 0)
			{
				lidAngle += 0.1D;
			}
			else
			{
				lidAngle -= 0.1D;
			}

			if (lidAngle > 1D)
			{
				lidAngle = 1D;
			}

			if (lidAngle < 0.5D && a >= 0.5D)
			{
				world.playSound(null, pos, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
			}

			if (lidAngle < 0D)
			{
				lidAngle = 0D;
			}
		}

		if (isDirty && world != null)
		{
			isDirty = false;
			updateContainingBlockInfo();
			world.markChunkDirty(pos, this);
		}

		if (sendUpdate && world != null)
		{
			sendUpdate = false;
			BlockState state = getBlockState();
			world.markAndNotifyBlock(pos, world.getChunkAt(pos), state, state, Constants.BlockFlags.DEFAULT, 512);
		}
	}
}