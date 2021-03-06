package com.simibubi.create.content.contraptions.relays.elementary;

import static com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock.AXIS;

import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.Rotating;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.placement.PlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import com.simibubi.create.registrate.util.nullness.MethodsReturnNonnullByDefault;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.World;

public class CogwheelBlockItem extends BlockItem {

	boolean large;

	private final int placementHelperId;
	private final int integratedCogHelperId;

	public CogwheelBlockItem(CogWheelBlock block, Settings builder) {
		super(block, builder);
		large = block.isLarge;

		placementHelperId = PlacementHelpers.register(large ? new LargeCogHelper() : new SmallCogHelper());
		integratedCogHelperId = large ? PlacementHelpers.register(new IntegratedCogHelper()) : -1;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);

		PlacementHelper helper = PlacementHelpers.get(placementHelperId);
		PlayerEntity player = context.getPlayer();
		BlockHitResult ray = new BlockHitResult(context.getHitPos(), context.getSide(), pos, true);
		if (helper.matchesState(state) && player != null && !player.isSneaking()) {
			return helper.getOffset(world, state, pos, ray).placeInWorld(world, this, player, context.getHand(), ray);
		}

		if (integratedCogHelperId != -1) {
			helper = PlacementHelpers.get(integratedCogHelperId);

			if (helper.matchesState(state) && player != null && !player.isSneaking()) {
				return helper.getOffset(world, state, pos, ray).placeInWorld(world, this, player, context.getHand(), ray);
			}
		}

