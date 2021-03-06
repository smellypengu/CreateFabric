package com.simibubi.create.content.contraptions.relays.encased;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.Direction;

public abstract class SplitShaftBlockEntity extends DirectionalShaftHalvesBlockEntity {

	public SplitShaftBlockEntity(BlockEntityType<?> typeIn) {
		super(typeIn);
	}

	public abstract float getRotationSpeedModifier(Direction face);
	
}
