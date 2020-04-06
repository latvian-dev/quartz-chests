package dev.latvian.mods.quartzchests.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.latvian.mods.quartzchests.block.QuartzChestsBlocks;
import dev.latvian.mods.quartzchests.block.entity.ColorType;
import dev.latvian.mods.quartzchests.net.QuartzChestsNet;
import dev.latvian.mods.quartzchests.net.SetColorMessage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

/**
 * @author LatvianModder
 */
public class ColorSelectorScreen extends Screen
{
	private static final ResourceLocation TEXTURE = new ResourceLocation("quartzchests:textures/gui/color_selector.png");
	private static final ResourceLocation HUE_TEXTURE = new ResourceLocation("quartzchests:textures/gui/color_selector_hue.png");

	public final ColorType type;
	private TextFieldWidget colorField;
	public final QuartzChestScreen parent;
	private int xSize, ySize;
	private ItemStack preview;
	private int prevColor;
	private float[] rgb, hsb, rgbMinS, rgbMaxS, rgbMinB, rgbMaxB;
	private double prevMouseX;

	public ColorSelectorScreen(QuartzChestScreen s, ColorType t)
	{
		super(new TranslationTextComponent(t.translationKey));
		xSize = 234;
		ySize = 107;
		parent = s;
		type = t;
		preview = new ItemStack(QuartzChestsBlocks.CHEST.get());
		prevColor = s.getContainer().chest.colors[t.index];
		rgb = new float[3];
		hsb = new float[3];
		rgbMinS = new float[3];
		rgbMaxS = new float[3];
		rgbMinB = new float[3];
		rgbMaxB = new float[3];
	}

	@Override
	protected void init()
	{
		super.init();
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		minecraft.keyboardListener.enableRepeatEvents(true);
		colorField = new TextFieldWidget(font, x + 11, y + 20, 116, 9, title.getString());
		colorField.setCanLoseFocus(true);
		colorField.setTextColor(-1);
		colorField.setDisabledTextColour(-1);
		colorField.setEnableBackgroundDrawing(false);
		colorField.setMaxStringLength(50);
		colorField.setTextColor(0xFF000000 | parent.getContainer().chest.colors[type.index]);
		colorField.setResponder(this::setColor);
		children.add(colorField);

		addButton(new Button(x + 9, y + 79, 41, 20, I18n.format("gui.cancel"), b -> {
			setColor(prevColor, false);
			minecraft.displayGuiScreen(parent);
		}));

		addButton(new Button(x + 51, y + 79, 37, 20, "Def", b -> setColor(type.defaultColor, true)));
		addButton(new Button(x + 89, y + 79, 41, 20, I18n.format("gui.done"), b -> minecraft.displayGuiScreen(parent)));

		setColor(parent.getContainer().chest.colors[type.index], true);
	}

	private void convertHSBtoRGB(float[] hsb, float[] rgb)
	{
		if (hsb[1] <= 0F)
		{
			rgb[0] = rgb[1] = rgb[2] = hsb[2];
			return;
		}

		float h = (hsb[0] - (float) Math.floor(hsb[0])) * 6F;
		float f = h - (float) Math.floor(h);
		float p = hsb[2] * (1F - hsb[1]);
		float q = hsb[2] * (1F - hsb[1] * f);
		float t = hsb[2] * (1F - (hsb[1] * (1F - f)));

		switch ((int) h)
		{
			case 0:
				rgb[0] = hsb[2];
				rgb[1] = t;
				rgb[2] = p;
				return;
			case 1:
				rgb[0] = q;
				rgb[1] = hsb[2];
				rgb[2] = p;
				return;
			case 2:
				rgb[0] = p;
				rgb[1] = hsb[2];
				rgb[2] = t;
				return;
			case 3:
				rgb[0] = p;
				rgb[1] = q;
				rgb[2] = hsb[2];
				return;
			case 4:
				rgb[0] = t;
				rgb[1] = p;
				rgb[2] = hsb[2];
				return;
			case 5:
				rgb[0] = hsb[2];
				rgb[1] = p;
				rgb[2] = q;
		}
	}

