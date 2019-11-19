package dev.latvian.mods.quartzchests.gui;

import dev.latvian.mods.quartzchests.QuartzChests;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

/**
 * @author LatvianModder
 */
public class ColorSelectorScreen extends Screen
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(QuartzChests.MOD_ID, "textures/gui/color_selector.png");
	private static final ResourceLocation HUE_TEXTURE = new ResourceLocation(QuartzChests.MOD_ID, "textures/gui/color_selector_hue.png");

	private TextFieldWidget colorField;
	private int xSize, ySize;

	public ColorSelectorScreen(ITextComponent title)
	{
		super(title);
		xSize = 139;
		ySize = 107;
	}

	@Override
	protected void init()
	{
		super.init();
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		minecraft.keyboardListener.enableRepeatEvents(true);
		colorField = new TextFieldWidget(font, x + 11, y + 19, 116, 9, I18n.format("container.repair"));
		colorField.setText("Hi");
		colorField.setCanLoseFocus(true);
		colorField.setTextColor(-1);
		colorField.setDisabledTextColour(-1);
		colorField.setEnableBackgroundDrawing(false);
		colorField.setMaxStringLength(50);
		colorField.setResponder(this::setColor);
		children.add(colorField);
	}

	private int HSBtoRGB(float hue, float saturation, float brightness)
	{
		int r = 0, g = 0, b = 0;
		if (saturation == 0)
		{
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		}
		else
		{
			float h = (hue - (float) Math.floor(hue)) * 6.0f;
			float f = h - (float) Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h)
			{
				case 0:
					r = (int) (brightness * 255.0f + 0.5f);
					g = (int) (t * 255.0f + 0.5f);
					b = (int) (p * 255.0f + 0.5f);
					break;
				case 1:
					r = (int) (q * 255.0f + 0.5f);
					g = (int) (brightness * 255.0f + 0.5f);
					b = (int) (p * 255.0f + 0.5f);
					break;
				case 2:
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (brightness * 255.0f + 0.5f);
					b = (int) (t * 255.0f + 0.5f);
					break;
				case 3:
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (q * 255.0f + 0.5f);
					b = (int) (brightness * 255.0f + 0.5f);
					break;
				case 4:
					r = (int) (t * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (brightness * 255.0f + 0.5f);
					break;
				case 5:
					r = (int) (brightness * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (q * 255.0f + 0.5f);
					break;
			}
		}
		return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
	}

	public void setColor(String color)
	{
	}

	@Override
	public void removed()
	{
		super.removed();
		minecraft.keyboardListener.enableRepeatEvents(false);
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

		return super.keyPressed(keyCode, scanCode, other);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		renderBackground();

		/*
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		blit(guiLeft, guiTop, 0, 0, xSize, ySize);
		int cc = container.chest.chestColor;
		GlStateManager.color4f(((cc >> 16) & 255) / 255F, ((cc >> 8) & 255) / 255F, (cc & 255) / 255F, 1F);
		blit(guiLeft + 118, guiTop + 4, 177, 0, 12, 12);
		int bc = container.chest.borderColor;
		GlStateManager.color4f(((bc >> 16) & 255) / 255F, ((bc >> 8) & 255) / 255F, (bc & 255) / 255F, 1F);
		blit(guiLeft + 131, guiTop + 4, 177, 0, 12, 12);
		int tc = container.chest.textColor;
		GlStateManager.color4f(((tc >> 16) & 255) / 255F, ((tc >> 8) & 255) / 255F, (tc & 255) / 255F, 1F);
		blit(guiLeft + 144, guiTop + 4, 177, 0, 12, 12);
		GlStateManager.color4f(1F, 1F, 1F, 1F);
*/
		super.render(mouseX, mouseY, partialTicks);
		colorField.render(mouseX, mouseY, partialTicks);
	}
}
