package dev.latvian.mods.quartzchests.gui;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author LatvianModder
 */
public class QuartzChestsContainers
{
	public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, "quartzchests");

	public static final RegistryObject<ContainerType<QuartzChestContainer>> CHEST = CONTAINERS.register("chest", () -> new ContainerType<>((IContainerFactory<QuartzChestContainer>) QuartzChestContainer::new));
}