package dev.latvian.mods.quartzchests.net;

import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class SetIconMessage
{
	private BlockPos pos;
	private ItemStack icon;

	public SetIconMessage(BlockPos p, ItemStack l)
	{
		pos = p;
		icon = l;
	}

	public SetIconMessage(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
		icon = buf.readItemStack();
	}

	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
		buf.writeItemStack(icon);
	}

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> {
			TileEntity entity = context.get().getSender().world.getTileEntity(pos);

			if (entity instanceof QuartzChestEntity)
			{
				((QuartzChestEntity) entity).icon = icon;
				entity.markDirty();
			}
		});
	}
}