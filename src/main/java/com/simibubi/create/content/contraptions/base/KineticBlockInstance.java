package com.simibubi.create.content.contraptions.base;

import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.render.backend.instancing.BlockEntityInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedBlockRenderer;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;

public abstract class KineticBlockInstance<T extends KineticBlockEntity> extends BlockEntityInstance<T> {

	public KineticBlockInstance(InstancedBlockRenderer<?> modelManager, T tile) {
		super(modelManager, tile);
	}

	public static BlockState shaft(Direction.Axis axis) {
		return AllBlocks.SHAFT.getDefaultState()
			.with(ShaftBlock.AXIS, axis);
	}

	protected final void updateRotation(InstanceKey<RotatingData> key, Direction.Axis axis) {
		key.modifyInstance(data -> {
			data.setColor(tile.network)
				.setRotationalSpeed(tile.getSpeed())
				.setRotationOffset(getRotationOffset(axis))
				.setRotationAxis(axis);
		});
	}

	protected final Consumer<RotatingData> setupFunc(float speed, Direction.Axis axis) {
		return data -> {
			data.setBlockLight(world.getLightLevel(LightType.BLOCK, pos))
				.setSkyLight(world.getLightLevel(LightType.SKY, pos))
				.setBlockEntity(tile)
				.setRotationalSpeed(speed)
				.setRotationOffset(getRotationOffset(axis))
				.setRotationAxis(axis);
		};
	}

	protected final void relight(KineticData<?> data) {
		data.setBlockLight(world.getLightLevel(LightType.BLOCK, pos))
			.setSkyLight(world.getLightLevel(LightType.SKY, pos));
	}

	protected float getRotationOffset(final Direction.Axis axis) {
		float offset = CogWheelBlock.isLargeCog(lastState) ? 11.25f : 0;
		double d = (((axis == Direction.Axis.X) ? 0 : pos.getX()) + ((axis == Direction.Axis.Y) ? 0 : pos.getY())
			+ ((axis == Direction.Axis.Z) ? 0 : pos.getZ())) % 2;
		if (d == 0) {
			offset = 22.5f;
		}
		return offset;
	}

	public Direction.Axis getRotationAxis() {
		return ((Rotating) lastState.getBlock()).getRotationAxis(lastState);
	}

	protected final RenderMaterial<?, InstancedModel<RotatingData>> rotatingMaterial() {
		return modelManager.getMaterial(KineticRenderMaterials.ROTATING);
	}
}
