package dev.latvian.mods.quartzchests.block.entity;

/**
 * @author LatvianModder
 */
public enum ColorType
{
	CHEST("chest", 0xEAD8C5),
	BORDER("border", 0x4A4040),
	TEXT("text", 0x222222);

	public static final ColorType[] VALUES = values();

	public final String name;
	public final String nbt;
	public final int index;
	public final int defaultColor;
	public final String translationKey;

	ColorType(String n, int d)
	{
		name = n;
		nbt = name + "_color";
		index = ordinal();
		defaultColor = d;
		translationKey = "block.quartzchests.chest." + name + "_color";
	}
}