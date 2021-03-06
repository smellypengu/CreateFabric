package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.AllBlockEntities;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.base.Rotating;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltInventory;
import com.simibubi.create.foundation.utility.CNBTHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LightType;

import java.util.Optional;

import static net.minecraft.util.math.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.math.Direction.AxisDirection.POSITIVE;

public class BeltBlockEntity extends KineticBlockEntity {

	/**
	 * public Map<Entity, TransportedEntityInfo> passengers;
	 */
	public Optional<DyeColor> color;
	public int beltLength;
	public int index;
	public Direction lastInsert;
	public CasingType casing;
	/**
	 * protected LazyOptional<IItemHandler> itemHandler;
	 */

	public CompoundTag trackerUpdateTag;
	// client
	public byte blockLight = -1;
	public byte skyLight = -1;
	protected BlockPos controller;
	protected BeltInventory inventory;

	public BeltBlockEntity() {
		super(AllBlockEntities.BELT);
		controller = BlockPos.ORIGIN;
		/**itemHandler = LazyOptional.empty();*/
		casing = CasingType.NONE;
		color = Optional.empty();
	}

	/**
	 * @Override public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	 * super.addBehaviours(behaviours);
	 * behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::canInsertFrom)
	 * .setInsertionHandler(this::tryInsertingFromSide));
	 * behaviours.add(new TransportedItemStackHandlerBehaviour(this, this::applyToAllItems)
	 * .withStackPlacement(this::getWorldPositionOf));
	 * }
	 */

	@Override
	public void lazyTick() {
		super.lazyTick();

		// Init belt
		if (beltLength == 0)
			BeltBlock.initBelt(world, pos);
		/**if (!AllBlocks.BELT.has(world.getBlockState(pos)))
		 return;

		 initializeItemHandler();

		 if (blockLight == -1)
		 updateLight();

		 // Move Items
		 if (!isController())
		 return;
		 getInventory().tick();

		 if (getSpeed() == 0)
		 return;

		 // Move Entities
		 if (passengers == null)
		 passengers = new HashMap<>();

		 List<Entity> toRemove = new ArrayList<>();
		 passengers.forEach((entity, info) -> {
		 boolean canBeTransported = BeltMovementHandler.canBeTransported(entity);
		 boolean leftTheBelt =
		 info.getTicksSinceLastCollision() > ((getBlockState().get(BeltBlock.SLOPE) != HORIZONTAL) ? 3 : 1);
		 if (!canBeTransported || leftTheBelt) {
		 toRemove.add(entity);
		 return;
		 }

		 info.tick();
		 BeltMovementHandler.transportEntity(this, entity, info);
		 });
		 toRemove.forEach(passengers::remove);*/
	}

	@Override
	public float calculateStressApplied() {
		if (!isController())
			return 0;
		return super.calculateStressApplied();
	}

	/**
	 * @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
	 * if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
	 * if (side == Direction.UP || BeltBlock.canAccessFromSide(side, getBlockState())) {
	 * return itemHandler.cast();
	 * }
	 * }
	 * return super.getCapability(cap, side);
	 * }
	 */

	@Override
	public void markRemoved() {
		super.markRemoved();
		/**itemHandler.invalidate();*/
	}

	/**@Override public Box makeRenderBoundingBox() {
	if (!isController())
	return super.makeRenderBoundingBox();
	else
	return super.makeRenderBoundingBox().expand(beltLength + 1);
	}*/

	/*protected void initializeItemHandler() {
		if (world.isClient || itemHandler.isPresent())
			return;
		if (!world.isBlockPresent(controller))
			return;
		BlockEntity te = world.getBlockEntity(controller);
		if (te == null || !(te instanceof BeltTileEntity))
			return;
		BeltInventory inventory = ((BeltTileEntity) te).getInventory();
		if (inventory == null)
			return;
		IItemHandler handler = new ItemHandlerBeltSegment(inventory, index);
		itemHandler = LazyOptional.of(() -> handler);
	}*/

	/**
	 * @Override public boolean hasFastRenderer() {
	 * return !isController();
	 * }
	 */

