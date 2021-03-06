package com.simibubi.create.foundation.mixin;

import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.utility.extensions.Matrix4fUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class RenderHooksMixin {

	@Shadow
	private ClientWorld world;

	/**
	 * JUSTIFICATION: This method is called once per layer per frame. It allows us to perform
	 * layer-correct custom rendering. RenderWorldLast is not refined enough for rendering world objects.
	 * This should probably be a forge event.
	 */
	@Inject(at = @At(value = "TAIL"), method = "renderLayer")
	private void renderLayer(RenderLayer type, MatrixStack stack, double camX, double camY, double camZ, CallbackInfo ci) {
		if (!Backend.available()) return;

		Matrix4f viewProjection = stack.peek().getModel().copy();
		Matrix4fUtils.multiplyBackward(viewProjection, FastRenderDispatcher.getProjectionMatrix());

		FastRenderDispatcher.renderLayer(type, viewProjection, camX, camY, camZ);

		ContraptionRenderDispatcher.renderLayer(type, viewProjection, camX, camY, camZ);

		GL20.glUseProgram(0);
	}

	@Inject(at = @At(value = "TAIL"), method = "reload")
	private void refresh(CallbackInfo ci) {
		CreateClient.kineticRenderer.invalidate();
		ContraptionRenderDispatcher.invalidateAll();
        //OptifineHandler.refresh();
        Backend.refresh();

        //if (Backend.canUseInstancing() && world != null) world.loadedTileEntityList.forEach(CreateClient.kineticRenderer::add);*/
	}
}
