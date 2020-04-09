package dev.latvian.mods.quartzchests.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

/**
 * @author LatvianModder
 */
public class QuartzChestItemRenderer extends ItemStackTileEntityRenderer
{
	private QuartzChestEntity dummy;

	@Override
	public void render(ItemStack stack, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay)
	{
		if (dummy == null)
		{
			dummy = new QuartzChestEntity();
		}

		dummy.readVisualData(stack.hasTag() ? stack.getTag().getCompound("BlockEntityTag") : new CompoundNBT());
		TileEntityRendererDispatcher.instance.renderNullable(dummy, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay);
	}
}