	private void convertRGBtoHSB(float[] rgb, float[] hsb)
	{
		float cmax = Math.max(rgb[0], rgb[1]);

		if (rgb[2] > cmax)
		{
			cmax = rgb[2];
		}

		float cmin = Math.min(rgb[0], rgb[1]);

		if (rgb[2] < cmin)
		{
			cmin = rgb[2];
		}

		hsb[2] = cmax;

		if (cmax != 0F)
		{
			hsb[1] = (cmax - cmin) / cmax;
		}
		else
		{
			hsb[1] = 0F;
		}

		if (hsb[1] == 0F)
		{
			hsb[0] = 0;
		}
		else
		{
			float redc = (cmax - rgb[0]) / (cmax - cmin);
			float greenc = (cmax - rgb[1]) / (cmax - cmin);
			float bluec = (cmax - rgb[2]) / (cmax - cmin);

			if (rgb[0] == cmax)
			{
				hsb[0] = bluec - greenc;
			}
			else if (rgb[1] == cmax)
			{
				hsb[0] = 2F + redc - bluec;
			}
			else
			{
				hsb[0] = 4F + greenc - redc;
			}

			hsb[0] /= 6F;

			if (hsb[0] < 0)
			{
				hsb[0] += 1F;
			}
		}
	}

	private int getRGB(float[] rgb)
	{
		return 0xFF000000 | ((int) (rgb[0] * 255F) << 16) | ((int) (rgb[1] * 255F) << 8) | (int) (rgb[2] * 255F);
	}

	private void setRGB(int c, float[] rgb)
	{
		rgb[0] = ((c >> 16) & 255) / 255F;
		rgb[1] = ((c >> 8) & 255) / 255F;
		rgb[2] = (c & 255) / 255F;
	}

	public void setColor(int color, boolean updateText)
	{
		if (updateText)
		{
			colorField.setText(String.format("#%06X", 0xFFFFFF & color));
			return;
		}

		colorField.setTextColor(0xFF000000 | color);
		parent.getContainer().chest.colors[type.index] = color;

		CompoundNBT data = new CompoundNBT();
		parent.getContainer().chest.writeVisualData(data);
		preview.setTagInfo("BlockEntityTag", data);

		setRGB(color, rgb);
		convertRGBtoHSB(rgb, hsb);

		float[] hsbMinS = Arrays.copyOf(hsb, 3);
		float[] hsbMaxS = Arrays.copyOf(hsb, 3);
		float[] hsbMinB = Arrays.copyOf(hsb, 3);
		float[] hsbMaxB = Arrays.copyOf(hsb, 3);

		hsbMinS[1] = 0F;
		hsbMaxS[1] = 1F;
		hsbMinB[2] = 0F;
		hsbMaxB[2] = 1F;

		convertHSBtoRGB(hsbMinS, rgbMinS);
		convertHSBtoRGB(hsbMaxS, rgbMaxS);
		convertHSBtoRGB(hsbMinB, rgbMinB);
		convertHSBtoRGB(hsbMaxB, rgbMaxB);

		QuartzChestsNet.MAIN.sendToServer(new SetColorMessage(parent.getContainer().chest.getPos(), type, color));
	}

	public void setColor(String color)
	{
		if (color.length() == 7 && color.startsWith("#"))
		{
			try
			{
				setColor(0xFFFFFF & Integer.parseInt(color.substring(1), 16), false);
			}
			catch (Exception ex)
			{
			}
		}
	}

	@Override
	public void removed()
	{
		super.removed();
		minecraft.keyboardListener.enableRepeatEvents(false);
	}

	@Override
	public void onClose()
	{
		minecraft.player.closeScreen();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int other)
	{
		if (colorField.isFocused())
		{
			if (keyCode == GLFW.GLFW_KEY_ENTER)
			{
				colorField.setFocused2(false);
			}
			else if (keyCode != GLFW.GLFW_KEY_ESCAPE)
			{
				colorField.keyPressed(keyCode, scanCode, other);
				return true;
			}
		}

		InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);

