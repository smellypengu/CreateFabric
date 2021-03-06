package com.simibubi.create.foundation.utility.placement.util;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.create.foundation.utility.placement.PlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class PoleHelper<T extends Comparable<T>> implements PlacementHelper {

	protected final Predicate<BlockState> statePredicate;
	protected final Property<T> property;
	protected final Function<BlockState, Direction.Axis> axisFunction;

	public PoleHelper(Predicate<BlockState> statePredicate, Function<BlockState, Direction.Axis> axisFunction, Property<T> property) {
		this.statePredicate = statePredicate;
		this.axisFunction = axisFunction;
		this.property = property;
	}

	public boolean matchesAxis(BlockState state, Direction.Axis axis) {
		if (!statePredicate.test(state))
			return false;

		return axisFunction.apply(state) == axis;
	}

	public int attachedPoles(World world, BlockPos pos, Direction direction) {
		BlockPos checkPos = pos.offset(direction);
		BlockState state = world.getBlockState(checkPos);
		int count = 0;
		while (matchesAxis(state, direction.getAxis())) {
			count++;
			checkPos = checkPos.offset(direction);
			state = world.getBlockState(checkPos);
		}
		return count;
	}

	@Override
	public Predicate<BlockState> getStatePredicate() {
		return this.statePredicate;
	}

	@Override
	public PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockHitResult ray) {
		List<Direction> directions = PlacementHelper.orderedByDistance(pos, ray.getPos(), dir -> dir.getAxis() == axisFunction.apply(state));
		for (Direction dir : directions) {
			int poles = attachedPoles(world, pos, dir);
			BlockPos newPos = pos.offset(dir, poles + 1);
			BlockState newState = world.getBlockState(newPos);

			if (newState.getMaterial().isReplaceable())
				return PlacementOffset.success(newPos, bState -> bState.with(property, state.get(property)));

		}

		return PlacementOffset.fail();
	}

	@Override
	public void renderAt(BlockPos pos, BlockState state, BlockHitResult ray, PlacementOffset offset) {
		//Vec3d centerOffset = new Vec3d(ray.getFace().getDirectionVec()).scale(.3);
		//IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos).add(centerOffset), VecHelper.getCenterOf(offset.getPos()).add(centerOffset), ray.getFace(), 0.75D);

		displayGhost(offset);
	}
}
