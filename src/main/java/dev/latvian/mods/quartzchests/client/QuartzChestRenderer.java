package dev.latvian.mods.quartzchests.client;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.latvian.mods.quartzchests.QuartzChests;
import dev.latvian.mods.quartzchests.block.QuartzChestsBlocks;
import dev.latvian.mods.quartzchests.block.entity.ColorType;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class QuartzChestRenderer extends TileEntityRenderer<QuartzChestEntity>
{
	private static class QuartzChestModel extends Model
	{
		private final RendererModel top, bottom;

		public QuartzChestModel()
		{
			top = new RendererModel(this, 0, 0).setTextureSize(64, 64);
			top.addBox(0, -5, -14, 14, 5, 14, 0);
			top.rotationPointX = 1;
			top.rotationPointY = 7;
			top.rotationPointZ = 15;
			bottom = new RendererModel(this, 0, 19).setTextureSize(64, 64);
			bottom.addBox(0, 0, 0, 14, 10, 14, 0);
			bottom.rotationPointX = 1;
			bottom.rotationPointY = 6;
			bottom.rotationPointZ = 1;
		}

		public void renderAll()
		{
			top.render(0.0625F);
			bottom.render(0.0625F);
		}
	}

	private static final ResourceLocation TEXTURE_BASE = new ResourceLocation(QuartzChests.MOD_ID, "textures/block/chest_base.png");
	private static final ResourceLocation TEXTURE_BORDERS = new ResourceLocation(QuartzChests.MOD_ID, "textures/block/chest_borders.png");

	private final QuartzChestModel baseModel, bordersModel;

	public QuartzChestRenderer()
	{
		baseModel = new QuartzChestModel();
		bordersModel = new QuartzChestModel();
	}

	@Override
	public void render(QuartzChestEntity chest, double x, double y, double z, float partialTicks, int destroyStage)
	{
		Minecraft mc = Minecraft.getInstance();
		BlockState state = chest.hasWorld() ? chest.getBlockState() : QuartzChestsBlocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);

		GlStateManager.color4f(1F, 1F, 1F, 1F);
		GlStateManager.enableDepthTest();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);

		if (destroyStage >= 0)
		{
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scalef(4F, 4F, 1F);
			GlStateManager.translatef(1F / 16F, 1F / 16F, 1F / 16F);
			GlStateManager.matrixMode(5888);
		}
		else
		{
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		GlStateManager.normal3f(0F, 1F, 0F);
		GlStateManager.translatef(0.5F, 0.5F, 0.5F);
		GlStateManager.rotatef(180F, 0F, 0F, 1F);
		GlStateManager.rotatef(state.get(HorizontalBlock.HORIZONTAL_FACING).getHorizontalAngle() + 180F, 0F, 1F, 0F);
		GlStateManager.translatef(-0.5F, -0.5F, -0.5F);

		GlStateManager.color4f(1F, 1F, 1F, 1F);

		if (chest.hasWorld())
		{
			double angle = MathHelper.lerp(partialTicks, chest.prevLidAngle, chest.lidAngle);
			angle = 1D - angle;
			angle = 1D - angle * angle * angle;
			baseModel.top.rotateAngleX = bordersModel.top.rotateAngleX = -(float) (angle * Math.PI / 2D);
		}
		else
		{
			baseModel.top.rotateAngleX = bordersModel.top.rotateAngleX = 0F;
		}

		bindTexture(destroyStage >= 0 ? DESTROY_STAGES[destroyStage] : TEXTURE_BASE);
		int cc = chest.colors[ColorType.CHEST.index];
		GlStateManager.color4f(((cc >> 16) & 255) / 255F, ((cc >> 8) & 255) / 255F, (cc & 255) / 255F, 1F);
		baseModel.renderAll();

		if (destroyStage < 0)
		{
			bindTexture(TEXTURE_BORDERS);
			int bc = chest.colors[ColorType.BORDER.index];
			GlStateManager.color4f(((bc >> 16) & 255) / 255F, ((bc >> 8) & 255) / 255F, (bc & 255) / 255F, 1F);
			bordersModel.renderAll();

			GlStateManager.color4f(1F, 1F, 1F, 1F);

			if (!chest.hasWorld() || baseModel.top.rotateAngleX == 0F)
			{
				if (chest.textGlow && chest.hasWorld())
				{
					setLightmapDisabled(true);
				}

				GlStateManager.disableLighting();
				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.depthMask(true);
				GlStateManager.enableAlphaTest();

				GlStateManager.pushMatrix();
				GlStateManager.translatef(0.5F, 0.22F, 0.062F);
				String label = chest.label;

				if (chest.textBold)
				{
					label = TextFormatting.BOLD + label;
				}

				if (chest.textItalic)
				{
					label = TextFormatting.ITALIC + label;
				}

				int sw1 = getFontRenderer().getStringWidth(label);
				float f1 = 1F / (float) Math.max((sw1 + 30), 64);
				GlStateManager.scalef(f1, f1, 1F);
				getFontRenderer().drawString(label, -sw1 / 2F, 0, 0xFF000000 | chest.colors[ColorType.TEXT.index]);
				GlStateManager.popMatrix();

				if (chest.textGlow && chest.hasWorld())
				{
					setLightmapDisabled(false);
				}
			}

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
				}
				catch (Exception e)
				{
				}

				GlStateManager.popMatrix();
			}
		}

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		GlStateManager.color4f(1F, 1F, 1F, 1F);

		if (destroyStage >= 0)
		{
			GlStateManager.matrixMode(5890);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
		}
	}
}