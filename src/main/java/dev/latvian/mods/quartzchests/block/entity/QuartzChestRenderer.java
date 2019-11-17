package dev.latvian.mods.quartzchests.block.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

/**
 * @author LatvianModder
 */
public class QuartzChestRenderer extends TileEntityRenderer<QuartzChestEntity>
{
	@Override
	public void render(QuartzChestEntity chest, double x, double y, double z, float partialTicks, int destroyStage)
	{
		Minecraft mc = Minecraft.getInstance();

		GlStateManager.color4f(1F, 1F, 1F, 1F);
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		GlStateManager.normal3f(0F, 1F, 0F);
		GlStateManager.translatef(0.5F, 0.5F, 0.5F);
		GlStateManager.rotatef(180F, 0F, 0F, 1F);
		GlStateManager.rotatef(chest.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING).getHorizontalAngle() + 180F, 0F, 1F, 0F);
		GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
		setLightmapDisabled(true);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.depthMask(true);
		GlStateManager.enableAlphaTest();

		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.5F, 0.22F, 0.062F);
		//boolean flag = getFontRenderer().getUnicodeFlag();
		//getFontRenderer().setUnicodeFlag(true);
		String label = chest.label;
		int sw1 = getFontRenderer().getStringWidth(label);
		float f1 = 1F / (float) Math.max((sw1 + 30), 64);
		GlStateManager.scalef(f1, f1, 1F);
		getFontRenderer().drawString(label, -sw1 / 2F, 0, 0xFF000000 | chest.textColor);
		//getFontRenderer().setUnicodeFlag(flag);
		GlStateManager.popMatrix();

		setLightmapDisabled(false);

		if (!chest.icon.isEmpty())
		{
			GlStateManager.pushMatrix();

			try
			{
				Block b = Block.getBlockFromItem(chest.icon.getItem());

				if (b == Blocks.AIR)
				{
					GlStateManager.translatef(0.5F, 0.67F, 0.04F);
					float iS = 0.4F;
					GlStateManager.scalef(-iS, -iS, iS);
				}
				else
				{
					GlStateManager.translatef(0.5F, 0.67F, 0.15F);
					float iS = 0.7F;
					GlStateManager.scalef(-iS, -iS, iS);
				}

				mc.getItemRenderer().renderItem(chest.icon, ItemCameraTransforms.TransformType.FIXED);
				//FTBLibClient.renderItem(FTBLibClient.mc.theWorld, iconItem);
			}
			catch (Exception e)
			{
			}

			GlStateManager.popMatrix();
		}

		GlStateManager.enableLighting();
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		GlStateManager.popMatrix();
	}
}