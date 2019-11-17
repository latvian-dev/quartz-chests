package dev.latvian.mods.quartzchests;

import dev.latvian.mods.quartzchests.block.QuartzChestBlock;
import dev.latvian.mods.quartzchests.block.QuartzChestsBlocks;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestEntity;
import dev.latvian.mods.quartzchests.block.entity.QuartzChestRenderer;
import dev.latvian.mods.quartzchests.item.QuartzChestItem;
import dev.latvian.mods.quartzchests.item.QuartzChestsItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
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
		event.getMinecraftSupplier().get().getBlockColors().register(this::chestBlockColor, QuartzChestsBlocks.CHEST);
		event.getMinecraftSupplier().get().getItemColors().register(this::chestItemColor, QuartzChestsItems.CHEST);
	}

	private int chestBlockColor(BlockState state, IEnviromentBlockReader context, BlockPos pos, int index)
	{
		TileEntity entity = context.getTileEntity(pos);

		if (entity instanceof QuartzChestEntity)
		{
			return 0xFF000000 | (index == 1 ? ((QuartzChestEntity) entity).borderColor : ((QuartzChestEntity) entity).color);
		}

		return index == 1 ? 0xFF4A4040 : 0xFFFFFFFF;
	}

	private int chestItemColor(ItemStack stack, int index)
	{
		CompoundNBT data = stack.getChildTag("BlockEntityTag");

		if (data != null)
		{
			if (index == 1)
			{
				if (data.contains("border_color"))
				{
					return 0xFF000000 | data.getInt("border_color");
				}
			}
			else if (data.contains("color"))
			{
				return 0xFF000000 | data.getInt("color");
			}
		}

		return index == 1 ? 0xFF4A4040 : 0xFFFFFFFF;
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
		event.getRegistry().register(new QuartzChestItem(QuartzChestsBlocks.CHEST, properties).setRegistryName("chest"));
	}

	private void registerBlockEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		event.getRegistry().register(TileEntityType.Builder.create(QuartzChestEntity::new, QuartzChestsBlocks.CHEST).build(null).setRegistryName("chest"));
	}
}
