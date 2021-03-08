package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class SeatMovementBehaviour extends MovementBehaviour {

	@Override
	public void startMoving(MovementContext context) {
		super.startMoving(context);
		int indexOf = context.contraption.getSeats()
			.indexOf(context.localPos);
		context.data.putInt("SeatIndex", indexOf);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);
		
		AbstractContraptionEntity contraptionEntity = context.contraption.entity;
		if (contraptionEntity == null)
			return;
		int index = context.data.getInt("SeatIndex");
		if (index == -1)
			return;

		Map<UUID, Integer> seatMapping = context.contraption.getSeatMapping();
		BlockState blockState = context.world.getBlockState(pos);
		boolean slab = blockState.getBlock() instanceof SlabBlock && blockState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		boolean solid = blockState.isOpaque() || slab;

		// Occupied
		if (seatMapping.containsValue(index)) {
			if (!solid)
				return;
			Entity toDismount = null;
			for (Map.Entry<UUID, Integer> entry : seatMapping.entrySet()) {
				if (entry.getValue() != index)
					continue;
				for (Entity entity : contraptionEntity.getPassengerList()) {
					if (!entry.getKey()
						.equals(entity.getUuid()))
						continue;
					toDismount = entity;
				}
			}
			if (toDismount != null) {
				toDismount.stopRiding();
				Vec3d position = VecHelper.getCenterOf(pos)
					.add(0, slab ? .5f : 1f, 0);
				toDismount.requestTeleport(position.x, position.y, position.z);
				toDismount.getPersistentData()
					.remove("ContraptionDismountLocation");
			}
			return;
		}

		if (solid)
			return;

		List<Entity> nearbyEntities = context.world.getEntitiesByClass(Entity.class,
			new Box(pos).contract(1 / 16f), SeatBlock::canBePickedUp);
		if (!nearbyEntities.isEmpty())
			contraptionEntity.addSittingPassenger(nearbyEntities.get(0), index);
	}

}
