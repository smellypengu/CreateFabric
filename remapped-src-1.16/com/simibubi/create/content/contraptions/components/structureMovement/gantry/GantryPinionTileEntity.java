package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import static net.minecraft.state.property.Properties.FACING;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlock;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class GantryPinionTileEntity extends KineticTileEntity implements IDisplayAssemblyExceptions {

	boolean assembleNextTick;
	protected AssemblyException lastException;

	public GantryPinionTileEntity(BlockEntityType<?> typeIn) {
		super(typeIn);
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
	}

	public void checkValidGantryShaft() {
		if (shouldAssemble())
			queueAssembly();
	}

	public void queueAssembly() {
		assembleNextTick = true;
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isClient)
			return;

		if (assembleNextTick) {
			tryAssemble();
			assembleNextTick = false;
		}
	}

	@Override
	public AssemblyException getLastAssemblyException() {
		return lastException;
	}

	private void tryAssemble() {
		BlockState blockState = getCachedState();
		if (!(blockState.getBlock() instanceof GantryPinionBlock))
			return;

		Direction direction = blockState.get(FACING);
		GantryContraption contraption = new GantryContraption(direction);

		BlockEntity shaftTe = world.getBlockEntity(pos.offset(direction.getOpposite()));
		if (!(shaftTe instanceof GantryShaftTileEntity))
			return;
		BlockState shaftState = shaftTe.getCachedState();
		if (!AllBlocks.GANTRY_SHAFT.has(shaftState))
			return;

		float pinionMovementSpeed = ((GantryShaftTileEntity) shaftTe).getPinionMovementSpeed();
		Direction shaftOrientation = shaftState.get(GantryShaftBlock.FACING);
		Direction movementDirection = shaftOrientation;
		if (pinionMovementSpeed < 0)
			movementDirection = movementDirection.getOpposite();

		try {
			lastException = null;
			if (!contraption.assemble(world, pos))
				return;

			sendData();
		} catch (AssemblyException e) {
			lastException = e;
			sendData();
			return;
		}
		if (ContraptionCollider.isCollidingWithWorld(world, contraption, pos.offset(movementDirection),
			movementDirection))
			return;

		contraption.removeBlocksFromWorld(world, BlockPos.ORIGIN);
		GantryContraptionEntity movedContraption =
			GantryContraptionEntity.create(world, contraption, shaftOrientation);
		BlockPos anchor = pos;
		movedContraption.updatePosition(anchor.getX(), anchor.getY(), anchor.getZ());
		world.spawnEntity(movedContraption);
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		AssemblyException.write(compound, lastException);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		lastException = AssemblyException.read(compound);
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public float propagateRotationTo(KineticTileEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		float defaultModifier =
			super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);

		if (connectedViaAxes)
			return defaultModifier;
		if (!AllBlocks.GANTRY_SHAFT.has(stateTo))
			return defaultModifier;
		if (!stateTo.get(GantryShaftBlock.POWERED))
			return defaultModifier;

		Direction direction = Direction.getFacing(diff.getX(), diff.getY(), diff.getZ());
		if (stateFrom.get(GantryPinionBlock.FACING) != direction.getOpposite())
			return defaultModifier;
		return getGantryPinionModifier(stateTo.get(GantryShaftBlock.FACING), stateFrom.get(GantryPinionBlock.FACING));
	}

	public static float getGantryPinionModifier(Direction shaft, Direction pinionDirection) {
		Axis shaftAxis = shaft.getAxis();
		float directionModifier = shaft.getDirection()
			.offset();
		if (shaftAxis == Axis.Y)
			if (pinionDirection == Direction.NORTH || pinionDirection == Direction.EAST)
				return -directionModifier;
		if (shaftAxis == Axis.X)
			if (pinionDirection == Direction.DOWN || pinionDirection == Direction.SOUTH)
				return -directionModifier;
		if (shaftAxis == Axis.Z)
			if (pinionDirection == Direction.UP || pinionDirection == Direction.WEST)
				return -directionModifier;
		return directionModifier;
	}

	private boolean shouldAssemble() {
		BlockState blockState = getCachedState();
		if (!(blockState.getBlock() instanceof GantryPinionBlock))
			return false;
		Direction facing = blockState.get(GantryPinionBlock.FACING)
			.getOpposite();
		BlockState shaftState = world.getBlockState(pos.offset(facing));
		if (!(shaftState.getBlock() instanceof GantryShaftBlock))
			return false;
		if (shaftState.get(GantryShaftBlock.POWERED))
			return false;
		BlockEntity te = world.getBlockEntity(pos.offset(facing));
		return te instanceof GantryShaftTileEntity && ((GantryShaftTileEntity) te).canAssembleOn();
	}

	@Override
	public boolean shouldRenderAsTE() {
		return true;
	}
}