	@Override
	public void toTag(CompoundTag compound, boolean clientPacket) {
		if (controller != null)
			compound.put("Controller", NbtHelper.fromBlockPos(controller));
		compound.putBoolean("IsController", isController());
		compound.putInt("Length", beltLength);
		compound.putInt("Index", index);
		CNBTHelper.writeEnum(compound, "Casing", casing);

		if (color.isPresent())
			CNBTHelper.writeEnum(compound, "Dye", color.get());

		if (isController())
			compound.put("Inventory", getInventory().write());
		super.toTag(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);

		/**if (compound.getBoolean("IsController"))
		 controller = pos;

		 color = compound.contains("Dye") ? Optional.of(NBTHelperC.readEnum(compound, "Dye", DyeColor.class))
		 : Optional.empty();

		 if (!wasMoved) {
		 if (!isController())
		 controller = NbtHelper.toBlockPos(compound.getCompound("Controller"));
		 trackerUpdateTag = compound;
		 beltLength = compound.getInt("Length");
		 index = compound.getInt("Index");
		 }

		 if (isController())
		 getInventory().read(compound.getCompound("Inventory"));

		 CasingType casingBefore = casing;
		 casing = NBTHelperC.readEnum(compound, "Casing", CasingType.class);

		 if (!clientPacket)
		 return;

		 if (casingBefore == casing)
		 return;
		 requestModelDataUpdate();
		 if (hasWorld())
		 world.notifyBlockUpdate(getPos(), getCachedState(), getCachedState(), 16);*/
	}

	@Override
	public void clearKineticInformation() {
		super.clearKineticInformation();
		beltLength = 0;
		index = 0;
		controller = null;
		trackerUpdateTag = new CompoundTag();
	}

	/**
	 * public void applyColor(DyeColor colorIn) {
	 * if (colorIn == null) {
	 * if (!color.isPresent())
	 * return;
	 * } else if (color.isPresent() && color.get() == colorIn)
	 * return;
	 * <p>
	 * for (BlockPos blockPos : BeltBlock.getBeltChain(world, getController())) {
	 * BeltTileEntity belt = BeltHelper.getSegmentTE(world, blockPos);
	 * if (belt == null)
	 * continue;
	 * belt.color = Optional.ofNullable(colorIn);
	 * belt.markDirty();
	 * belt.sendData();
	 * DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FastRenderDispatcher.enqueueUpdate(belt));
	 * }
	 * }
	 */

	public BeltBlockEntity getControllerTE() {
		if (controller == null)
			return null;
		if (!world.canSetBlock(controller))
			return null;
		BlockEntity te = world.getBlockEntity(controller);
		if (te == null || !(te instanceof BeltBlockEntity))
			return null;
		return (BeltBlockEntity) te;
	}

	public BlockPos getController() {
		return controller == null ? pos : controller;
	}

	public boolean isController() {
		return controller != null &&
			pos.getX() == controller.getX() &&
			pos.getY() == controller.getY() &&
			pos.getZ() == controller.getZ();
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
		/**cachedBoundingBox = null;*/
	}

	public float getBeltMovementSpeed() {
		return getSpeed() / 480f;
	}

	public float getDirectionAwareBeltMovementSpeed() {
		int offset = getBeltFacing().getDirection()
			.offset();
		if (getBeltFacing().getAxis() == Direction.Axis.X)
			offset *= -1;
		return getBeltMovementSpeed() * offset;
	}

	public boolean hasPulley() {
		if (!AllBlocks.BELT.getStateManager().getStates().contains(getCachedState()))
			return false;
		return getCachedState().get(BeltBlock.PART) != BeltPart.MIDDLE;
	}

	protected boolean isLastBelt() {
		if (getSpeed() == 0)
			return false;

		Direction direction = getBeltFacing();
		if (getCachedState().get(BeltBlock.SLOPE) == BeltSlope.VERTICAL)
			return false;

		BeltPart part = getCachedState().get(BeltBlock.PART);
		if (part == BeltPart.MIDDLE)
			return false;

		boolean movingPositively = (getSpeed() > 0 == (direction.getDirection()
			.offset() == 1)) ^ direction.getAxis() == Direction.Axis.X;
		return part == BeltPart.START ^ movingPositively;
	}

