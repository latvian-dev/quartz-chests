package dev.latvian.mods.quartzchests.net;

import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class SetLabelMessage
{
	private BlockPos pos;
	private String label;

	public SetLabelMessage(BlockPos p, String l)
	{
		pos = p;
		label = l;
	}

	public SetLabelMessage(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
		label = buf.readString(50);
	}

	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
		buf.writeString(label, 50);
	}

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> {
			TileEntity entity = context.get().getSender().world.getTileEntity(pos);

			if (entity instanceof QuartzChestEntity)
			{
				((QuartzChestEntity) entity).label = label;
				entity.markDirty();

				if (label.equalsIgnoreCase("owo") || label.equalsIgnoreCase("uwu"))
				{
					context.get().getSender().closeScreen();
					((QuartzChestEntity) entity).label = "No.";
					entity.getWorld().createExplosion(context.get().getSender(), entity.getPos().getX() + 0.5D, entity.getPos().getY() + 0.5D, entity.getPos().getZ() + 0.5D, 2F, Explosion.Mode.NONE);
					entity.getWorld().destroyBlock(entity.getPos(), true);
				}
			}
		});
	}
}