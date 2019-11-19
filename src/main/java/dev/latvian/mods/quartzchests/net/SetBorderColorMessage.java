package dev.latvian.mods.quartzchests.net;

import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class SetBorderColorMessage
{
	private BlockPos pos;
	private int color;

	public SetBorderColorMessage(BlockPos p, int c)
	{
		pos = p;
		color = c;
	}

	public SetBorderColorMessage(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
		color = buf.readInt();
	}

	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
		buf.writeInt(color);
	}

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> {
			TileEntity entity = context.get().getSender().world.getTileEntity(pos);

			if (entity instanceof QuartzChestEntity)
			{
				((QuartzChestEntity) entity).borderColor = color;
				entity.markDirty();
				entity.getWorld().markAndNotifyBlock(pos, null, entity.getBlockState(), entity.getBlockState(), Constants.BlockFlags.DEFAULT);
			}
		});
	}
}