	public Vec3i getMovementDirection(boolean firstHalf) {
		return this.getMovementDirection(firstHalf, false);
	}

	public Vec3i getBeltChainDirection() {
		return this.getMovementDirection(true, true);
	}

	protected Vec3i getMovementDirection(boolean firstHalf, boolean ignoreHalves) {
		if (getSpeed() == 0)
			return BlockPos.ZERO;

		final BlockState blockState = getCachedState();
		final Direction beltFacing = blockState.get(Properties.HORIZONTAL_FACING);
		final BeltSlope slope = blockState.get(BeltBlock.SLOPE);
		final BeltPart part = blockState.get(BeltBlock.PART);
		final Direction.Axis axis = beltFacing.getAxis();

		Direction movementFacing = Direction.get(axis == Direction.Axis.X ? NEGATIVE : POSITIVE, axis);
		boolean notHorizontal = blockState.get(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL;
		if (getSpeed() < 0)
			movementFacing = movementFacing.getOpposite();
		Vec3i movement = movementFacing.getVector();

		boolean slopeBeforeHalf = (part == BeltPart.END) == (beltFacing.getDirection() == POSITIVE);
		boolean onSlope = notHorizontal && (part == BeltPart.MIDDLE || slopeBeforeHalf == firstHalf || ignoreHalves);
		boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

		if (!onSlope)
			return movement;

		return new Vec3i(movement.getX(), movingUp ? 1 : -1, movement.getZ());
	}

	public Direction getMovementFacing() {
		Direction.Axis axis = getBeltFacing().getAxis();
		return Direction.get(
			getBeltMovementSpeed() < 0 ^ axis == Direction.Axis.X ? NEGATIVE : POSITIVE, axis);
	}

	protected Direction getBeltFacing() {
		return getCachedState().get(Properties.HORIZONTAL_FACING);
	}

	public BeltInventory getInventory() {
		if (!isController()) {
			BeltBlockEntity controllerTE = getControllerTE();
			if (controllerTE != null)
				return controllerTE.getInventory();
			return null;
		}
		if (inventory == null) {
			inventory = new BeltInventory(this);
		}
		return inventory;
	}

	/**
	 * public void setCasingType(CasingType type) {
	 * if (casing == type)
	 * return;
	 * if (casing != CasingType.NONE)
	 * world.playEvent(2001, pos,
	 * Block.getStateId(casing == CasingType.ANDESITE ? AllBlocks.ANDESITE_CASING.getDefaultState()
	 * : AllBlocks.BRASS_CASING.getDefaultState()));
	 * casing = type;
	 * boolean shouldBlockHaveCasing = type != CasingType.NONE;
	 * BlockState blockState = getBlockState();
	 * if (blockState.get(BeltBlock.CASING) != shouldBlockHaveCasing)
	 * KineticTileEntity.switchToBlockState(world, pos, blockState.with(BeltBlock.CASING, shouldBlockHaveCasing));
	 * markDirty();
	 * sendData();
	 * }
	 */

	private boolean canInsertFrom(Direction side) {
		if (getSpeed() == 0)
			return false;
		return getMovementFacing() != side.getOpposite();
	}

	/**private void applyToAllItems(float maxDistanceFromCenter,
	 Function<TransportedItemStack, TransportedResult> processFunction) {
	 BeltTileEntity controller = getControllerTE();
	 if (controller == null)
	 return;
	 BeltInventory inventory = controller.getInventory();
	 if (inventory != null)
	 inventory.applyToEachWithin(index + .5f, maxDistanceFromCenter, processFunction);
	 }*/

	/**private Vec3d getWorldPositionOf(TransportedItemStack transported) {
	 BeltTileEntity controllerTE = getControllerTE();
	 if (controllerTE == null)
	 return Vec3d.ZERO;
	 return BeltHelper.getVectorForOffset(controllerTE, transported.beltPosition);
	 }*/

	/**
	 * public static ModelProperty<CasingType> CASING_PROPERTY = new ModelProperty<>();
	 *
	 * @Override public ModelData getModelData() {
	 * return new ModelDataMap.Builder().withInitial(CASING_PROPERTY, casing)
	 * .build();
	 * }
	 */

	@Override
	protected boolean canPropagateDiagonally(Rotating block, BlockState state) {
		return state.contains(BeltBlock.SLOPE)
			&& (state.get(BeltBlock.SLOPE) == BeltSlope.UPWARD || state.get(BeltBlock.SLOPE) == BeltSlope.DOWNWARD);
	}

	/**
	 * private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
	 * BeltTileEntity nextBeltController = getControllerTE();
	 * ItemStack inserted = transportedStack.stack;
	 * ItemStack empty = ItemStack.EMPTY;
	 * <p>
	 * if (nextBeltController == null)
	 * return inserted;
	 * BeltInventory nextInventory = nextBeltController.getInventory();
	 * if (nextInventory == null)
	 * return inserted;
	 * <p>
	 * BlockEntity teAbove = world.getBlockEntity(pos.up());
	 * if (teAbove instanceof BrassTunnelTileEntity) {
	 * BrassTunnelTileEntity tunnelTE = (BrassTunnelTileEntity) teAbove;
	 * if (tunnelTE.hasDistributionBehaviour()) {
	 * if (!tunnelTE.getStackToDistribute()
	 * .isEmpty())
	 * return inserted;
	 * if (!tunnelTE.testFlapFilter(side.getOpposite(), inserted))
	 * return inserted;
	 * if (!simulate) {
	 * BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);
	 * tunnelTE.setStackToDistribute(inserted);
	 * }
	 * return empty;
	 * }
	 * }
	 * <p>
	 * if (getSpeed() == 0)
	 * return inserted;
	 * if (getMovementFacing() == side.getOpposite())
	 * return inserted;
	 * if (!nextInventory.canInsertAtFromSide(index, side))
	 * return inserted;
	 * if (simulate)
	 * return empty;
	 * <p>
	 * transportedStack = transportedStack.copy();
	 * transportedStack.beltPosition = index + .5f - Math.signum(getDirectionAwareBeltMovementSpeed()) / 16f;
	 * <p>
	 * Direction movementFacing = getMovementFacing();
	 * if (!side.getAxis()
	 * .isVertical()) {
	 * if (movementFacing != side) {
	 * transportedStack.sideOffset = side.getDirection()
	 * .getOffset() * .35f;
	 * if (side.getAxis() == Direction.Axis.X)
	 * transportedStack.sideOffset *= -1;
	 * } else
	 * transportedStack.beltPosition = getDirectionAwareBeltMovementSpeed() > 0 ? index : index + 1;
	 * }
	 * <p>
	 * transportedStack.prevSideOffset = transportedStack.sideOffset;
	 * transportedStack.insertedAt = index;
	 * transportedStack.insertedFrom = side;
	 * transportedStack.prevBeltPosition = transportedStack.beltPosition;
	 * <p>
	 * BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);
	 * <p>
	 * nextInventory.addItem(transportedStack);
	 * nextBeltController.markDirty();
	 * nextBeltController.sendData();
	 * return empty;
	 * }
	 */

	@Override
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
									 boolean connectedViaAxes, boolean connectedViaCogs) {
		if (target instanceof BeltBlockEntity && !connectedViaAxes)
			return getController().equals(((BeltBlockEntity) target).getController()) ? 1 : 0;
		return 0;
	}

	@Override
	public void onChunkLightUpdate() {
		super.onChunkLightUpdate();
		updateLight();
	}

	@Override
	public boolean shouldRenderAsBE() {
		return isController();
	}

	private void updateLight() {
		if (world != null) {
			skyLight = (byte) world.getLightLevel(LightType.SKY, pos);
			blockLight = (byte) world.getLightLevel(LightType.BLOCK, pos);
		} else {
			skyLight = -1;
			blockLight = -1;
		}
	}

	public enum CasingType {
		NONE, ANDESITE, BRASS
	}
}
