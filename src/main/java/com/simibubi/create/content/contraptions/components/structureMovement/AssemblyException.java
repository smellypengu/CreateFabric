package com.simibubi.create.content.contraptions.components.structureMovement;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class AssemblyException extends Exception {

	private static final long serialVersionUID = 1L;
	public final TranslatableText component;
	private BlockPos position = null;

	public AssemblyException(TranslatableText component) {
		this.component = component;
	}

	public AssemblyException(String langKey, Object... objects) {
		this(new TranslatableText("create.gui.assembly.exception." + langKey, objects));
	}

	public static void write(CompoundTag compound, AssemblyException exception) {
		if (exception == null)
			return;

		CompoundTag nbt = new CompoundTag();
		nbt.putString("Component", TranslatableText.Serializer.toJson(exception.component));
		if (exception.hasPosition())
			nbt.putLong("Position", exception.getPosition()
				.asLong());

		compound.put("LastException", nbt);
	}

	public static AssemblyException read(CompoundTag compound) {
		if (!compound.contains("LastException"))
			return null;

		CompoundTag nbt = compound.getCompound("LastException");
		String string = nbt.getString("Component");
		AssemblyException exception = new AssemblyException((TranslatableText) TranslatableText.Serializer.fromJson(string));
		if (nbt.contains("Position"))
			exception.position = BlockPos.fromLong(nbt.getLong("Position"));

		return exception;
	}

	public static AssemblyException unmovableBlock(BlockPos pos, BlockState state) {
		AssemblyException e = new AssemblyException("unmovableBlock", pos.getX(), pos.getY(), pos.getZ(),
			new TranslatableText(state.getBlock()
				.getTranslationKey()));
		e.position = pos;
		return e;
	}

	public static AssemblyException unloadedChunk(BlockPos pos) {
		AssemblyException e = new AssemblyException("chunkNotLoaded", pos.getX(), pos.getY(), pos.getZ());
		e.position = pos;
		return e;
	}

	public static AssemblyException structureTooLarge() {
		//return new AssemblyException("structureTooLarge", AllConfigs.SERVER.kinetics.maxBlocksMoved.get()); // TODO FIX ASSEMBLY EXCEPTIONS WITH CONFIG FILE
		return new AssemblyException("structureTooLarge", 1);
	}

	public static AssemblyException tooManyPistonPoles() {
		//return new AssemblyException("tooManyPistonPoles", AllConfigs.SERVER.kinetics.maxPistonPoles.get());
		return new AssemblyException("structureTooLarge", 1);
	}

	public static AssemblyException noPistonPoles() {
		return new AssemblyException("noPistonPoles");
	}

	public static AssemblyException notEnoughSails(int sails) {
		//return new AssemblyException("not_enough_sails", sails, AllConfigs.SERVER.kinetics.minimumWindmillSails.get());
		return new AssemblyException("structureTooLarge", 1);
	}

	public String getFormattedText() {
		return component.getString();
	}

	public boolean hasPosition() {
		return position != null;
	}

	public BlockPos getPosition() {
		return position;
	}
}
