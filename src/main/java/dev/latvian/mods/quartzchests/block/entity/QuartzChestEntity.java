package dev.latvian.mods.quartzchests.block.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class QuartzChestEntity extends TileEntity
{
	public int color;

	public QuartzChestEntity()
	{
		super(QuartzChestsBlockEntities.CHEST);
		color = 0xFFFFFFFF;
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt)
	{
		nbt.putInt("color", color);
		return super.write(nbt);
	}

	@Override
	public void read(CompoundNBT nbt)
	{
		super.read(nbt);
		color = nbt.getInt("color");
	}

	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		handleUpdateTag(pkt.getNbtCompound());

		if (world != null)
		{
			world.markAndNotifyBlock(pos, null, getBlockState(), getBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
		}
	}

	public CompoundNBT getUpdateTag()
	{
		return write(new CompoundNBT());
	}

	@Override
	public void handleUpdateTag(CompoundNBT nbt)
	{
		read(nbt);
	}
}