package dev.latvian.mods.quartzchests.block.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
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
public class QuartzChestEntity extends TileEntity implements INameable
{
	public static final int DEFAULT_COLOR = 0xFFFFFF;
	public static final int DEFAULT_BORDER_COLOR = 0x4A4040;
	public static final int DEFAULT_TEXT_COLOR = 0x000000;

	public String label;
	public int color;
	public int borderColor;
	public int textColor;
	public ItemStack icon;
	public ItemStackHandler inventory;
	private final LazyOptional<IItemHandler> inventoryCap;

	public QuartzChestEntity()
	{
		super(QuartzChestsBlockEntities.CHEST);
		label = "";
		color = DEFAULT_COLOR;
		borderColor = DEFAULT_BORDER_COLOR;
		textColor = DEFAULT_TEXT_COLOR;
		icon = ItemStack.EMPTY;

		inventory = new ItemStackHandler(54)
		{
			@Override
			protected void onContentsChanged(int slot)
			{
				markDirty();
			}
		};

		inventoryCap = LazyOptional.of(() -> inventory);
	}

	public void writeData(CompoundNBT nbt)
	{
		if (!label.isEmpty())
		{
			nbt.putString("label", label);
		}

		if (color != DEFAULT_COLOR)
		{
			nbt.putInt("color", color);
		}

		if (borderColor != DEFAULT_BORDER_COLOR)
		{
			nbt.putInt("border_color", borderColor);
		}

		if (textColor != DEFAULT_TEXT_COLOR)
		{
			nbt.putInt("text_color", textColor);
		}

		if (!icon.isEmpty())
		{
			nbt.put("icon", icon.write(new CompoundNBT()));
		}

		ListNBT items = inventory.serializeNBT().getList("Items", Constants.NBT.TAG_COMPOUND);

		if (!items.isEmpty())
		{
			nbt.put("items", items);
		}
	}

	public void readData(CompoundNBT nbt)
	{
		label = nbt.getString("label");
		color = nbt.contains("color") ? nbt.getInt("color") : DEFAULT_COLOR;
		borderColor = nbt.contains("border_color") ? nbt.getInt("border_color") : DEFAULT_BORDER_COLOR;
		textColor = nbt.contains("text_color") ? nbt.getInt("text_color") : DEFAULT_TEXT_COLOR;
		icon = nbt.contains("icon") ? ItemStack.read(nbt.getCompound("icon")) : ItemStack.EMPTY;


		ListNBT items = nbt.getList("items", Constants.NBT.TAG_COMPOUND);

		if (items.isEmpty())
		{
			inventory.setSize(54);
		}
		else
		{
			CompoundNBT invNBT = new CompoundNBT();
			invNBT.put("Items", items);
			inventory.deserializeNBT(invNBT);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt)
	{
		writeData(nbt);
		return super.write(nbt);
	}

	@Override
	public void read(CompoundNBT nbt)
	{
		super.read(nbt);
		readData(nbt);
	}

	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		CompoundNBT nbt = new CompoundNBT();
		writeData(nbt);
		return new SUpdateTileEntityPacket(pos, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		readData(pkt.getNbtCompound());

		if (world != null)
		{
			world.markAndNotifyBlock(pos, null, getBlockState(), getBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
		}
	}

	@Override
	public CompoundNBT getUpdateTag()
	{
		return write(new CompoundNBT());
	}

	@Override
	public void handleUpdateTag(CompoundNBT nbt)
	{
		read(nbt);
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
}