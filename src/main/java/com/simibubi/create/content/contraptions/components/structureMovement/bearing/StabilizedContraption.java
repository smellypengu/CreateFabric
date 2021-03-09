package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.content.contraptions.components.structureMovement.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class StabilizedContraption extends Contraption {

	private Direction facing;

	public StabilizedContraption() {}

	public StabilizedContraption(Direction facing) {
		this.facing = facing;
	}

	@Override
	public boolean assemble(World world, BlockPos pos) throws AssemblyException {
		BlockPos offset = pos.offset(facing);
		if (!searchMovedStructure(world, offset, null))
			return false;
		startMoving(world);
		if (blocks.isEmpty())
			return false;
		return true;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return false;
	}

	@Override
	protected ContraptionType getType() {
		return ContraptionType.STABILIZED;
	}
	
	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("Facing", facing.getId());
		return tag;
	}

	@Override
	public void readNBT(World world, CompoundTag tag, boolean spawnData) {
		facing = Direction.byId(tag.getInt("Facing"));
		super.readNBT(world, tag, spawnData);
	}
	
	@Override
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		return false;
	}
	
	public Direction getFacing() {
		return facing;
	}

	@Override
	public ContraptionLighter<?> makeLighter() {
		return new NonStationaryLighter<>(this);
	}
}
