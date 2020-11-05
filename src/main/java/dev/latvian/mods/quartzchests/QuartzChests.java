package dev.latvian.mods.quartzchests;

import dev.latvian.mods.quartzchests.block.QuartzChestsBlocks;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestsBlockEntities;
import dev.latvian.mods.quartzchests.client.QuartzChestsClient;
import dev.latvian.mods.quartzchests.gui.QuartzChestsContainers;
import dev.latvian.mods.quartzchests.item.QuartzChestsItems;
import dev.latvian.mods.quartzchests.net.QuartzChestsNet;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("quartzchests")
public class QuartzChests
{
	public static QuartzChests instance;
	public final QuartzChestsCommon proxy;

	public QuartzChests()
	{
		instance = this;
		proxy = DistExecutor.safeRunForDist(() -> QuartzChestsClient::new, () -> QuartzChestsCommon::new);
		QuartzChestsBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
		QuartzChestsItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
		QuartzChestsBlockEntities.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
		QuartzChestsContainers.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());

		proxy.init();
		QuartzChestsNet.init();
	}
}
