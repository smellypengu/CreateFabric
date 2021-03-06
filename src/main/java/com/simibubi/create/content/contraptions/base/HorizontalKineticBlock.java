package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public abstract class HorizontalKineticBlock extends KineticBlock {

	public static final Property<Direction> HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;

	public HorizontalKineticBlock(Settings properties) {
		super(properties);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
		super.appendProperties(builder);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return this.getDefaultState()
			.with(HORIZONTAL_FACING, context.getPlayerFacing()
				.getOpposite());
	}

	public Direction getPreferredHorizontalFacing(ItemPlacementContext context) {
		Direction prefferedSide = null;
		for (Direction side : Iterate.horizontalDirections) {
			BlockState blockState = context.getWorld()
				.getBlockState(context.getBlockPos()
					.offset(side));
			if (blockState.getBlock() instanceof Rotating) {
				if (((Rotating) blockState.getBlock()).hasShaftTowards(context.getWorld(), context.getBlockPos()
					.offset(side), blockState, side.getOpposite()))
					if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
						prefferedSide = null;
						break;
					} else {
						prefferedSide = side;
					}
			}
		}
		return prefferedSide;
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rot) {
		return state.with(HORIZONTAL_FACING, rot.rotate(state.get(HORIZONTAL_FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.get(HORIZONTAL_FACING)));
	}

}
