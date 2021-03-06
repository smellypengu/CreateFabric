package com.simibubi.create.content.contraptions.components.crank;

import com.simibubi.create.AllBlockEntities;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.GeneratingKineticBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;

public class HandCrankBlockEntity extends GeneratingKineticBlockEntity {

	public int inUse;
	public boolean backwards;
	public float independentAngle;
	public float chasingVelocity;

	public HandCrankBlockEntity() {
		super(AllBlockEntities.HAND_CRANK);
	}

	public void turn(boolean back) {
		boolean update = false;

		if (getGeneratedSpeed() == 0 || back != backwards)
			update = true;

		inUse = 10;
		this.backwards = back;
		if (update && !world.isClient)
			updateGeneratedRotation();
	}

	@Override
	public float getGeneratedSpeed() {
		Block block = getCachedState().getBlock();
		if (!(block instanceof HandCrankBlock))
			return 0;
		HandCrankBlock crank = (HandCrankBlock) block;
		int speed = (inUse == 0 ? 0 : backwards ? -1 : 1) * crank.getRotationSpeed();
		return convertToDirection(speed, getCachedState().get(HandCrankBlock.FACING));
	}

	@Override
	public void toTag(CompoundTag compound, boolean clientPacket) {
		compound.putInt("InUse", inUse);
		super.toTag(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		inUse = compound.getInt("InUse");
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();

		float actualSpeed = getSpeed();
		chasingVelocity += ((actualSpeed * 10 / 3f) - chasingVelocity) * .25f;
		independentAngle += chasingVelocity;

		if (inUse > 0) {
			inUse--;

			if (inUse == 0 && !world.isClient)
				updateGeneratedRotation();
		}
	}
	
	@Override
	protected Block getStressConfigKey() {
		return AllBlocks.HAND_CRANK;
	}

	@Override
	public boolean shouldRenderAsBE() {
		return true;
	}
}
