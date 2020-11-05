package dev.latvian.mods.quartzchests.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import dev.latvian.mods.quartzchests.block.QuartzChestsBlocks;
import dev.latvian.mods.quartzchests.block.entity.ColorType;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * @author LatvianModder
 */
public class QuartzChestRenderer extends TileEntityRenderer<QuartzChestEntity>
{
	private static class QuartzChestModel extends Model
	{
		private final ModelRenderer top, bottom;

		public QuartzChestModel()
		{
			super(RenderType::getEntityCutout);
			textureWidth = 64;
			textureHeight = 64;
			bottom = new ModelRenderer(this, 0, 19);
			bottom.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);

			top = new ModelRenderer(this, 0, 0);
			top.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
			top.rotationPointY = 9.0F;
			top.rotationPointZ = 1.0F;
		}

		@Override
		public void render(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int light1, int light2, float red, float green, float blue, float alpha)
		{
			top.render(matrixStack, vertexBuilder, light1, light2, red, green, blue, alpha);
			bottom.render(matrixStack, vertexBuilder, light1, light2, red, green, blue, alpha);
		}
	}

	private static final RenderMaterial TEXTURE_BASE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation("quartzchests:block/chest_base"));
	private static final RenderMaterial TEXTURE_BORDERS = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation("quartzchests:block/chest_borders"));

	private final QuartzChestModel baseModel, bordersModel;

	public QuartzChestRenderer(TileEntityRendererDispatcher dispatcher)
	{
		super(dispatcher);
		baseModel = new QuartzChestModel();
		bordersModel = new QuartzChestModel();
	}

	@Override
	public void render(QuartzChestEntity chest, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer typeBuffer, int combinedLight, int combinedOverlay)
	{
		World world = chest.getWorld();
		BlockState blockstate = world != null ? chest.getBlockState() : QuartzChestsBlocks.CHEST.get().getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.SOUTH);

		matrixStack.push();
		float f = blockstate.get(HorizontalBlock.HORIZONTAL_FACING).getHorizontalAngle();
		matrixStack.translate(0.5D, 0.5D, 0.5D);
		matrixStack.rotate(Vector3f.YP.rotationDegrees(-f));
		matrixStack.translate(-0.5D, -0.5D, -0.5D);

		if (world != null)
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

		IVertexBuilder builderBase = TEXTURE_BASE.getBuffer(typeBuffer, RenderType::getEntityCutout);
		IVertexBuilder builderBorders = TEXTURE_BORDERS.getBuffer(typeBuffer, RenderType::getEntityCutout);

		int baseR = (chest.colors[ColorType.CHEST.index] >> 16) & 255;
		int baseG = (chest.colors[ColorType.CHEST.index] >> 8) & 255;
		int baseB = (chest.colors[ColorType.CHEST.index] >> 0) & 255;
		baseModel.render(matrixStack, builderBase, combinedLight, combinedOverlay, baseR / 255F, baseG / 255F, baseB / 255F, 1F);

		int borderR = (chest.colors[ColorType.BORDER.index] >> 16) & 255;
		int borderG = (chest.colors[ColorType.BORDER.index] >> 8) & 255;
		int borderB = (chest.colors[ColorType.BORDER.index] >> 0) & 255;
		bordersModel.render(matrixStack, builderBorders, combinedLight, combinedOverlay, borderR / 255F, borderG / 255F, borderB / 255F, 1F);

		if (!chest.icon.isEmpty())
		{
			matrixStack.push();
			matrixStack.translate(0.5F, 0.33F, 0.96F);
			matrixStack.rotate(Vector3f.YP.rotationDegrees(180F));
			matrixStack.scale(0.4F, 0.4F, 0.4F);
			Minecraft.getInstance().getItemRenderer().renderItem(chest.icon, ItemCameraTransforms.TransformType.FIXED, combinedLight, OverlayTexture.NO_OVERLAY, matrixStack, typeBuffer);
			matrixStack.pop();
		}

		if (!chest.hasWorld() || baseModel.top.rotateAngleX == 0F)
		{
			if (chest.textGlow && chest.hasWorld())
			{
				//setLightmapDisabled(true);
			}

			RenderSystem.disableLighting();
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.depthMask(true);
			RenderSystem.enableAlphaTest();

			matrixStack.push();
			//matrixStack.translate(0.5F, 0.22F, 0.062F);
			matrixStack.translate(0.5F, 0.78F, 0.938F);
			String label = chest.label;

			if (chest.textBold)
			{
				label = TextFormatting.BOLD + label;
			}

			if (chest.textItalic)
			{
				label = TextFormatting.ITALIC + label;
			}

			int sw1 = renderDispatcher.getFontRenderer().getStringWidth(label);
			float f1 = 1F / (float) Math.max((sw1 + 30), 64);
			matrixStack.scale(f1, -f1, f1);
			//dispatcher.getFontRenderer().drawString(label, -sw1 / 2F, 0, 0xFF000000 | chest.colors[ColorType.TEXT.index]);
			renderDispatcher.getFontRenderer().renderString(label, -sw1 / 2F, 0, 0xFF000000 | chest.colors[ColorType.TEXT.index], false, matrixStack.getLast().getMatrix(), typeBuffer, false, 0, combinedLight);
			matrixStack.pop();

			if (chest.textGlow && chest.hasWorld())
			{
				//setLightmapDisabled(false);
			}
		}

		matrixStack.pop();
	}
}