		return super.useOnBlock(context);
	}

	@Override
	// Trigger cogwheel criterion
	protected boolean place(ItemPlacementContext context, BlockState state) {
		triggerShiftingGearsAdvancement(context.getWorld(), context.getBlockPos(), state, context.getPlayer());
		return super.place(context, state);
	}

	protected void triggerShiftingGearsAdvancement(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (world.isClient || player == null)
			return;

		Axis axis = state.get(CogWheelBlock.AXIS);
		for (Axis perpendicular1 : Iterate.axes) {
			if (perpendicular1 == axis)
				continue;
			Direction d1 = Direction.get(AxisDirection.POSITIVE, perpendicular1);
			for (Axis perpendicular2 : Iterate.axes) {
				if (perpendicular1 == perpendicular2)
					continue;
				if (axis == perpendicular2)
					continue;
				Direction d2 = Direction.get(AxisDirection.POSITIVE, perpendicular2);
				for (int offset1 : Iterate.positiveAndNegative) {
					for (int offset2 : Iterate.positiveAndNegative) {
						BlockPos connectedPos = pos.offset(d1, offset1)
							.offset(d2, offset2);
						BlockState blockState = world.getBlockState(connectedPos);
						if (!(blockState.getBlock() instanceof CogWheelBlock))
							continue;
						if (blockState.get(CogWheelBlock.AXIS) != axis)
							continue;
						if (AllBlocks.LARGE_COGWHEEL.getStateManager().getStates().contains(blockState) == large)
							continue;
						//AllTriggers.triggerFor(AllTriggers.SHIFTING_GEARS, player);
					}
				}
			}
		}
	}

	@MethodsReturnNonnullByDefault
	private static class SmallCogHelper extends DiagonalCogHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return s -> AllBlocks.COGWHEEL.asItem().getDefaultStack() == s;
		}

		@Override
		public PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockHitResult ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (!((CogWheelBlock) state.getBlock()).isLarge) {
				List<Direction> directions =
					PlacementHelper.orderedByDistanceExceptAxis(pos, ray.getPos(), state.get(AXIS));

				for (Direction dir : directions) {
					BlockPos newPos = pos.offset(dir);

					if (hasLargeCogwheelNeighbor(world, newPos, state.get(AXIS)))
						continue;

					if (!world.getBlockState(newPos)
						.getMaterial()
						.isReplaceable())
						continue;

					return PlacementOffset.success(newPos, s -> s.with(AXIS, state.get(AXIS)));

				}

				return PlacementOffset.fail();
			}

			return super.getOffset(world, state, pos, ray);
		}

		@Override
		public void renderAt(BlockPos pos, BlockState state, BlockHitResult ray, PlacementOffset offset) {
			//IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()),
			//	Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, state.get(AXIS)),
			//	((CogWheelBlock) state.getBlock()).isLarge ? 1.5D : 0.75D);

			displayGhost(offset);
		}
	}

	@MethodsReturnNonnullByDefault
	private static class LargeCogHelper extends DiagonalCogHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return s -> AllBlocks.LARGE_COGWHEEL.asItem().getDefaultStack() == s;
		}

		@Override
		public PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockHitResult ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (((CogWheelBlock) state.getBlock()).isLarge) {
				Direction side = PlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getPos(), state.get(AXIS))
					.get(0);
				List<Direction> directions =
					PlacementHelper.orderedByDistanceExceptAxis(pos, ray.getPos(), state.get(AXIS));
				for (Direction dir : directions) {
					BlockPos newPos = pos.offset(dir)
						.offset(side);
					if (!world.getBlockState(newPos)
						.getMaterial()
						.isReplaceable())
						continue;

					return PlacementOffset.success(newPos, s -> s.with(AXIS, dir.getAxis()));
				}

				return PlacementOffset.fail();
			}

			return super.getOffset(world, state, pos, ray);
		}
	}

	@MethodsReturnNonnullByDefault
	public abstract static class DiagonalCogHelper implements PlacementHelper {

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return s -> s.getBlock() instanceof CogWheelBlock;
		}

		@Override
		public PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockHitResult ray) {
			// diagonal gears of different size
			Direction closest = PlacementHelper.orderedByDistanceExceptAxis(pos, ray.getPos(), state.get(AXIS))
				.get(0);
			List<Direction> directions = PlacementHelper.orderedByDistanceExceptAxis(pos, ray.getPos(),
				state.get(AXIS), d -> d.getAxis() != closest.getAxis());

			for (Direction dir : directions) {
				BlockPos newPos = pos.offset(dir)
					.offset(closest);
				if (!world.getBlockState(newPos)
					.getMaterial()
					.isReplaceable())
					continue;

				if (AllBlocks.COGWHEEL.getStateManager().getStates().contains(state) && hasSmallCogwheelNeighbor(world, newPos, state.get(AXIS)))
					continue;

				return PlacementOffset.success(newPos, s -> s.with(AXIS, state.get(AXIS)));
			}

			return PlacementOffset.fail();
		}

		@Override
		public void renderAt(BlockPos pos, BlockState state, BlockHitResult ray, PlacementOffset offset) {
			//IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()),
			//	Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, state.get(AXIS)), 1D);

			displayGhost(offset);
		}

		protected boolean hitOnShaft(BlockState state, BlockHitResult ray) {
			return AllShapes.SIX_VOXEL_POLE.get(state.get(AXIS))
				.getBoundingBox()
				.expand(0.001)
				.contains(ray.getPos()
					.subtract(ray.getPos()
						.floorAlongAxes(Iterate.axisSet)));
		}

		static public boolean hasLargeCogwheelNeighbor(World world, BlockPos pos, Axis axis) {
			for (Direction dir : Iterate.directions) {
				if (dir.getAxis() == axis)
					continue;

				if (AllBlocks.LARGE_COGWHEEL.getStateManager().getStates().contains(world.getBlockState(pos.offset(dir))))
					return true;
			}

			return false;
		}

		static public boolean hasSmallCogwheelNeighbor(World world, BlockPos pos, Axis axis) {
			for (Direction dir : Iterate.directions) {
				if (dir.getAxis() == axis)
					continue;

				if (AllBlocks.COGWHEEL.getStateManager().getStates().contains(world.getBlockState(pos.offset(dir))))
					return true;
			}

			return false;
		}
	}

	@MethodsReturnNonnullByDefault
	public static class IntegratedCogHelper implements PlacementHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return s -> AllBlocks.LARGE_COGWHEEL.asItem().getDefaultStack() == s;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return s -> !AllBlocks.COGWHEEL.getStateManager().getStates().contains(s) && s.getBlock() instanceof Rotating
				&& ((Rotating) s.getBlock()).hasIntegratedCogwheel(null, null, null);
		}

		@Override
		public PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockHitResult ray) {
			Direction face = ray.getSide();
			Axis newAxis;

			if (state.contains(HorizontalKineticBlock.HORIZONTAL_FACING))
				newAxis = state.get(HorizontalKineticBlock.HORIZONTAL_FACING)
					.getAxis();
			else if (state.contains(DirectionalKineticBlock.FACING))
				newAxis = state.get(DirectionalKineticBlock.FACING)
					.getAxis();
			else
				newAxis = Axis.Y;

			if (face.getAxis() == newAxis)
				return PlacementOffset.fail();

			List<Direction> directions =
				PlacementHelper.orderedByDistanceExceptAxis(pos, ray.getPos(), face.getAxis(), newAxis);

			for (Direction d : directions) {
				BlockPos newPos = pos.offset(face)
					.offset(d);

				if (!world.getBlockState(newPos)
					.getMaterial()
					.isReplaceable())
					continue;

				if (DiagonalCogHelper.hasLargeCogwheelNeighbor(world, newPos, newAxis)
					|| DiagonalCogHelper.hasSmallCogwheelNeighbor(world, newPos, newAxis))
					return PlacementOffset.fail();

				return PlacementOffset.success(newPos, s -> s.with(CogWheelBlock.AXIS, newAxis));
			}

			return PlacementOffset.fail();
		}

		@Override
		public void renderAt(BlockPos pos, BlockState state, BlockHitResult ray, PlacementOffset offset) {
			//IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()),
			//	Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, offset.getTransform()
			//		.apply(AllBlocks.LARGE_COGWHEEL.getDefaultState())
			//		.get(AXIS)));

			displayGhost(offset);
		}
	}
}
