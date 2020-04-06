package dev.latvian.mods.quartzchests.block.entity;

import dev.latvian.mods.quartzchests.block.QuartzChestsBlocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author LatvianModder
 */
public class QuartzChestsBlockEntities
{
	public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, "quartzchests");

	public static final RegistryObject<TileEntityType<QuartzChestEntity>> CHEST = BLOCK_ENTITIES.register("chest", () -> TileEntityType.Builder.create(QuartzChestEntity::new, QuartzChestsBlocks.CHEST.get()).build(null));
}