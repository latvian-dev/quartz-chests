package dev.latvian.mods.quartzchests;

import dev.latvian.mods.quartzchests.block.QuartzChestBlock;
import dev.latvian.mods.quartzchests.block.QuartzChestsBlocks;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import dev.latvian.mods.quartzchests.client.QuartzChestItemRenderer;
import dev.latvian.mods.quartzchests.client.QuartzChestsClient;
import dev.latvian.mods.quartzchests.gui.QuartzChestContainer;
import dev.latvian.mods.quartzchests.net.QuartzChestsNet;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.IContainerFactory;

@Mod(QuartzChests.MOD_ID)
public class QuartzChests
{
	public static final String MOD_ID = "quartzchests";

	public QuartzChests()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, this::registerBlocks);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(TileEntityType.class, this::registerBlockEntities);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ContainerType.class, this::registerContainers);

		//noinspection Convert2MethodRef
		DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> new QuartzChestsClient());
	}

	private void init(FMLCommonSetupEvent event)
	{
		QuartzChestsNet.init();
	}

	private void registerBlocks(RegistryEvent.Register<Block> event)
	{
		Block.Properties properties = Block.Properties.create(Material.WOOD, MaterialColor.QUARTZ);
		properties.hardnessAndResistance(2F);
		properties.sound(SoundType.WOOD);
		event.getRegistry().register(new QuartzChestBlock(properties).setRegistryName("chest"));
	}

	private void registerItems(RegistryEvent.Register<Item> event)
	{
		Item.Properties properties = new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(16);
		//noinspection Convert2MethodRef
		properties.setTEISR(() -> () -> new QuartzChestItemRenderer());
		event.getRegistry().register(new BlockItem(QuartzChestsBlocks.CHEST, properties).setRegistryName("chest"));
	}

	private void registerBlockEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		event.getRegistry().register(TileEntityType.Builder.create(QuartzChestEntity::new, QuartzChestsBlocks.CHEST).build(null).setRegistryName("chest"));
	}

	private void registerContainers(RegistryEvent.Register<ContainerType<?>> event)
	{
		event.getRegistry().register(new ContainerType<>((IContainerFactory<QuartzChestContainer>) QuartzChestContainer::new).setRegistryName("chest"));
	}
}
