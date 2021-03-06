package com.smellypengu.createfabric.content.contraptions;

import java.util.HashMap;
import java.util.Map;

import com.smellypengu.createfabric.Create;
import com.smellypengu.createfabric.content.contraptions.base.KineticTileEntity;
import net.minecraft.world.WorldAccess;

public class TorquePropagator {

	static Map<WorldAccess, Map<Long, KineticNetwork>> networks = new HashMap<>();

	public void onLoadWorld(WorldAccess world) {
		networks.put(world, new HashMap<>());
		Create.logger.debug("Prepared Kinetic Network Space for " + world.getDimension()); // TODO COULD BE WRONG HERE
	}

	public void onUnloadWorld(WorldAccess world) {
		networks.remove(world);
		Create.logger.debug("Removed Kinetic Network Space for " + world.getDimension());
	}

	public KineticNetwork getOrCreateNetworkFor(KineticTileEntity te) {
		Long id = te.network;
		KineticNetwork network;
		Map<Long, KineticNetwork> map = networks.get(te.getWorld());
		if (id == null)
			return null;

		if (!map.containsKey(id)) {
			network = new KineticNetwork();
			network.id = te.network;
			map.put(id, network);
		}
		network = map.get(id);
		return network;
	}

}