		if (keyCode == GLFW.GLFW_KEY_ESCAPE || minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))
		{
			onClose();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, other);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button)
	{
		if (button == 0 && checkHSBMouse(mx, my))
		{
			return true;
		}

		return super.mouseClicked(mx, my, button);
	}

	public boolean checkHSBMouse(double mx, double my)
	{
		prevMouseX = mx;
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;

		if (mx >= x + 10 && mx < x + 129)
		{
			float value = MathHelper.clamp((float) ((mx - (x + 10D)) / 118D), 0F, 1F);

			if (my >= y + 33 && my < y + 44)
			{
				hsb[0] = value;
				convertHSBtoRGB(hsb, rgb);
				setColor(getRGB(rgb), true);
				return true;
			}
			else if (my >= y + 47 && my < y + 58)
			{
				hsb[1] = value;
				convertHSBtoRGB(hsb, rgb);
				setColor(getRGB(rgb), true);
				return true;
			}
			else if (my >= y + 61 && my < y + 72)
			{
				hsb[2] = value;
				convertHSBtoRGB(hsb, rgb);
				setColor(getRGB(rgb), true);
				return true;
			}
		}

		return false;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;

		renderBackground();
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		blit(x, y, 0, 0, xSize, ySize);

		if (prevMouseX != mouseX && GLFW.glfwGetMouseButton(minecraft.getMainWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS)
		{
			checkHSBMouse(mouseX, mouseY);
		}

		minecraft.getTextureManager().bindTexture(HUE_TEXTURE);
		innerBlit(x + 10, x + 129, y + 33, y + 44, 0, 0, 1, 0, 1);

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.shadeModel(GL11.GL_SMOOTH);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(x + 10, y + 58, 0D).color(rgbMinS[0], rgbMinS[1], rgbMinS[2], 1F).endVertex();
		bufferbuilder.pos(x + 129, y + 58, 0D).color(rgbMaxS[0], rgbMaxS[1], rgbMaxS[2], 1F).endVertex();
		bufferbuilder.pos(x + 129, y + 47, 0D).color(rgbMaxS[0], rgbMaxS[1], rgbMaxS[2], 1F).endVertex();
		bufferbuilder.pos(x + 10, y + 47, 0D).color(rgbMinS[0], rgbMinS[1], rgbMinS[2], 1F).endVertex();

		bufferbuilder.pos(x + 10, y + 72, 0D).color(rgbMinB[0], rgbMinB[1], rgbMinB[2], 1F).endVertex();
		bufferbuilder.pos(x + 129, y + 72, 0D).color(rgbMaxB[0], rgbMaxB[1], rgbMaxB[2], 1F).endVertex();
		bufferbuilder.pos(x + 129, y + 61, 0D).color(rgbMaxB[0], rgbMaxB[1], rgbMaxB[2], 1F).endVertex();
		bufferbuilder.pos(x + 10, y + 61, 0D).color(rgbMinB[0], rgbMinB[1], rgbMinB[2], 1F).endVertex();
		tessellator.draw();

		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();

		RenderSystem.color4f(1F, 1F, 1F, 1F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		blit(x + 10 + (int) (119F * hsb[0]), y + 33, 0, 108, 1, 11);
		blit(x + 10 + (int) (119F * hsb[1]), y + 47, 0, 108, 1, 11);
		blit(x + 10 + (int) (119F * hsb[2]), y + 61, 0, 108, 1, 11);

		super.render(mouseX, mouseY, partialTicks);
		colorField.render(mouseX, mouseY, partialTicks);

		String t = title.getFormattedText();
		font.drawString(t, x + 9, y + 7, 4210752);

		RenderHelper.enableStandardItemLighting();
		RenderSystem.disableLighting();
		RenderSystem.enableRescaleNormal();

		RenderSystem.pushMatrix();
		RenderSystem.translatef(x + 149F, y + 23F, 16F);
		RenderSystem.scalef(4F, 4F, 1F);
		setBlitOffset(200);
		itemRenderer.zLevel = 200F;
		net.minecraft.client.gui.FontRenderer f = preview.getItem().getFontRenderer(preview);

		if (f == null)
		{
			f = font;
		}

		itemRenderer.renderItemAndEffectIntoGUI(preview, 0, 0);
		itemRenderer.renderItemOverlayIntoGUI(f, preview, 0, 0, "");
		setBlitOffset(0);
		itemRenderer.zLevel = 0F;
		RenderSystem.popMatrix();
		RenderSystem.color4f(1F, 1F, 1F, 1F);

		RenderSystem.enableLighting();
		RenderSystem.enableDepthTest();
		RenderHelper.disableStandardItemLighting();
	}
}
