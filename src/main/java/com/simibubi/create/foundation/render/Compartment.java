package com.simibubi.create.foundation.render;

import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlockPartials;

import net.minecraft.block.BlockState;

public class Compartment<T> {
    public static final Compartment<BlockState> GENERIC_TILE = new Compartment<>();
    public static final Compartment<AllBlockPartials> PARTIAL = new Compartment<>();
    public static final Compartment<Pair<Direction, AllBlockPartials>> DIRECTIONAL_PARTIAL = new Compartment<>();
}
