package com.simibubi.create.content.contraptions.goggles;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

/*
 * Implement this Interface in the TileEntity class that wants to add info to the screen
 * */
public interface GoggleInformationProvider {

	DecimalFormat decimalFormat = new DecimalFormat("#.##");
	String spacing = "    ";
	public static Text componentSpacing = new LiteralText(spacing);

	static String format(double d) {
		return decimalFormat.format(d);
	}

	/**
	 * this method will be called when looking at a TileEntity that implemented this interface
	 *
	 * @return {@code true} if the tooltip creation was successful and should be displayed,
	 * or {@code false} if the overlay should not be displayed
	 */
	default boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		return false;
	}

}
