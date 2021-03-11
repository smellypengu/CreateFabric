package com.simibubi.create.foundation.utility.outliner;

import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.block.entity.behaviour.ValueBox;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class Outliner {

	final Map<Object, OutlineEntry> outlines;

	public Map<Object, OutlineEntry> getOutlines() {
		return Collections.unmodifiableMap(outlines);
	}

	// Facade

	public Outline.OutlineParams showValueBox(Object slot, ValueBox box) {
		outlines.put(slot, new OutlineEntry(box));
		return box.getParams();
	}

	public Outline.OutlineParams showLine(Object slot, Vec3d start, Vec3d end) {
		if (!outlines.containsKey(slot)) {
			LineOutline outline = new LineOutline();
			outlines.put(slot, new OutlineEntry(outline));
		}
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		((LineOutline) entry.outline).set(start, end);
		return entry.outline.getParams();
	}

	public Outline.OutlineParams endChasingLine(Object slot, Vec3d start, Vec3d end, float chasingProgress) {
		if (!outlines.containsKey(slot)) {
			LineOutline.EndChasingLineOutline outline = new LineOutline.EndChasingLineOutline();
			outlines.put(slot, new OutlineEntry(outline));
		}
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		((LineOutline.EndChasingLineOutline) entry.outline).setProgress(chasingProgress)
			.set(start, end);
		return entry.outline.getParams();
	}

	public Outline.OutlineParams showAABB(Object slot, Box bb, int ttl) {
		createAABBOutlineIfMissing(slot, bb);
		ChasingAABBOutline outline = getAndRefreshAABB(slot, ttl);
		outline.prevBB = outline.targetBB = bb;
		return outline.getParams();
	}

	public Outline.OutlineParams showAABB(Object slot, Box bb) {
		createAABBOutlineIfMissing(slot, bb);
		ChasingAABBOutline outline = getAndRefreshAABB(slot);
		outline.prevBB = outline.targetBB = bb;
		return outline.getParams();
	}

	public Outline.OutlineParams chaseAABB(Object slot, Box bb) {
		createAABBOutlineIfMissing(slot, bb);
		ChasingAABBOutline outline = getAndRefreshAABB(slot);
		outline.targetBB = bb;
		return outline.getParams();
	}

	public Outline.OutlineParams showCluster(Object slot, Iterable<BlockPos> selection) {
		BlockClusterOutline outline = new BlockClusterOutline(selection);
		OutlineEntry entry = new OutlineEntry(outline);
		outlines.put(slot, entry);
		return entry.getOutline()
			.getParams();
	}

	public void keep(Object slot) {
		if (outlines.containsKey(slot))
			outlines.get(slot).ticksTillRemoval = 1;
	}

	public void remove(Object slot) {
		outlines.remove(slot);
	}

	public Optional<Outline.OutlineParams> edit(Object slot) {
		keep(slot);
		if (outlines.containsKey(slot))
			return Optional.of(outlines.get(slot)
				.getOutline()
				.getParams());
		return Optional.empty();
	}

	// Utility

	private void createAABBOutlineIfMissing(Object slot, Box bb) {
		if (!outlines.containsKey(slot)) {
			ChasingAABBOutline outline = new ChasingAABBOutline(bb);
			outlines.put(slot, new OutlineEntry(outline));
		}
	}

	private ChasingAABBOutline getAndRefreshAABB(Object slot) {
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		return (ChasingAABBOutline) entry.getOutline();
	}

	private ChasingAABBOutline getAndRefreshAABB(Object slot, int ttl) {
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = ttl;
		return (ChasingAABBOutline) entry.getOutline();
	}

	// Maintenance

	public Outliner() {
		outlines = Collections.synchronizedMap(new HashMap<>());
	}

	public void tickOutlines() {
		Set<Object> toClear = new HashSet<>();

		outlines.forEach((key, entry) -> {
			entry.ticksTillRemoval--;
			entry.getOutline()
				.tick();
			if (entry.isAlive())
				return;
			toClear.add(key);
		});

		toClear.forEach(outlines::remove);
	}

	public void renderOutlines(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		outlines.forEach((key, entry) -> {
			Outline outline = entry.getOutline();
			outline.params.alpha = 1;
			if (entry.ticksTillRemoval < 0) {

				int prevTicks = entry.ticksTillRemoval + 1;
				float fadeticks = OutlineEntry.fadeTicks;
				float lastAlpha = prevTicks >= 0 ? 1 : 1 + (prevTicks / fadeticks);
				float currentAlpha = 1 + (entry.ticksTillRemoval / fadeticks);
				float alpha = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), lastAlpha, currentAlpha);

				outline.params.alpha = alpha * alpha * alpha;
				if (outline.params.alpha < 1 / 8f)
					return;
			}
			outline.render(ms, buffer);
		});
	}

	public static class OutlineEntry {

		static final int fadeTicks = 8;
		private Outline outline;
		private int ticksTillRemoval;

		public OutlineEntry(Outline outline) {
			this.outline = outline;
			ticksTillRemoval = 1;
		}

		public boolean isAlive() {
			return ticksTillRemoval >= -fadeTicks;
		}

		public Outline getOutline() {
			return outline;
		}

	}

}