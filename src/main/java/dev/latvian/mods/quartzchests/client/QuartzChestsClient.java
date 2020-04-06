package dev.latvian.mods.quartzchests.client;

import dev.latvian.mods.quartzchests.QuartzChestsCommon;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestsBlockEntities;
import dev.latvian.mods.quartzchests.gui.QuartzChestScreen;
import dev.latvian.mods.quartzchests.gui.QuartzChestsContainers;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
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
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::textureStitch);
	}

	private void setup(FMLClientSetupEvent event)
	{
		ClientRegistry.bindTileEntityRenderer(QuartzChestsBlockEntities.CHEST.get(), QuartzChestRenderer::new);
		ScreenManager.registerFactory(QuartzChestsContainers.CHEST.get(), QuartzChestScreen::new);
	}

	private void textureStitch(TextureStitchEvent.Pre event)
	{
		if (event.getMap().getBasePath().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE))
		{
			event.addSprite(new ResourceLocation("quartzchests:block/chest_base"));
			event.addSprite(new ResourceLocation("quartzchests:block/chest_borders"));
		}
	}
}