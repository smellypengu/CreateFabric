package com.simibubi.create.content.curiosities.symmetry.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlaneMirror extends SymmetryMirror {

	public static enum Align implements StringIdentifiable {
		XY("xy"), YZ("yz");

		private final String name;

		private Align(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public PlaneMirror(Vec3d pos) {
		super(pos);
		orientation = Align.XY;
	}

	@Override
	protected void setOrientation() {
		if (orientationIndex < 0)
			orientationIndex += Align.values().length;
		if (orientationIndex >= Align.values().length)
			orientationIndex -= Align.values().length;
		orientation = Align.values()[orientationIndex];
	}

	@Override
	public void setOrientation(int index) {
		this.orientation = Align.values()[index];
		orientationIndex = index;
	}

	@Override
	public Map<BlockPos, BlockState> process(BlockPos position, BlockState block) {
		Map<BlockPos, BlockState> result = new HashMap<>();
		switch ((Align) orientation) {

		case XY:
			result.put(flipZ(position), flipZ(block));
			break;
		case YZ:
			result.put(flipX(position), flipX(block));
			break;
		default:
			break;

		}
		return result;
	}

	@Override
	public String typeName() {
		return PLANE;
	}

	@Override
	public AllBlockPartials getModel() {
		return AllBlockPartials.SYMMETRY_PLANE;
	}

	@Override
	public void applyModelTransform(MatrixStack ms) {
		super.applyModelTransform(ms);
		MatrixStacker.of(ms)
			.centre()
			.rotateY(((Align) orientation) == Align.XY ? 0 : 90)
			.unCentre();
	}

	@Override
	public List<Text> getAlignToolTips() {
		return ImmutableList.of(Lang.translate("orientation.alongZ"), Lang.translate("orientation.alongX"));
	}

}
