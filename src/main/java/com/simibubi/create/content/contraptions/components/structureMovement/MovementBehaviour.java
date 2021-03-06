package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.content.contraptions.components.structureMovement.render.RenderedContraption;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class MovementBehaviour {

	public boolean isActive(MovementContext context) {
		return true;
	}

	public void tick(MovementContext context) {
	}

	public void startMoving(MovementContext context) {
	}

	public void visitNewPosition(MovementContext context, BlockPos pos) {
	}

	public Vec3d getActiveAreaOffset(MovementContext context) {
		return Vec3d.ZERO;
	}

	public void dropItem(MovementContext context, ItemStack stack) {
		/*ItemStack remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, stack, false);
		if (remainder.isEmpty())
			return;

		Vec3d vec = context.position;
		ItemEntity itemEntity = new ItemEntity(context.world, vec.x, vec.y, vec.z, remainder);
		itemEntity.setVelocity(context.motion.add(0, 0.5f, 0)
			.multiply(context.world.random.nextFloat() * .3f));
		context.world.spawnEntity(itemEntity);*/
 	}


	public void stopMoving(MovementContext context) {

	}

	public void writeExtraData(MovementContext context) {

	}

	public boolean renderAsNormalBlockEntity() {
		return false;
	}

	public boolean hasSpecialInstancedRendering() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
									VertexConsumerProvider buffer) {
	}

	@Environment(EnvType.CLIENT)
	public void addInstance(RenderedContraption contraption, MovementContext context) {
	}

	public void onSpeedChanged(MovementContext context, Vec3d oldMotion, Vec3d motion) {

	}
}
