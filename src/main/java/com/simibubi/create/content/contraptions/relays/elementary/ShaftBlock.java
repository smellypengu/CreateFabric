package com.simibubi.create.content.contraptions.relays.elementary;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.util.PoleHelper;

public class ShaftBlock extends AbstractShaftBlock {

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public ShaftBlock(Settings properties) {
		super(properties);
	}

	public static boolean isShaft(BlockState state) {
		return AllBlocks.SHAFT.stateManager.getStates().contains(state);
	}

	/**@Override
	public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return AllShapes.SIX_VOXEL_POLE.get(state.get(AXIS));
	}*/

	@Override
	public float getParticleTargetRadius() {
		return .25f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 0f;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
							  BlockHitResult ray) {
		if (player.isSneaking() || !player.canModifyBlocks())
			return ActionResult.PASS;

		ItemStack heldItem = player.getStackInHand(hand);
		/**for (EncasedShaftBlock encasedShaft : new EncasedShaftBlock[] { AllBlocks.ANDESITE_ENCASED_SHAFT.get(), TODO EncasedShaft CHECK
			AllBlocks.BRASS_ENCASED_SHAFT.get() }) {

			if (!encasedShaft.getCasing()
				.isIn(heldItem))
				continue;

			if (world.isClient)
				return ActionResult.SUCCESS;
			
			AllTriggers.triggerFor(AllTriggers.CASING_SHAFT, player);
			KineticTileEntity.switchToBlockState(world, pos, encasedShaft.getDefaultState()
				.with(AXIS, state.get(AXIS)));
			return ActionResult.SUCCESS;
		}*/

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		if (helper.matchesItem(heldItem))
			return helper.getOffset(world, state, pos, ray).placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);

		return ActionResult.PASS;
	}

	private static class PlacementHelper extends PoleHelper<Direction.Axis> {
		//used for extending a shaft in its axis, like the piston poles. works with shafts and cogs

		private PlacementHelper(){
			super(
					state -> state.getBlock() instanceof AbstractShaftBlock,
					state -> state.get(Properties.AXIS),
					Properties.AXIS
			);
		}

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return i -> i.getItem() instanceof BlockItem && ((BlockItem) i.getItem()).getBlock() instanceof AbstractShaftBlock;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return AllBlocks.SHAFT.stateManager.getStates()::contains;
		}
	}
}