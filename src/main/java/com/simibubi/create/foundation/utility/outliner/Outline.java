package com.simibubi.create.foundation.utility.outliner;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.renderState.RenderTypes;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Vec3d;

public abstract class Outline {

	protected OutlineParams params;
	protected Matrix3f transformNormals;

	public Outline() {
		params = new OutlineParams();
	}

	public abstract void render(MatrixStack ms, SuperRenderTypeBuffer buffer);

	public void renderCuboidLine(MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d start, Vec3d end) {
		Vec3d diff = end.subtract(start);
		float hAngle = AngleHelper.deg(MathHelper.atan2(diff.x, diff.z));
		float hDistance = (float) diff.multiply(1, 0, 1)
			.length();
		float vAngle = AngleHelper.deg(MathHelper.atan2(hDistance, diff.y)) - 90;
		ms.push();
		MatrixStacker.of(ms)
			.translate(start)
			.rotateY(hAngle).rotateX(vAngle);
		renderAACuboidLine(ms, buffer, Vec3d.ZERO, new Vec3d(0, 0, diff.length()));
		ms.pop();
	}

	public void renderAACuboidLine(MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d start, Vec3d end) {
		float lineWidth = params.getLineWidth();
		if (lineWidth == 0)
			return;
		
		VertexConsumer builder = buffer.getBuffer(RenderTypes.getOutlineSolid());

		Vec3d diff = end.subtract(start);
		if (diff.x + diff.y + diff.z < 0) {
			Vec3d temp = start;
			start = end;
			end = temp;
			diff = diff.multiply(-1);
		}

		Vec3d extension = diff.normalize()
			.multiply(lineWidth / 2);
		Vec3d plane = VecHelper.axisAlingedPlaneOf(diff);
		Direction face = Direction.getFacing(diff.x, diff.y, diff.z);
		Direction.Axis axis = face.getAxis();

		start = start.subtract(extension);
		end = end.add(extension);
		plane = plane.multiply(lineWidth / 2);

		Vec3d a1 = plane.add(start);
		Vec3d b1 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a2 = plane.add(start);
		Vec3d b2 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a3 = plane.add(start);
		Vec3d b3 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a4 = plane.add(start);
		Vec3d b4 = plane.add(end);

		if (params.disableNormals) {
			face = Direction.UP;
			putQuad(ms, builder, b4, b3, b2, b1, face);
			putQuad(ms, builder, a1, a2, a3, a4, face);
			putQuad(ms, builder, a1, b1, b2, a2, face);
			putQuad(ms, builder, a2, b2, b3, a3, face);
			putQuad(ms, builder, a3, b3, b4, a4, face);
			putQuad(ms, builder, a4, b4, b1, a1, face);
			return;
		}

		putQuad(ms, builder, b4, b3, b2, b1, face);
		putQuad(ms, builder, a1, a2, a3, a4, face.getOpposite());
		Vec3d vec = a1.subtract(a4);
		face = Direction.getFacing(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a1, b1, b2, a2, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getFacing(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a2, b2, b3, a3, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getFacing(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a3, b3, b4, a4, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getFacing(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a4, b4, b1, a1, face);
	}

	public void putQuad(MatrixStack ms, VertexConsumer builder, Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4,
		Direction normal) {
		putQuadUV(ms, builder, v1, v2, v3, v4, 0, 0, 1, 1, normal);
	}

	public void putQuadUV(MatrixStack ms, VertexConsumer builder, Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, float minU,
		float minV, float maxU, float maxV, Direction normal) {
		putVertex(ms, builder, v1, minU, minV, normal);
		putVertex(ms, builder, v2, maxU, minV, normal);
		putVertex(ms, builder, v3, maxU, maxV, normal);
		putVertex(ms, builder, v4, minU, maxV, normal);
	}

	protected void putVertex(MatrixStack ms, VertexConsumer builder, Vec3d pos, float u, float v, Direction normal) {
		int i = 15 << 20 | 15 << 4;
		int j = i >> 16 & '\uffff';
		int k = i & '\uffff';
		MatrixStack.Entry peek = ms.peek();
		Vector3f rgb = params.rgb;
		if (transformNormals == null)
			transformNormals = peek.getNormal();

		int xOffset = 0;
		int yOffset = 0;
		int zOffset = 0;

		if (normal != null) {
			xOffset = normal.getOffsetX();
			yOffset = normal.getOffsetY();
			zOffset = normal.getOffsetZ();
		}

		builder.vertex(peek.getModel(), (float) pos.x, (float) pos.y, (float) pos.z)
			.color(rgb.getX(), rgb.getY(), rgb.getZ(), params.alpha)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(j, k)
			.normal(peek.getNormal(), xOffset, yOffset, zOffset)
			.next(); // TODO could be wrong?

		transformNormals = null;
	}

	public void tick() {}

	public OutlineParams getParams() {
		return params;
	}

	public static class OutlineParams {
		protected Optional<AllSpecialTextures> faceTexture;
		protected Optional<AllSpecialTextures> hightlightedFaceTexture;
		protected Direction highlightedFace;
		protected boolean fadeLineWidth;
		protected boolean disableCull;
		protected boolean disableNormals;
		protected float alpha;
		protected int lightMapU, lightMapV;
		protected Vector3f rgb;
		private float lineWidth;

		public OutlineParams() {
			faceTexture = hightlightedFaceTexture = Optional.empty();
			alpha = 1;
			lineWidth = 1 / 32f;
			fadeLineWidth = true;
			rgb = ColorHelper.getRGB(0xFFFFFF);

			int i = 15 << 20 | 15 << 4;
			lightMapU = i >> 16 & '\uffff';
			lightMapV = i & '\uffff';
		}

		// builder

		public OutlineParams colored(int color) {
			rgb = ColorHelper.getRGB(color);
			return this;
		}

		public OutlineParams lineWidth(float width) {
			this.lineWidth = width;
			return this;
		}

		public OutlineParams withFaceTexture(AllSpecialTextures texture) {
			this.faceTexture = Optional.ofNullable(texture);
			return this;
		}

		public OutlineParams clearTextures() {
			return this.withFaceTextures(null, null);
		}

		public OutlineParams withFaceTextures(AllSpecialTextures texture, AllSpecialTextures highlightTexture) {
			this.faceTexture = Optional.ofNullable(texture);
			this.hightlightedFaceTexture = Optional.ofNullable(highlightTexture);
			return this;
		}

		public OutlineParams highlightFace(@Nullable Direction face) {
			highlightedFace = face;
			return this;
		}

		public OutlineParams disableNormals() {
			disableNormals = true;
			return this;
		}

		public OutlineParams disableCull() {
			disableCull = true;
			return this;
		}

		// getter

		public float getLineWidth() {
			return fadeLineWidth ? alpha * lineWidth : lineWidth;
		}

		public Direction getHighlightedFace() {
			return highlightedFace;
		}

	}

}
