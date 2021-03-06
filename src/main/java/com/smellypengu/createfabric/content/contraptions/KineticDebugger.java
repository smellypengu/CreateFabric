package com.smellypengu.createfabric.content.contraptions;

import com.smellypengu.createfabric.CreateClient;
import com.smellypengu.createfabric.content.contraptions.base.IRotate;
import com.smellypengu.createfabric.content.contraptions.base.KineticTileEntity;
import com.smellypengu.createfabric.content.contraptions.base.KineticTileEntityRenderer;
import com.smellypengu.createfabric.foundation.utility.ColorHelper;
import com.smellypengu.createfabric.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class KineticDebugger {

	public static void tick() {
		if (!isActive()) {
			if (KineticTileEntityRenderer.rainbowMode) {
				KineticTileEntityRenderer.rainbowMode = false;
				CreateClient.bufferCache.invalidate();
			}
			return;
		}
		
		KineticTileEntity te = getSelectedTE();
		if (te == null)
			return;

		World world = MinecraftClient.getInstance().world;
		BlockPos toOutline = te.hasSource() ? te.source : te.getPos();
		BlockState state = te.getCachedState();
		VoxelShape shape = world.getBlockState(toOutline)
			.getSidesShape(world, toOutline); // TODO EITHER THIS OR .getCullingShape

		/*if (te.getTheoreticalSpeed() != 0 && !shape.isEmpty())
			CreateClient.outliner.chaseAABB("kineticSource", shape.getBoundingBox() TODO OUTLINER
				.offset(toOutline))
				.lineWidth(1 / 16f)
				.colored(te.hasSource() ? ColorHelper.colorFromLong(te.network) : 0xffcc00);*/

		/*if (state.getBlock() instanceof IRotate) {
			Direction.Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
			Vec3d vec = new Vec3d(Direction.get(Direction.AxisDirection.POSITIVE, axis)
				.getVector());
			Vec3d center = VecHelper.getCenterOf(te.getPos());
			CreateClient.outliner.showLine("rotationAxis", center.add(vec), center.subtract(vec)) TODO OUTLINER
				.lineWidth(1 / 16f);
		}*/

	}

	public static boolean isActive() {
		return MinecraftClient.getInstance().options.debugEnabled; //&& AllConfigs.CLIENT.rainbowDebug.get(); TODO CONFIG THING
	}

	public static KineticTileEntity getSelectedTE() {
		HitResult obj = MinecraftClient.getInstance().crosshairTarget;
		ClientWorld world = MinecraftClient.getInstance().world;
		if (obj == null)
			return null;
		if (world == null)
			return null;
		if (!(obj instanceof BlockHitResult))
			return null;

		BlockHitResult ray = (BlockHitResult) obj;
		BlockEntity te = world.getBlockEntity(ray.getBlockPos());
		if (!(te instanceof KineticTileEntity))
			return null;

		return (KineticTileEntity) te;
	}

}
