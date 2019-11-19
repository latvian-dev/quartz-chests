package dev.latvian.mods.quartzchests.net;

import dev.latvian.mods.quartzchests.QuartzChests;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class QuartzChestsNet
{
	public static SimpleChannel MAIN;
	private static final String MAIN_VERSION = "1";

	public static void init()
	{
		MAIN = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(QuartzChests.MOD_ID, "main"))
				.clientAcceptedVersions(MAIN_VERSION::equals)
				.serverAcceptedVersions(MAIN_VERSION::equals)
				.networkProtocolVersion(() -> MAIN_VERSION)
				.simpleChannel();

		MAIN.registerMessage(1, SetLabelMessage.class, SetLabelMessage::write, SetLabelMessage::new, SetLabelMessage::handle);
		MAIN.registerMessage(2, SetChestColorMessage.class, SetChestColorMessage::write, SetChestColorMessage::new, SetChestColorMessage::handle);
		MAIN.registerMessage(3, SetBorderColorMessage.class, SetBorderColorMessage::write, SetBorderColorMessage::new, SetBorderColorMessage::handle);
		MAIN.registerMessage(4, SetTextColorMessage.class, SetTextColorMessage::write, SetTextColorMessage::new, SetTextColorMessage::handle);
		MAIN.registerMessage(5, SetIconMessage.class, SetIconMessage::write, SetIconMessage::new, SetIconMessage::handle);
	}
}