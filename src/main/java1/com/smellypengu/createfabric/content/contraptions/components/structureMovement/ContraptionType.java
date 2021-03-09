package com.smellypengu.createfabric.content.contraptions.components.structureMovement;

import com.smellypengu.createfabric.content.contraptions.components.structureMovement.bearing.StabilizedContraption;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class ContraptionType {

	public static Map<String, ContraptionType> entries = new HashMap<>();
	public static ContraptionType
		/*PISTON = register("piston", PistonContraption::new),
		BEARING = register("bearing", BearingContraption::new), TODO DO THIS POGCHAMP
		PULLEY = register("pulley", PulleyContraption::new),
		CLOCKWORK = register("clockwork", ClockworkContraption::new),
		MOUNTED = register("mounted", MountedContraption::new),*/
		STABILIZED = register("stabilized", StabilizedContraption::new);
		//GANTRY = register("gantry", GantryContraption::new);

	Supplier<? extends Contraption> factory;
	String id;

	public static ContraptionType register(String id, Supplier<? extends Contraption> factory) {
		ContraptionType value = new ContraptionType(id, factory);
		entries.put(id, value);
		return value;
	}

	private ContraptionType(String id, Supplier<? extends Contraption> factory) {
		this.factory = factory;
		this.id = id;
	}

	public static Contraption fromType(String type) {
		for (Entry<String, ContraptionType> allContraptionTypes : entries.entrySet())
			if (type.equals(allContraptionTypes.getKey()))
				return allContraptionTypes.getValue().factory.get();
		return null;
	}

}