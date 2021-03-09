package com.smellypengu.createfabric.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.smellypengu.createfabric.CreateClient;
import com.smellypengu.createfabric.content.contraptions.KineticDebugger;
import com.smellypengu.createfabric.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.smellypengu.createfabric.content.contraptions.relays.belt.item.BeltConnectorHandler;
import com.smellypengu.createfabric.foundation.render.backend.FastRenderDispatcher;
import com.smellypengu.createfabric.foundation.render.backend.RenderWork;
import com.smellypengu.createfabric.foundation.renderState.SuperRenderTypeBuffer;
import com.smellypengu.createfabric.foundation.tileEntity.behaviour.scrollvalue.ScrollValueRenderer;
import com.smellypengu.createfabric.foundation.utility.AnimationTickHolder;
import com.smellypengu.createfabric.foundation.utility.placement.PlacementHelpers;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ClientEvents {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onTick);
        WorldRenderEvents.END.register(ClientEvents::onRenderWorld);
    }

    public static void onTick(MinecraftClient client) {
    	if (!isGameActive())
			return;

        AnimationTickHolder.tick();
        FastRenderDispatcher.tick();

        BeltConnectorHandler.tick();
        ScrollValueRenderer.tick();

        KineticDebugger.tick();
        PlacementHelpers.tick();
        CreateClient.outliner.tickOutlines();
        CreateClient.ghostBlocks.tickGhosts();
        ContraptionRenderDispatcher.tick();
    }

    public static void onLoadWorld(World world) {
        if (world.isClient && world instanceof ClientWorld) {
            CreateClient.invalidateRenderers();
            AnimationTickHolder.reset();
            //((ClientWorld) world).blockentites.forEach(CreateClient.kineticRenderer::add);
        }
    }

    public static void onUnloadWorld(World world) {
        if (world.isClient) {
            CreateClient.invalidateRenderers();
            AnimationTickHolder.reset();
        }
    }

    public static void onRenderWorld(WorldRenderContext context) {
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        MatrixStack ms = context.matrixStack();
        ms.push();
        ms.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
        SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();

        CreateClient.ghostBlocks.renderAll(ms, buffer);

        CreateClient.outliner.renderOutlines(ms, buffer);
        buffer.draw();
        RenderSystem.enableCull();

        ms.pop();

        RenderWork.runAll();
        FastRenderDispatcher.endFrame();
    }

    protected static boolean isGameActive() {
		return !(MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null);
	}

}