package com.smellypengu.createfabric.content.contraptions.relays.belt;

import com.smellypengu.createfabric.foundation.utility.Lang;
import net.minecraft.util.StringIdentifiable;

public enum BeltSlope implements StringIdentifiable {
	HORIZONTAL, UPWARD, DOWNWARD, VERTICAL, SIDEWAYS;

	@Override
	public String asString() {
		return Lang.asId(name());
	}

	public boolean isDiagonal() {
		return this == UPWARD || this == DOWNWARD;
	}
}