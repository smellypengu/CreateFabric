package com.simibubi.create.foundation.item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.registrate.util.nullness.NonNullBiConsumer;
import com.simibubi.create.registrate.util.nullness.NonNullFunction;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.Item;

public class CustomRenderedItems {

	private final List<Pair<Supplier<? extends Item>, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>>> registered;
	private final Map<Item, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>> customModels;

	public CustomRenderedItems() {
		registered = new ArrayList<>();
		customModels = new IdentityHashMap<>();
	}

	public void register(Supplier<? extends Item> entry,
						 NonNullFunction<BakedModel, ? extends CustomRenderedItemModel> behaviour) {
		registered.add(Pair.of(entry, behaviour));
	}

	public void foreach(NonNullBiConsumer<Item, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>> consumer) {
		loadEntriesIfMissing();
		customModels.forEach(consumer);
	}

	private void loadEntriesIfMissing() {
		if (customModels.isEmpty())
			loadEntries();
	}

	private void loadEntries() {
		customModels.clear();
		registered.forEach(p -> customModels.put(p.getKey()
			.get(), p.getValue()));
	}

}
