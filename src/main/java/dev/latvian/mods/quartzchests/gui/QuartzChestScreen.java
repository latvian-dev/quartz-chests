package dev.latvian.mods.quartzchests.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.latvian.mods.quartzchests.QuartzChests;
import dev.latvian.mods.quartzchests.block.QuartzChestsBlocks;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import dev.latvian.mods.quartzchests.net.QuartzChestsNet;
import dev.latvian.mods.quartzchests.net.SetBorderColorMessage;
import dev.latvian.mods.quartzchests.net.SetChestColorMessage;
import dev.latvian.mods.quartzchests.net.SetIconMessage;
import dev.latvian.mods.quartzchests.net.SetLabelMessage;
import dev.latvian.mods.quartzchests.net.SetTextColorMessage;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.IntSupplier;

/**
 * @author LatvianModder
 */
public class QuartzChestScreen extends ContainerScreen<QuartzChestContainer>
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(QuartzChests.MOD_ID, "textures/gui/chest.png");

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
		labelField = new TextFieldWidget(font, guiLeft + 9, guiTop + 6, 63, 9, I18n.format("container.repair"));
		labelField.setText(container.chest.label);
		labelField.setCanLoseFocus(true);
		labelField.setTextColor(-1);
		labelField.setDisabledTextColour(-1);
		labelField.setEnableBackgroundDrawing(false);
		labelField.setMaxStringLength(50);
		labelField.setResponder(this::setLabel);
		children.add(labelField);

		chestColorButton = addColorButton(guiLeft + 118, guiTop + 4, QuartzChestEntity.DEFAULT_CHEST_COLOR, () -> container.chest.chestColor, SetChestColorMessage::new);
		borderColorButton = addColorButton(guiLeft + 131, guiTop + 4, QuartzChestEntity.DEFAULT_BORDER_COLOR, () -> container.chest.borderColor, SetBorderColorMessage::new);
		textColorButton = addColorButton(guiLeft + 144, guiTop + 4, QuartzChestEntity.DEFAULT_TEXT_COLOR, () -> container.chest.textColor, SetTextColorMessage::new);

		iconButton = addSmallButton(guiLeft + 157, guiTop + 4, b -> {
			if (!minecraft.player.inventory.getItemStack().isEmpty() && minecraft.player.inventory.getItemStack().getItem() != QuartzChestsBlocks.CHEST.asItem())
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

	public Button addColorButton(int x, int y, int def, IntSupplier getter, BiFunction<BlockPos, Integer, Object> message)
	{
		return addSmallButton(x, y, b -> {
			if (hasShiftDown())
			{
				QuartzChestsNet.MAIN.sendToServer(message.apply(container.chest.getPos(), def));
			}
			else if (minecraft.player.inventory.getItemStack().getItem() instanceof DyeItem)
			{
				QuartzChestsNet.MAIN.sendToServer(message.apply(container.chest.getPos(), ((DyeItem) minecraft.player.inventory.getItemStack().getItem()).getDyeColor().getColorValue()));
			}
			else
			{
				QuartzChestsNet.MAIN.sendToServer(message.apply(container.chest.getPos(), getter.getAsInt()));
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
			RenderHelper.enableGUIStandardItemLighting();

			GlStateManager.pushMatrix();
			GlStateManager.translatef(guiLeft + 159F, guiTop + 6F, 16F);
			GlStateManager.scalef(0.5F, 0.5F, 1F);
			blitOffset = 200;
			itemRenderer.zLevel = 200F;
			net.minecraft.client.gui.FontRenderer f = container.chest.icon.getItem().getFontRenderer(container.chest.icon);

			if (f == null)
			{
				f = font;
			}

			itemRenderer.renderItemAndEffectIntoGUI(container.chest.icon, 0, 0);
			itemRenderer.renderItemOverlayIntoGUI(f, container.chest.icon, 0, 0, "");
			blitOffset = 0;
			itemRenderer.zLevel = 0F;
			GlStateManager.popMatrix();
			GlStateManager.color4f(1F, 1F, 1F, 1F);

			GlStateManager.enableLighting();
			GlStateManager.enableDepthTest();
			RenderHelper.enableStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		//String s = title.getFormattedText();
		//font.drawString(s, (float) (xSize / 2 - font.getStringWidth(s) / 2), 6F, 4210752);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8F, (float) (ySize - 94), 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
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
	}
}
