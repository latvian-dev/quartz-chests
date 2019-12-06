package dev.latvian.mods.quartzchests.net;

import dev.latvian.mods.quartzchests.block.entity.ColorType;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class SetColorMessage
{
	private BlockPos pos;
	private ColorType type;
	private int color;

	public SetColorMessage(BlockPos p, ColorType t, int c)
	{
		pos = p;
		type = t;
		color = c;
	}

	public SetColorMessage(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
		type = ColorType.VALUES[buf.readByte()];
		color = buf.readInt();
	}

	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
		buf.writeByte(type.index);
		buf.writeInt(color);
	}

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> {
			TileEntity entity = context.get().getSender().world.getTileEntity(pos);

			if (entity instanceof QuartzChestEntity)
			{
				((QuartzChestEntity) entity).colors[type.index] = color;
				entity.markDirty();
			}
		});

		context.get().setPacketHandled(true);
	}
}