package com.simibubi.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.relays.encased.CasingConnectivity;
import com.simibubi.create.content.schematics.ClientSchematicLoader;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.foundation.ResourceReloadHandler;
import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.item.CustomItemModels;
import com.simibubi.create.foundation.item.CustomRenderedItems;
import com.simibubi.create.foundation.render.KineticRenderer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.OptifineHandler;
import com.simibubi.create.foundation.utility.ghost.GhostBlocks;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateClient {

	public static ClientSchematicLoader schematicSender;
	public static SchematicHandler schematicHandler;
	public static SchematicAndQuillHandler schematicAndQuillHandler;
	public static SuperByteBufferCache bufferCache;
	public static KineticRenderer kineticRenderer;
	public static final Outliner outliner = new Outliner();
	public static GhostBlocks ghostBlocks;

	private static CustomBlockModels customBlockModels;
	private static CustomItemModels customItemModels;
	private static CustomRenderedItems customRenderedItems;
	private static AllColorHandlers colorHandlers;
	private static CasingConnectivity casingConnectivity;

	public static void addClientListeners(IEventBus modEventBus) {
		modEventBus.addListener(CreateClient::clientInit);
		modEventBus.addListener(CreateClient::onModelBake);
		modEventBus.addListener(CreateClient::onModelRegistry);
		modEventBus.addListener(CreateClient::onTextureStitch);
		modEventBus.addListener(AllParticleTypes::registerFactories);

		Backend.init();
		OptifineHandler.init();
	}

	public static void clientInit(FMLClientSetupEvent event) {
		kineticRenderer = new KineticRenderer();

		schematicSender = new ClientSchematicLoader();
		schematicHandler = new SchematicHandler();
		schematicAndQuillHandler = new SchematicAndQuillHandler();

		bufferCache = new SuperByteBufferCache();
		bufferCache.registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
		bufferCache.registerCompartment(ContraptionRenderDispatcher.CONTRAPTION, 20);

		ghostBlocks = new GhostBlocks();

		AllKeys.register();
		AllContainerTypes.registerScreenFactories();
		//AllTileEntities.registerRenderers();
		AllEntityTypes.registerRenderers();
		getColorHandler().init();
		AllFluids.assignRenderLayers();

		ResourceManager resourceManager = MinecraftClient.getInstance()
			.getResourceManager();
		if (resourceManager instanceof ReloadableResourceManager)
			((ReloadableResourceManager) resourceManager).registerListener(new ResourceReloadHandler());
	}

	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		if (!event.getMap()
			.getId()
			.equals(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE))
			return;
		SpriteShifter.getAllTargetSprites()
			.forEach(event::addSprite);
	}

	public static void onModelBake(ModelBakeEvent event) {
		Map<Identifier, BakedModel> modelRegistry = event.getModelRegistry();
		AllBlockPartials.onModelBake(event);

		getCustomBlockModels()
			.foreach((block, modelFunc) -> swapModels(modelRegistry, getAllBlockStateModelLocations(block), modelFunc));
		getCustomItemModels()
			.foreach((item, modelFunc) -> swapModels(modelRegistry, getItemModelLocation(item), modelFunc));
		getCustomRenderedItems().foreach((item, modelFunc) -> {
			swapModels(modelRegistry, getItemModelLocation(item), m -> modelFunc.apply(m)
				.loadPartials(event));
		});
	}

	public static void onModelRegistry(ModelRegistryEvent event) {
		AllBlockPartials.onModelRegistry(event);

		getCustomRenderedItems().foreach((item, modelFunc) -> modelFunc.apply(null)
			.getModelLocations()
			.forEach(ModelLoader::addSpecialModel));
	}

	protected static ModelIdentifier getItemModelLocation(Item item) {
		return new ModelIdentifier(item.getRegistryName(), "inventory");
	}

	protected static List<ModelIdentifier> getAllBlockStateModelLocations(Block block) {
		List<ModelIdentifier> models = new ArrayList<>();
		block.getStateManager()
			.getStates()
			.forEach(state -> {
				models.add(getBlockModelLocation(block, BlockModels.propertyMapToString(state.getEntries())));
			});
		return models;
	}

	protected static ModelIdentifier getBlockModelLocation(Block block, String suffix) {
		return new ModelIdentifier(block.getRegistryName(), suffix);
	}

	protected static <T extends BakedModel> void swapModels(Map<Identifier, BakedModel> modelRegistry,
		List<ModelIdentifier> locations, Function<BakedModel, T> factory) {
		locations.forEach(location -> {
			swapModels(modelRegistry, location, factory);
		});
	}
	
	protected static <T extends BakedModel> void swapModels(Map<Identifier, BakedModel> modelRegistry,
		ModelIdentifier location, Function<BakedModel, T> factory) {
		modelRegistry.put(location, factory.apply(modelRegistry.get(location)));
	}

	public static CustomItemModels getCustomItemModels() {
		if (customItemModels == null)
			customItemModels = new CustomItemModels();
		return customItemModels;
	}

	public static CustomRenderedItems getCustomRenderedItems() {
		if (customRenderedItems == null)
			customRenderedItems = new CustomRenderedItems();
		return customRenderedItems;
	}

	public static CustomBlockModels getCustomBlockModels() {
		if (customBlockModels == null)
			customBlockModels = new CustomBlockModels();
		return customBlockModels;
	}

	public static AllColorHandlers getColorHandler() {
		if (colorHandlers == null)
			colorHandlers = new AllColorHandlers();
		return colorHandlers;
	}
	
	public static CasingConnectivity getCasingConnectivity() {
		if (casingConnectivity == null)
			casingConnectivity = new CasingConnectivity();
		return casingConnectivity;
	}

	public static void invalidateRenderers() {
		CreateClient.bufferCache.invalidate();
		CreateClient.kineticRenderer.invalidate();
		ContraptionRenderDispatcher.invalidateAll();
	}
}
