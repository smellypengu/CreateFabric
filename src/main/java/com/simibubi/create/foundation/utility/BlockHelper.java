package com.simibubi.create.foundation.utility;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class BlockHelper {

	/**@Environment(EnvType.CLIENT)
	public static void addReducedDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
		VoxelShape voxelshape = state.getShape(world, pos);
		MutableInt amtBoxes = new MutableInt(0);
		voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> amtBoxes.increment());
		double chance = 1d / amtBoxes.getValue();

		voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
			double d1 = Math.min(1.0D, x2 - x1);
			double d2 = Math.min(1.0D, y2 - y1);
			double d3 = Math.min(1.0D, z2 - z1);
			int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
			int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
			int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

			for (int l = 0; l < i; ++l) {
				for (int i1 = 0; i1 < j; ++i1) {
					for (int j1 = 0; j1 < k; ++j1) {
						if (world.rand.nextDouble() > chance)
							continue;

						double d4 = ((double) l + 0.5D) / (double) i;
						double d5 = ((double) i1 + 0.5D) / (double) j;
						double d6 = ((double) j1 + 0.5D) / (double) k;
						double d7 = d4 * d1 + x1;
						double d8 = d5 * d2 + y1;
						double d9 = d6 * d3 + z1;
						manager
							.addEffect((new DiggingParticle(world, (double) pos.getX() + d7, (double) pos.getY() + d8,
								(double) pos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, state)).setBlockPos(pos));
					}
				}
			}

		});
	}*/

	public static BlockState setZeroAge(BlockState blockState) {
		if (blockState.contains(Properties.AGE_1))
			return blockState.with(Properties.AGE_1, 0);
		if (blockState.contains(Properties.AGE_2))
			return blockState.with(Properties.AGE_2, 0);
		if (blockState.contains(Properties.AGE_3))
			return blockState.with(Properties.AGE_3, 0);
		if (blockState.contains(Properties.AGE_5))
			return blockState.with(Properties.AGE_5, 0);
		if (blockState.contains(Properties.AGE_7))
			return blockState.with(Properties.AGE_7, 0);
		if (blockState.contains(Properties.AGE_15))
			return blockState.with(Properties.AGE_15, 0);
		if (blockState.contains(Properties.AGE_25))
			return blockState.with(Properties.AGE_25, 0);
		if (blockState.contains(Properties.HONEY_LEVEL))
			return blockState.with(Properties.HONEY_LEVEL, 0);
		if (blockState.contains(Properties.HATCH))
			return blockState.with(Properties.HATCH, 0);
		if (blockState.contains(Properties.STAGE))
			return blockState.with(Properties.STAGE, 0);
		if (blockState.contains(Properties.LEVEL_3))
			return blockState.with(Properties.LEVEL_3, 0);
		if (blockState.contains(Properties.LEVEL_8))
			return blockState.with(Properties.LEVEL_8, 0);
		if (blockState.contains(Properties.EXTENDED))
			return blockState.with(Properties.EXTENDED, false);
		return blockState;
	}

	public static int findAndRemoveInInventory(BlockState block, PlayerEntity player, int amount) {
		int amountFound = 0;
		Item required = getRequiredItem(block).getItem();

		boolean needsTwo =
			block.contains(Properties.SLAB_TYPE) && block.get(Properties.SLAB_TYPE) == SlabType.DOUBLE;

		if (needsTwo)
			amount *= 2;

		if (block.contains(Properties.EGGS))
			amount *= block.get(Properties.EGGS);

		if (block.contains(Properties.PICKLES))
			amount *= block.get(Properties.PICKLES);

		{
			// Try held Item first
			int preferredSlot = player.inventory.selectedSlot;
			ItemStack itemstack = player.inventory.getStack(preferredSlot);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setStack(preferredSlot,
					new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		// Search inventory
		for (int i = 0; i < player.inventory.size(); ++i) {
			if (amountFound == amount)
				break;

			ItemStack itemstack = player.inventory.getStack(i);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setStack(i, new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		if (needsTwo) {
			// Give back 1 if uneven amount was removed
			if (amountFound % 2 != 0)
				player.inventory.insertStack(new ItemStack(required));
			amountFound /= 2;
		}

		return amountFound;
	}

	public static ItemStack getRequiredItem(BlockState state) {
		ItemStack itemStack = new ItemStack(state.getBlock());
		if (itemStack.getItem() == Items.FARMLAND)
			itemStack = new ItemStack(Items.DIRT);
		else if (itemStack.getItem() == Items.GRASS_PATH)
			itemStack = new ItemStack(Items.GRASS_BLOCK);
		return itemStack;
	}

	/**public static void destroyBlock(World world, BlockPos pos, float effectChance) {
		destroyBlock(world, pos, effectChance, stack -> Block.dropStack(world, pos, stack));
	}

	public static void destroyBlock(World world, BlockPos pos, float effectChance,
		Consumer<ItemStack> droppedItemCallback) {
		FluidState ifluidstate = world.getFluidState(pos);
		BlockState state = world.getBlockState(pos);
		if (world.random.nextFloat() < effectChance)
			world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));
		BlockEntity tileentity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;

		if (world.getGameRules()
			.getBoolean(GameRules.DO_TILE_DROPS) && !world.restoringBlockSnapshots) {
			for (ItemStack itemStack : Block.getDroppedStacks(state, (ServerWorld) world, pos, tileentity))
				droppedItemCallback.accept(itemStack);
			state.onStacksDropped((ServerWorld) world, pos, ItemStack.EMPTY);
		}

		world.setBlockState(pos, ifluidstate.getBlockState());
	}*/

	/**public static boolean isSolidWall(BlockView reader, BlockPos fromPos, Direction toDirection) {
		return Block.hasSolidSide(reader.getBlockState(fromPos.offset(toDirection)), reader,
			fromPos.offset(toDirection), toDirection.getOpposite());
	}*/
	
	public static boolean noCollisionInSpace(BlockView reader, BlockPos pos) {
		return reader.getBlockState(pos).getCollisionShape(reader, pos).isEmpty();
	}

}
