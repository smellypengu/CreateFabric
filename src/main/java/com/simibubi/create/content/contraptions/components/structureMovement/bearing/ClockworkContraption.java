package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.foundation.utility.CNBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ClockworkContraption extends Contraption {

	protected Direction facing;
	public HandType handType;
	public int offset;
	private Set<BlockPos> ignoreBlocks = new HashSet<>();

	@Override
	protected ContraptionType getType() {
		return ContraptionType.CLOCKWORK;
	}

	private void ignoreBlocks(Set<BlockPos> blocks, BlockPos anchor) {
		for (BlockPos blockPos : blocks)
			ignoreBlocks.add(anchor.add(blockPos));
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor.offset(facing.getOpposite(), offset + 1));
	}

	public static Pair<ClockworkContraption, ClockworkContraption> assembleClockworkAt(World world, BlockPos pos,
		Direction direction) throws AssemblyException {
		int hourArmBlocks = 0;

		ClockworkContraption hourArm = new ClockworkContraption();
		ClockworkContraption minuteArm = null;

		hourArm.facing = direction;
		hourArm.handType = HandType.HOUR;
		if (!hourArm.assemble(world, pos))
			return null;
		for (int i = 0; i < 16; i++) {
			BlockPos offsetPos = BlockPos.ORIGIN.offset(direction, i);
			if (hourArm.getBlocks()
				.containsKey(offsetPos))
				continue;
			hourArmBlocks = i;
			break;
		}

		if (hourArmBlocks > 0) {
			minuteArm = new ClockworkContraption();
			minuteArm.facing = direction;
			minuteArm.handType = HandType.MINUTE;
			minuteArm.offset = hourArmBlocks;
			minuteArm.ignoreBlocks(hourArm.getBlocks()
				.keySet(), hourArm.anchor);
			if (!minuteArm.assemble(world, pos))
				return null;
			if (minuteArm.getBlocks()
				.isEmpty())
				minuteArm = null;
		}

		hourArm.startMoving(world);
		hourArm.expandBoundsAroundAxis(direction.getAxis());
		if (minuteArm != null) {
			minuteArm.startMoving(world);
			minuteArm.expandBoundsAroundAxis(direction.getAxis());
		}
		return Pair.of(hourArm, minuteArm);
	}
	
	@Override
	public boolean assemble(World world, BlockPos pos) throws AssemblyException {
		return searchMovedStructure(world, pos, facing);
	}

	@Override
	public boolean searchMovedStructure(World world, BlockPos pos, Direction direction) throws AssemblyException {
		return super.searchMovedStructure(world, pos.offset(direction, offset + 1), null);
	}

	@Override
	protected boolean moveBlock(World world, Direction direction, Queue<BlockPos> frontier,
		Set<BlockPos> visited) throws AssemblyException {
		if (ignoreBlocks.contains(frontier.peek())) {
			frontier.poll();
			return true;
		}
		return super.moveBlock(world, direction, frontier, visited);
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("facing", facing.getId());
		tag.putInt("offset", offset);
		CNBTHelper.writeEnum(tag, "HandType", handType);
		return tag;
	}

	@Override
	public void readNBT(World world, CompoundTag tag, boolean spawnData) {
		facing = Direction.byId(tag.getInt("facing"));
		handType = CNBTHelper.readEnum(tag, "HandType", HandType.class);
		offset = tag.getInt("offset");
		super.readNBT(world, tag, spawnData);
	}

	@Override
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		if (BlockPos.ORIGIN.equals(localPos) || BlockPos.ORIGIN.equals(localPos.offset(facing)))
			return false;
		return facing.getAxis() == this.facing.getAxis();
	}
	
	public static enum HandType {
		HOUR, MINUTE
	}

}
