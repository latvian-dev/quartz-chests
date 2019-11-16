package dev.latvian.mods.quartzchests;

import dev.latvian.mods.quartzchests.block.QuartzChestBlock;
import dev.latvian.mods.quartzchests.block.QuartzChestsBlocks;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestRenderer;
import dev.latvian.mods.quartzchests.item.QuartzChestItem;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QuartzChests.MOD_ID)
public class QuartzChests
{
	public static final String MOD_ID = "quartzchests";

	public QuartzChests()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, this::registerBlocks);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(TileEntityType.class, this::registerBlockEntities);
	}

	private void clientSetup(FMLClientSetupEvent event)
	{
		ClientRegistry.bindTileEntitySpecialRenderer(QuartzChestEntity.class, new QuartzChestRenderer());
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
		Item.Properties properties = new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(1);
		event.getRegistry().register(new QuartzChestItem(QuartzChestsBlocks.CHEST, properties).setRegistryName("chest"));
	}

	private void registerBlockEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		event.getRegistry().register(TileEntityType.Builder.create(QuartzChestEntity::new, QuartzChestsBlocks.CHEST).build(null).setRegistryName("chest"));
	}
}
