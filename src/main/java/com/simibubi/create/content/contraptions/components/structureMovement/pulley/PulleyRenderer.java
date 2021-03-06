package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class PulleyRenderer extends AbstractPulleyRenderer {

	public PulleyRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher, AllBlockPartials.ROPE_HALF, AllBlockPartials.ROPE_HALF_MAGNET);
	}

	@Override
	protected Axis getShaftAxis(KineticBlockEntity te) {
		return te.getCachedState()
			.get(PulleyBlock.HORIZONTAL_AXIS);
	}

	@Override
	protected AllBlockPartials getCoil() {
		return AllBlockPartials.ROPE_COIL;
	}

	@Override
	protected SuperByteBuffer renderRope(KineticBlockEntity te) {
		return CreateClient.bufferCache.renderBlock(AllBlocks.ROPE.getDefaultState());
	}

	@Override
	protected SuperByteBuffer renderMagnet(KineticBlockEntity te) {
		return CreateClient.bufferCache.renderBlock(AllBlocks.PULLEY_MAGNET.getDefaultState());
	}

	@Override
	protected float getOffset(KineticBlockEntity te, float partialTicks) {
		PulleyBlockEntity pulley = (PulleyBlockEntity) te;
		boolean running = pulley.running;
		boolean moving = running && (pulley.movedContraption == null || !pulley.movedContraption.isStalled());
		float offset = pulley.getInterpolatedOffset(moving ? partialTicks : 0.5f);

		if (pulley.movedContraption != null) {
			AbstractContraptionEntity e = pulley.movedContraption;
			PulleyContraption c = (PulleyContraption) pulley.movedContraption.getContraption();
			double entityPos = MathHelper.lerp(partialTicks, e.lastRenderY, e.getY());
			offset = (float) -(entityPos - c.anchor.getY() - c.initialOffset);
		}

		return offset;
	}

	@Override
	protected boolean isRunning(KineticBlockEntity te) {
		return ((PulleyBlockEntity) te).running;
	}

}
