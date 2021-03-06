package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.block.entity.render.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BeltRenderer extends SafeBlockEntityRenderer<BeltBlockEntity> {

	public BeltRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	public static SpriteShiftEntry getSpriteShiftEntry(DyeColor color, boolean diagonal, boolean bottom) {
		if (color != null) {
			return (diagonal ? AllSpriteShifts.DYED_DIAGONAL_BELTS
				: bottom ? AllSpriteShifts.DYED_OFFSET_BELTS : AllSpriteShifts.DYED_BELTS).get(color);
		} else
			return diagonal ? AllSpriteShifts.BELT_DIAGONAL
				: bottom ? AllSpriteShifts.BELT_OFFSET : AllSpriteShifts.BELT;
	}

	public static AllBlockPartials getBeltPartial(boolean diagonal, boolean start, boolean end, boolean bottom) {
		if (diagonal) {
			if (start) return AllBlockPartials.BELT_DIAGONAL_START;
			if (end) return AllBlockPartials.BELT_DIAGONAL_END;
			return AllBlockPartials.BELT_DIAGONAL_MIDDLE;
		} else if (bottom) {
			if (start) return AllBlockPartials.BELT_START_BOTTOM;
			if (end) return AllBlockPartials.BELT_END_BOTTOM;
			return AllBlockPartials.BELT_MIDDLE_BOTTOM;
		} else {
			if (start) return AllBlockPartials.BELT_START;
			if (end) return AllBlockPartials.BELT_END;
			return AllBlockPartials.BELT_MIDDLE;
		}
	}

	@Override
	public boolean rendersOutsideBoundingBox(BeltBlockEntity te) {
		return BeltBlock.canTransportObjects(te.getCachedState());
	}

	@Override
	protected void renderSafe(BeltBlockEntity te, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer,
							  int light, int overlay) {

		if (!FastRenderDispatcher.available(te.getWorld())) {

			BlockState blockState = te.getCachedState();
			if (!AllBlocks.BELT.hasBlockEntity()) return;

			BeltSlope beltSlope = blockState.get(BeltBlock.SLOPE);
			BeltPart part = blockState.get(BeltBlock.PART);
			Direction facing = blockState.get(BeltBlock.HORIZONTAL_FACING);
			Direction.AxisDirection axisDirection = facing.getDirection();

			boolean downward = beltSlope == BeltSlope.DOWNWARD;
			boolean upward = beltSlope == BeltSlope.UPWARD;
			boolean diagonal = downward || upward;
			boolean start = part == BeltPart.START;
			boolean end = part == BeltPart.END;
			boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
			boolean alongX = facing.getAxis() == Direction.Axis.X;

			MatrixStacker msr = MatrixStacker.of(ms);
			VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());
			float renderTick = AnimationTickHolder.getRenderTick();

			ms.push();
			msr.centre();
			msr.rotateY(AngleHelper.horizontalAngle(facing) + (upward ? 180 : 0) + (sideways ? 270 : 0));
			msr.rotateZ(sideways ? 90 : 0);
			msr.rotateX(!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0);
			msr.unCentre();

			if (downward || beltSlope == BeltSlope.VERTICAL && axisDirection == Direction.AxisDirection.POSITIVE) {
				boolean b = start;
				start = end;
				end = b;
			}

			DyeColor color = te.color.orElse(null);

			for (boolean bottom : Iterate.trueAndFalse) {

				AllBlockPartials beltPartial = getBeltPartial(diagonal, start, end, bottom);

				SuperByteBuffer beltBuffer = beltPartial.renderOn(blockState)
					.light(light);

				SpriteShiftEntry spriteShift = getSpriteShiftEntry(color, diagonal, bottom);

				// UV shift
				float speed = te.getSpeed();
				if (speed != 0 || te.color.isPresent()) {
					float time = renderTick * axisDirection.offset();
					if (diagonal && (downward ^ alongX) || !sideways && !diagonal && alongX || sideways && axisDirection == Direction.AxisDirection.NEGATIVE)
						speed = -speed;

					float scrollMult = diagonal ? 3f / 8f : 0.5f;

					float spriteSize = spriteShift.getTarget().getMaxV() - spriteShift.getTarget().getMinV();

					double scroll = speed * time / (36 * 16) + (bottom ? 0.5 : 0.0);
					scroll = scroll - Math.floor(scroll);
					scroll = scroll * spriteSize * scrollMult;

					beltBuffer.shiftUVScrolling(spriteShift, (float) scroll);
				}

				beltBuffer.renderInto(ms, vb);

				// Diagonal belt do not have a separate bottom model
				if (diagonal) break;
			}
			ms.pop();

			if (te.hasPulley()) {
		 		// TODO 1.15 find a way to cache this model matrix computation
		 		MatrixStack modelTransform = new MatrixStack();
		 		Direction dir = blockState.get(BeltBlock.HORIZONTAL_FACING).rotateYClockwise();
		 		if (sideways) dir = Direction.UP;
				msr = MatrixStacker.of(modelTransform);
		 		msr.centre();
		 		if (dir.getAxis() == Direction.Axis.X) msr.rotateY(90);
		 		if (dir.getAxis() == Direction.Axis.Y) msr.rotateX(90);
		 		msr.rotateX(90);
		 		msr.unCentre();

		 		SuperByteBuffer superBuffer = CreateClient.bufferCache.renderDirectionalPartial(AllBlockPartials.BELT_PULLEY, blockState, dir, modelTransform);
		 		KineticBlockEntityRenderer.standardKineticRotationTransform(superBuffer, te, light).renderInto(ms, vb);
			 }
		}

		renderItems(te, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderItems(BeltBlockEntity te, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer,
							   int light, int overlay) {
		if (!te.isController())
			return;
		if (te.beltLength == 0)
			return;

		ms.push();

		Direction beltFacing = te.getBeltFacing();
		Vec3i directionVec = beltFacing
			.getVector();
		Vec3d beltStartOffset = new Vec3d(directionVec.getX(), directionVec.getY(), directionVec.getZ()).multiply(-.5)
			.add(.5, 13 / 16f + .125f, .5);
		ms.translate(beltStartOffset.x, beltStartOffset.y, beltStartOffset.z);
		BeltSlope slope = te.getCachedState()
			.get(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
		boolean slopeAlongX = beltFacing
			.getAxis() == Direction.Axis.X;

		/**for (TransportedItemStack transported : te.getInventory()
		 .getTransportedItems()) {
		 ms.push();
		 MatrixStacker.of(ms)
		 .nudge(transported.angle);
		 float offset = MathHelper.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);
		 float sideOffset = MathHelper.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);
		 float verticalMovement = verticality;

		 if (te.getSpeed() == 0) {
		 offset = transported.beltPosition;
		 sideOffset = transported.sideOffset;
		 }

		 int stackLight = getPackedLight(te, offset);

		 if (offset < .5)
		 verticalMovement = 0;
		 verticalMovement = verticalMovement * (Math.min(offset, te.beltLength - .5f) - .5f);
		 Vec3d offsetVec = new Vec3d(directionVec).scale(offset)
		 .add(0, verticalMovement, 0);
		 boolean onSlope =
		 slope != BeltSlope.HORIZONTAL && MathHelper.clamp(offset, .5f, te.beltLength - .5f) == offset;
		 boolean tiltForward = (slope == BeltSlope.DOWNWARD ^ beltFacing
		 .getDirection() == Direction.AxisDirection.POSITIVE) == (beltFacing
		 .getAxis() == Direction.Axis.Z);
		 float slopeAngle = onSlope ? tiltForward ? -45 : 45 : 0;

		 ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);

		 boolean alongX = beltFacing
		 .rotateYClockwise()
		 .getAxis() == Direction.Axis.X;
		 if (!alongX)
		 sideOffset *= -1;
		 ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

		 ItemRenderer itemRenderer = MinecraftClient.getInstance()
		 .getItemRenderer();
		 boolean renderUpright = BeltHelper.isItemUpright(transported.stack);
		 boolean blockItem = itemRenderer.getHeldItemModel(transported.stack, te.getWorld(), null, 0)
		 .hasDepth();
		 int count = (int) (MathHelper.log2((int) (transported.stack.getCount()))) / 2;
		 Random r = new Random(transported.angle);

		 if (MinecraftClient.getInstance().options.graphicsMode == GraphicsMode.FANCY) {
		 Vec3d shadowPos = new Vec3d(te.getPos()).add(beltStartOffset.multiply(1) TODO ShadowRendering
		 .add(offsetVec)
		 .add(alongX ? sideOffset : 0, .39, alongX ? 0 : sideOffset));
		 ShadowRenderHelper.renderShadow(ms, buffer, shadowPos, .75f, blockItem ? .2f : .2f);
		 }

		 if (renderUpright) {
		 Entity renderViewEntity = MinecraftClient.getInstance().renderViewEntity;
		 if (renderViewEntity != null) {
		 Vec3d positionVec = renderViewEntity.getPos();
		 Vec3d vectorForOffset = BeltHelper.getVectorForOffset(te, offset);
		 Vec3d diff = vectorForOffset.subtract(positionVec);
		 float yRot = (float) MathHelper.atan2(diff.z, -diff.x);
		 ms.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((float) (yRot + Math.PI / 2)));
		 }
		 ms.translate(0, 3 / 32d, 1 / 16f);
		 }
		 if (!renderUpright)
		 ms.multiply(new Vec3f(slopeAlongX ? 0 : 1, 0, slopeAlongX ? 1 : 0).getDegreesQuaternion(slopeAngle));

		 if (onSlope)
		 ms.translate(0, 1 / 8f, 0);

		 for (int i = 0; i <= count; i++) {
		 ms.push();

		 ms.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(transported.angle));
		 if (!blockItem && !renderUpright) {
		 ms.translate(0, -.09375, 0);
		 ms.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
		 }

		 if (blockItem) {
		 ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
		 }

		 ms.scale(.5f, .5f, .5f);
		 itemRenderer.renderItem(transported.stack, ModelTransformation.Mode.FIXED, stackLight, overlay, ms, buffer);
		 ms.pop();

		 if (!renderUpright) {
		 if (!blockItem)
		 ms.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(10));
		 ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
		 } else
		 ms.translate(0, 0, -1 / 16f);

		 }

		 ms.pop();
		 }*/
		ms.pop();
	}

	protected int getPackedLight(BeltBlockEntity controller, float beltPos) {
		BeltBlockEntity belt = BeltHelper.getBeltForOffset(controller, beltPos);

		if (belt == null) return 0;

		return (belt.skyLight << 20) | (belt.blockLight << 4);
	}
}
