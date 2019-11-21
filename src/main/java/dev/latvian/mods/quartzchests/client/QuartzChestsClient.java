package dev.latvian.mods.quartzchests.client;

import dev.latvian.mods.quartzchests.QuartzChestsCommon;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import dev.latvian.mods.quartzchests.gui.QuartzChestScreen;
import dev.latvian.mods.quartzchests.gui.QuartzChestsContainers;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * @author LatvianModder
 */
public class QuartzChestsClient extends QuartzChestsCommon
{
	@Override
	public void init()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	private void setup(FMLClientSetupEvent event)
	{
		ClientRegistry.bindTileEntitySpecialRenderer(QuartzChestEntity.class, new QuartzChestRenderer());
		ScreenManager.registerFactory(QuartzChestsContainers.CHEST, QuartzChestScreen::new);
	}

	@Override
	public void setQuartzChestTESIR(Item.Properties properties)
	{
		properties.setTEISR(() -> QuartzChestItemRenderer::new);
	}
}