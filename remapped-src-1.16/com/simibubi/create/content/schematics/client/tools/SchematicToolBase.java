package com.simibubi.create.content.schematics.client.tools;

import java.util.Arrays;
import java.util.List;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.content.schematics.client.SchematicTransformation;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public abstract class SchematicToolBase implements ISchematicTool {

	protected SchematicHandler schematicHandler;

	protected BlockPos selectedPos;
	protected Vec3d chasingSelectedPos;
	protected Vec3d lastChasingSelectedPos;

	protected boolean selectIgnoreBlocks;
	protected int selectionRange;
	protected boolean schematicSelected;
	protected boolean renderSelectedFace;
	protected Direction selectedFace;

	protected final List<String> mirrors = Arrays.asList("none", "leftRight", "frontBack");
	protected final List<String> rotations = Arrays.asList("none", "cw90", "cw180", "cw270");

	@Override
	public void init() {
		schematicHandler = CreateClient.schematicHandler;
		selectedPos = null;
		selectedFace = null;
		schematicSelected = false;
		chasingSelectedPos = Vec3d.ZERO;
		lastChasingSelectedPos = Vec3d.ZERO;
	}

	@Override
	public void updateSelection() {
		updateTargetPos();

		if (selectedPos == null)
			return;
		lastChasingSelectedPos = chasingSelectedPos;
		Vec3d target = Vec3d.of(selectedPos);
		if (target.distanceTo(chasingSelectedPos) < 1 / 512f) {
			chasingSelectedPos = target;
			return;
		}

		chasingSelectedPos = chasingSelectedPos.add(target.subtract(chasingSelectedPos)
			.multiply(1 / 2f));
	}

	public void updateTargetPos() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		// Select Blueprint
		if (schematicHandler.isDeployed()) {
			SchematicTransformation transformation = schematicHandler.getTransformation();
			Box localBounds = schematicHandler.getBounds();

			Vec3d traceOrigin = RaycastHelper.getTraceOrigin(player);
			Vec3d start = transformation.toLocalSpace(traceOrigin);
			Vec3d end = transformation.toLocalSpace(RaycastHelper.getTraceTarget(player, 70, traceOrigin));
			PredicateTraceResult result =
				RaycastHelper.rayTraceUntil(start, end, pos -> localBounds.contains(VecHelper.getCenterOf(pos)));

			schematicSelected = !result.missed();
			selectedFace = schematicSelected ? result.getFacing() : null;
		}

		boolean snap = this.selectedPos == null;

		// Select location at distance
		if (selectIgnoreBlocks) {
			float pt = AnimationTickHolder.getPartialTicks();
			selectedPos = new BlockPos(player.getCameraPosVec(pt)
				.add(player.getRotationVector()
					.multiply(selectionRange)));
			if (snap)
				lastChasingSelectedPos = chasingSelectedPos = Vec3d.of(selectedPos);
			return;
		}

		// Select targeted Block
		selectedPos = null;
		BlockHitResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
		if (trace == null || trace.getType() != Type.BLOCK)
			return;

		BlockPos hit = new BlockPos(trace.getPos());
		boolean replaceable = player.world.getBlockState(hit)
			.getMaterial()
			.isReplaceable();
		if (trace.getSide()
			.getAxis()
			.isVertical() && !replaceable)
			hit = hit.offset(trace.getSide());
		selectedPos = hit;
		if (snap)
			lastChasingSelectedPos = chasingSelectedPos = Vec3d.of(selectedPos);
	}

	@Override
	public void renderTool(MatrixStack ms, SuperRenderTypeBuffer buffer) {}

	@Override
	public void renderOverlay(MatrixStack ms, VertexConsumerProvider buffer) {}

	@Override
	public void renderOnSchematic(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		if (!schematicHandler.isDeployed())
			return;

		ms.push();
		AABBOutline outline = schematicHandler.getOutline();
		if (renderSelectedFace) {
			outline.getParams()
				.highlightFace(selectedFace)
				.withFaceTextures(AllSpecialTextures.CHECKERED,
					AllKeys.ctrlDown() ? AllSpecialTextures.HIGHLIGHT_CHECKERED : AllSpecialTextures.CHECKERED);
		}
		outline.getParams()
			.colored(0x6886c5)
			.withFaceTexture(AllSpecialTextures.CHECKERED)
			.lineWidth(1 / 16f);
		outline.render(ms, buffer);
		outline.getParams()
			.clearTextures();
		ms.pop();
	}

}
