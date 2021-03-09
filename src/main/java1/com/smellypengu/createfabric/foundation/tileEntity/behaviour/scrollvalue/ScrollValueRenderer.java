package com.smellypengu.createfabric.foundation.tileEntity.behaviour.scrollvalue;

import com.smellypengu.createfabric.AllKeys;
import com.smellypengu.createfabric.CreateClient;
import com.smellypengu.createfabric.foundation.tileEntity.SmartTileEntity;
import com.smellypengu.createfabric.foundation.tileEntity.TileEntityBehaviour;
import com.smellypengu.createfabric.foundation.tileEntity.behaviour.ValueBox;
import com.smellypengu.createfabric.foundation.utility.Lang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ScrollValueRenderer {

	public static void tick() {
		MinecraftClient mc = MinecraftClient.getInstance();
		HitResult target = mc.crosshairTarget;
		if (target == null || !(target instanceof BlockHitResult))
			return;

		BlockHitResult result = (BlockHitResult) target;
		ClientWorld world = mc.world;
		BlockPos pos = result.getBlockPos();
		Direction face = result.getSide();

		ScrollValueBehaviour behaviour = TileEntityBehaviour.get(world, pos, ScrollValueBehaviour.TYPE);
		if (behaviour == null)
			return;
		/**if (behaviour.needsWrench && !AllItems.WRENCH.isIn(MinecraftClient.getInstance().player.getHeldItemMainhand())) TODO WRENCH CHECK
			return;*/
		boolean highlight = behaviour.testHit(target.getPos());

		if (behaviour instanceof BulkScrollValueBehaviour && AllKeys.ctrlDown()) {
			BulkScrollValueBehaviour bulkScrolling = (BulkScrollValueBehaviour) behaviour;
			for (SmartTileEntity smartTileEntity : bulkScrolling.getBulk()) {
				ScrollValueBehaviour other = smartTileEntity.getBehaviour(ScrollValueBehaviour.TYPE);
				if (other != null)
					addBox(world, smartTileEntity.getPos(), face, other, highlight);
			}
		} else
			addBox(world, pos, face, behaviour, highlight);
	}

	protected static void addBox(ClientWorld world, BlockPos pos, Direction face, ScrollValueBehaviour behaviour,
		boolean highlight) {
		Box bb = new Box(Vec3d.ZERO, Vec3d.ZERO).expand(.5f)
			.shrink(0, 0, -.5f)
			.offset(0, 0, -.125f);
		String label = behaviour.label;
		ValueBox box;

		if (behaviour instanceof ScrollOptionBehaviour) {
			box = new ValueBox.IconValueBox(label, ((ScrollOptionBehaviour<?>) behaviour).getIconForSelected(), bb, pos);
		} else {
			box = new ValueBox.TextValueBox(label, bb, pos, behaviour.formatValue());
			if (behaviour.unit != null)
				box.subLabel("(" + behaviour.unit.apply(behaviour.scrollableValue) + ")");
		}

		box.scrollTooltip("[" + Lang.translate("action.scroll") + "]");
		box.offsetLabel(behaviour.textShift.add(20, -10, 0))
			.withColors(0x5A5D5A, 0xB5B7B6)
			.passive(!highlight);

		CreateClient.outliner.showValueBox(pos, box.transform(behaviour.slotPositioning))
			.lineWidth(1 / 64f)
			.highlightFace(face);
	}

}