package dev.latvian.mods.quartzchests.gui;

import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class QuartzChestContainer extends Container
{
	public final QuartzChestEntity chest;

	public QuartzChestContainer(int id, PlayerInventory playerInventory, QuartzChestEntity c)
	{
		super(QuartzChestsContainers.CHEST, id);
		chest = c;

		for (int y = 0; y < 6; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				addSlot(new SlotItemHandler(chest.inventory, x + y * 9, 8 + x * 18, 18 + y * 18));
			}
		}

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 140 + y * 18));
			}
		}

		for (int x = 0; x < 9; x++)
		{
			addSlot(new Slot(playerInventory, x, 8 + x * 18, 198));
		}

		if (!playerInventory.player.isSpectator() && !playerInventory.player.world.isRemote())
		{
			chest.containerOpened();
		}
	}

	public QuartzChestContainer(int id, PlayerInventory playerInventory, @Nullable PacketBuffer data)
	{
		this(id, playerInventory, (QuartzChestEntity) playerInventory.player.world.getTileEntity(data.readBlockPos()));
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int index)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack stack1 = slot.getStack();
			stack = stack1.copy();

			if (index < 54)
			{
				if (!mergeItemStack(stack1, 54, inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!mergeItemStack(stack1, 0, 54, false))
			{
				return ItemStack.EMPTY;
			}

			if (stack1.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return stack;
	}

	@Override
	public boolean canInteractWith(PlayerEntity player)
	{
		return true;
	}

	@Override
	public void onContainerClosed(PlayerEntity player)
	{
		super.onContainerClosed(player);

		if (!player.isSpectator() && !player.world.isRemote())
		{
			chest.containerClosed();
		}
	}
}