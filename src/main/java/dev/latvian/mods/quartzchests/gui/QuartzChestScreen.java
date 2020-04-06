package dev.latvian.mods.quartzchests.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.latvian.mods.quartzchests.block.entity.ColorType;
import dev.latvian.mods.quartzchests.item.QuartzChestsItems;
import dev.latvian.mods.quartzchests.net.QuartzChestsNet;
import dev.latvian.mods.quartzchests.net.SetColorMessage;
import dev.latvian.mods.quartzchests.net.SetIconMessage;
import dev.latvian.mods.quartzchests.net.SetLabelMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;

/**
 * @author LatvianModder
 */
public class QuartzChestScreen extends ContainerScreen<QuartzChestContainer>
{
	private static final ResourceLocation TEXTURE = new ResourceLocation("quartzchests:textures/gui/chest.png");

	private TextFieldWidget labelField;
	private Button chestColorButton, borderColorButton, textColorButton, iconButton;

	public QuartzChestScreen(QuartzChestContainer container, PlayerInventory playerInventory, ITextComponent title)
	{
		super(container, playerInventory, title);
		xSize = 176;
		ySize = 222;
	}

	@Override
	protected void init()
	{
		super.init();
		minecraft.keyboardListener.enableRepeatEvents(true);
		labelField = new TextFieldWidget(font, guiLeft + 9, guiTop + 6, 69, 9, I18n.format("block.quartzchests.chest.label"));
		labelField.setText(container.chest.label);
		labelField.setCanLoseFocus(true);
		labelField.setTextColor(-1);
		labelField.setDisabledTextColour(-1);
		labelField.setEnableBackgroundDrawing(false);
		labelField.setMaxStringLength(50);
		labelField.setResponder(this::setLabel);
		children.add(labelField);

		chestColorButton = addColorButton(ColorType.CHEST, guiLeft + 118, guiTop + 4);
		borderColorButton = addColorButton(ColorType.BORDER, guiLeft + 131, guiTop + 4);
		textColorButton = addColorButton(ColorType.TEXT, guiLeft + 144, guiTop + 4);

		iconButton = addSmallButton(guiLeft + 157, guiTop + 4, b -> {
			if (!minecraft.player.inventory.getItemStack().isEmpty() && minecraft.player.inventory.getItemStack().getItem() != QuartzChestsItems.CHEST.get())
			{
				QuartzChestsNet.MAIN.sendToServer(new SetIconMessage(container.chest.getPos(), minecraft.player.inventory.getItemStack()));
			}
			else if (hasShiftDown())
			{
				QuartzChestsNet.MAIN.sendToServer(new SetIconMessage(container.chest.getPos(), ItemStack.EMPTY));
			}
		});
	}

	public Button addSmallButton(int x, int y, Button.IPressable click)
	{
		return addButton(new Button(x, y, 12, 12, "", click)
		{
			@Override
			public void renderButton(int mouseX, int mouseY, float partialTicks)
			{
			}
		});
	}

	public Button addColorButton(ColorType type, int x, int y)
	{
		return addSmallButton(x, y, b -> {
			if (minecraft.player.inventory.getItemStack().getItem() instanceof DyeItem)
			{
				QuartzChestsNet.MAIN.sendToServer(new SetColorMessage(container.chest.getPos(), type, ((DyeItem) minecraft.player.inventory.getItemStack().getItem()).getDyeColor().getColorValue()));
			}
			else
			{
				minecraft.displayGuiScreen(new ColorSelectorScreen(this, type));
			}
		});
	}

	public void setLabel(String label)
	{
		//container.chest.label = label;
		QuartzChestsNet.MAIN.sendToServer(new SetLabelMessage(container.chest.getPos(), label));
	}

	@Override
	public void resize(Minecraft mc, int w, int h)
	{
		String s = labelField.getText();
		init(mc, w, h);
		labelField.setText(s);
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
		if (labelField.isFocused())
		{
			if (keyCode == GLFW.GLFW_KEY_ENTER)
			{
				labelField.setFocused2(false);
			}
			else if (keyCode != GLFW.GLFW_KEY_ESCAPE)
			{
				labelField.keyPressed(keyCode, scanCode, other);
				return true;
			}
		}

		return super.keyPressed(keyCode, scanCode, other);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
		RenderSystem.disableLighting();
		labelField.render(mouseX, mouseY, partialTicks);

		if (chestColorButton.isHovered())
		{
			renderTooltip(Collections.singletonList(I18n.format("block.quartzchests.chest.chest_color")), mouseX, mouseY);
		}

		if (borderColorButton.isHovered())
		{
			renderTooltip(Collections.singletonList(I18n.format("block.quartzchests.chest.border_color")), mouseX, mouseY);
		}

		if (textColorButton.isHovered())
		{
			renderTooltip(Collections.singletonList(I18n.format("block.quartzchests.chest.text_color")), mouseX, mouseY);
		}

		if (iconButton.isHovered())
		{
			renderTooltip(Collections.singletonList(I18n.format("block.quartzchests.chest.icon")), mouseX, mouseY);
		}

		if (!container.chest.icon.isEmpty())
		{
			RenderHelper.disableStandardItemLighting();

			RenderSystem.pushMatrix();
			RenderSystem.translatef(guiLeft + 159F, guiTop + 6F, 16F);
			RenderSystem.scalef(0.5F, 0.5F, 1F);
			setBlitOffset(200);
			itemRenderer.zLevel = 200F;
			net.minecraft.client.gui.FontRenderer f = container.chest.icon.getItem().getFontRenderer(container.chest.icon);

			if (f == null)
			{
				f = font;
			}

			itemRenderer.renderItemAndEffectIntoGUI(container.chest.icon, 0, 0);
			itemRenderer.renderItemOverlayIntoGUI(f, container.chest.icon, 0, 0, "");
			setBlitOffset(0);
			itemRenderer.zLevel = 0F;
			RenderSystem.popMatrix();
			RenderSystem.color4f(1F, 1F, 1F, 1F);

			RenderSystem.enableLighting();
			RenderSystem.enableDepthTest();
			RenderHelper.enableStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8F, ySize - 94, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		blit(guiLeft, guiTop, 0, 0, xSize, ySize);

		for (int i = 0; i < ColorType.VALUES.length; i++)
		{
			int c = container.chest.colors[i];
			RenderSystem.color4f(((c >> 16) & 255) / 255F, ((c >> 8) & 255) / 255F, (c & 255) / 255F, 1F);
			blit(guiLeft + 118 + i * 13, guiTop + 4, 177, 0, 12, 12);
		}

		RenderSystem.color4f(1F, 1F, 1F, 1F);
	}
}
