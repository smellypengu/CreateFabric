package com.smellypengu.createfabric.foundation.render.backend.instancing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.smellypengu.createfabric.AllBlockPartials;
import com.smellypengu.createfabric.content.contraptions.base.KineticTileEntityRenderer;
import com.smellypengu.createfabric.foundation.render.Compartment;
import com.smellypengu.createfabric.foundation.render.SuperByteBufferCache;
import com.smellypengu.createfabric.foundation.render.backend.Backend;
import com.smellypengu.createfabric.foundation.render.backend.FastRenderDispatcher;
import com.smellypengu.createfabric.foundation.render.backend.gl.BasicProgram;
import com.smellypengu.createfabric.foundation.render.backend.gl.shader.ProgramSpec;
import com.smellypengu.createfabric.foundation.render.backend.gl.shader.ShaderCallback;
import net.minecraft.block.BlockRenderType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.block.BlockState;

public class RenderMaterial<P extends BasicProgram, MODEL extends InstancedModel<?>> {

    protected final InstancedTileRenderer<?> renderer;
    protected final Map<Compartment<?>, Cache<Object, MODEL>> models;
    protected final ModelFactory<MODEL> factory;
    protected final ProgramSpec<P> programSpec;
    protected final Predicate<RenderLayer> layerPredicate;

    /**
     * Creates a material that renders in the default layer (CUTOUT_MIPPED)
     */
    public RenderMaterial(InstancedTileRenderer<?> renderer, ProgramSpec<P> programSpec, ModelFactory<MODEL> factory) {
        this(renderer, programSpec, factory, type -> type == RenderLayer.getCutoutMipped());
    }

    public RenderMaterial(InstancedTileRenderer<?> renderer, ProgramSpec<P> programSpec, ModelFactory<MODEL> factory, Predicate<RenderLayer> layerPredicate) {
        this.renderer = renderer;
        this.models = new HashMap<>();
        this.factory = factory;
        this.programSpec = programSpec;
        this.layerPredicate = layerPredicate;
        registerCompartment(Compartment.PARTIAL);
        registerCompartment(Compartment.DIRECTIONAL_PARTIAL);
        registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
    }

    public boolean canRenderInLayer(RenderLayer layer) {
        return layerPredicate.test(layer);
    }

    public void render(RenderLayer layer, Matrix4f projection, double camX, double camY, double camZ) {
        render(layer, projection, camX, camY, camZ, null);
    }

    public void render(RenderLayer layer, Matrix4f viewProjection, double camX, double camY, double camZ, ShaderCallback<P> setup) {
        P program = Backend.getProgram(programSpec);
        program.bind(viewProjection, camX, camY, camZ, FastRenderDispatcher.getDebugMode());

        if (setup != null) setup.call(program);

        makeRenderCalls();
        teardown();
    }

    public void teardown() {}

    public void delete() {
        runOnAll(InstancedModel::delete);
        models.values().forEach(Cache::invalidateAll);
    }

    protected void makeRenderCalls() {
        for (Cache<Object, MODEL> cache : models.values()) {
            for (MODEL model : cache.asMap().values()) {
                if (!model.isEmpty()) {
                    model.render();
                }
            }
        }
    }

    public void runOnAll(Consumer<MODEL> f) {
        for (Cache<Object, MODEL> cache : models.values()) {
            for (MODEL model : cache.asMap().values()) {
                f.accept(model);
            }
        }
    }

    public void registerCompartment(Compartment<?> instance) {
        models.put(instance, CacheBuilder.newBuilder().build());
    }

    public MODEL getModel(AllBlockPartials partial, BlockState referenceState) {
        return get(Compartment.PARTIAL, partial, () -> buildModel(partial.get(), referenceState));
    }

    public MODEL getModel(AllBlockPartials partial, BlockState referenceState, Direction dir) {
        return get(Compartment.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
                   () -> buildModel(partial.get(), referenceState));
    }

    public MODEL getModel(AllBlockPartials partial, BlockState referenceState, Direction dir, Supplier<MatrixStack> modelTransform) {
        return get(Compartment.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
                   () -> buildModel(partial.get(), referenceState, modelTransform.get()));
    }

    public MODEL getModel(Compartment<BlockState> compartment, BlockState toRender) {
        return get(compartment, toRender, () -> buildModel(toRender));
    }

    public <T> MODEL get(Compartment<T> compartment, T key, Supplier<MODEL> supplier) {
        Cache<Object, MODEL> compartmentCache = models.get(compartment);
        try {
            return compartmentCache.get(key, supplier::get);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private MODEL buildModel(BlockState renderedState) {
        BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
        return buildModel(dispatcher.getModel(renderedState), renderedState);
    }

    private MODEL buildModel(BakedModel model, BlockState renderedState) {
        return buildModel(model, renderedState, new MatrixStack());
    }

    private MODEL buildModel(BakedModel model, BlockState referenceState, MatrixStack ms) {
        BufferBuilder builder = SuperByteBufferCache.getBufferBuilder(model, referenceState, ms);

        return factory.makeModel(renderer, builder);
    }

}
