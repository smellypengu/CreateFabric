package com.simibubi.create.content;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.ItemDescription;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum AllSections {

	/**
	 * Create's kinetic mechanisms
	 */
	KINETICS(ItemDescription.Palette.Red),

	/**
	 * Item transport and other Utility
	 */
	LOGISTICS(ItemDescription.Palette.Yellow),

	/**
	 * Tools for strucuture movement and replication
	 */
	SCHEMATICS(ItemDescription.Palette.Blue),

	/**
	 * Decorative blocks
	 */
	PALETTES(ItemDescription.Palette.Green),

	/**
	 * Base materials, ingredients and tools
	 */
	MATERIALS(ItemDescription.Palette.Green),

	/**
	 * Helpful gadgets and other shenanigans
	 */
	CURIOSITIES(ItemDescription.Palette.Purple),

	/**
	 * Fallback section
	 */
	UNASSIGNED(ItemDescription.Palette.Gray);

	private final ItemDescription.Palette tooltipPalette;

	AllSections(ItemDescription.Palette tooltipPalette) {
		this.tooltipPalette = tooltipPalette;
	}

	public ItemDescription.Palette getTooltipPalette() {
		return tooltipPalette;
	}
/* todo: registrate stuff
	public static AllSections of(ItemStack stack) {
	 Item item = stack.getItem();
	 if (item instanceof BlockItem)
	 return ofBlock(((BlockItem) item).getBlock());
	 return ofItem(item);
	 }

	 static AllSections ofItem(Item item) {
	 return Create.com.smellypengu.registrate()
		 .getSection(item);
	 }

	 static AllSections ofBlock(Block block) {
	 return Create.com.smellypengu.registrate()
		 .getSection(block);
	 }
*/
}