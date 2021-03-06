package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class DeforesterItemRenderer extends CustomRenderedItemModelRenderer<DeforesterModel> {

	@Override
	public void render(ItemStack stack, DeforesterModel model, PartialItemModelRenderer renderer,
		MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
		int maxLight = 0xF000F0;
		float worldTime = AnimationTickHolder.getRenderTick();
		
		//renderer.renderSolid(model.getBakedModel(), light);
		renderer.renderSolidGlowing(model.getPartial("core"), maxLight);
		renderer.renderGlowing(model.getPartial("core_glow"), maxLight);
		
		float angle = worldTime * -.5f % 360;
		//ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle));
		renderer.renderSolid(model.getPartial("gear"), light);
	}
	

}
