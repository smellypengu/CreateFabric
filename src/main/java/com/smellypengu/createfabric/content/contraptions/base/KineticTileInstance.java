package com.smellypengu.createfabric.content.contraptions.base;

import com.smellypengu.createfabric.AllBlocks;
import com.smellypengu.createfabric.content.contraptions.relays.elementary.ShaftBlock;
import com.smellypengu.createfabric.foundation.render.backend.instancing.*;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;

import java.util.function.Consumer;

public abstract class KineticTileInstance<T extends KineticBlockEntity> extends TileEntityInstance<T> {

    public KineticTileInstance(InstancedTileRenderer<?> modelManager, T tile) {
        super(modelManager, tile);
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
                .setTileEntity(tile)
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
        float offset = /*CogWheelBlock.isLargeCog(lastState) ? 11.25f :*/ 0; // TODO CogWheel CHECK
        double d = (((axis == Direction.Axis.X) ? 0 : pos.getX()) + ((axis == Direction.Axis.Y) ? 0 : pos.getY())
                + ((axis == Direction.Axis.Z) ? 0 : pos.getZ())) % 2;
        if (d == 0) {
            offset = 22.5f;
        }
        return offset;
    }

    public static BlockState shaft(Direction.Axis axis) {
        return AllBlocks.SHAFT.getDefaultState()
                              .with(ShaftBlock.AXIS, axis);
    }

    public Direction.Axis getRotationAxis() {
        return ((Rotating) lastState.getBlock()).getRotationAxis(lastState);
    }

    protected final RenderMaterial<?, InstancedModel<RotatingData>> rotatingMaterial() {
        return modelManager.getMaterial(KineticRenderMaterials.ROTATING);
    }
}
