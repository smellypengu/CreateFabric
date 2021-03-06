package com.smellypengu.createfabric;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

abstract class CreateFluid extends FlowableFluid  {
    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }

    @Override
    protected boolean isInfinite() {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        final BlockEntity blockEntity = state.getBlock().getDefaultState().hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState fluidState, BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected int getFlowSpeed(WorldView worldView) {
        return 4;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView worldView) {
        return 1;
    }

    @Override
    public int getTickRate(WorldView worldView) {
        return 5;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0F;
    }
}

public class AllFluids {

    public abstract static class ChocolateFluid extends CreateFluid {
        @Override
        public Fluid getStill() {
            return STILL_CHOCOLATE;
        }

        @Override
        public Fluid getFlowing() {
            return FLOWING_CHOCOLATE;
        }

        @Override
        public Item getBucketItem() {
            return CHOCOLATE_BUCKET;
        }

        @Override
        protected int getLevelDecreasePerBlock(WorldView worldView) {
            return 2;
        }

        @Override
        public int getTickRate(WorldView worldview) { return 25; }

        @Override
        protected BlockState toBlockState(FluidState fluidState) {
            return CHOCOLATE.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(fluidState));
        }

        public static class Flowing extends ChocolateFluid {
            @Override
            protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
                super.appendProperties(builder);
                builder.add(LEVEL);
            }

            @Override
            public int getLevel(FluidState fluidState) {
                return fluidState.get(LEVEL);
            }

            @Override
            public boolean isStill(FluidState fluidState) {
                return false;
            }
        }

        public static class Still extends ChocolateFluid {
            @Override
            public int getLevel(FluidState fluidState) {
                return 8;
            }

            @Override
            public boolean isStill(FluidState fluidState) {
                return true;
            }
        }
    }

    public abstract static class HoneyFluid extends CreateFluid {
        @Override
        public Fluid getStill() {
            return STILL_HONEY;
        }

        @Override
        public Fluid getFlowing() {
            return FLOWING_HONEY;
        }

        @Override
        public Item getBucketItem() {
            return HONEY_BUCKET;
        }

        @Override
        protected int getLevelDecreasePerBlock(WorldView worldView) {
            return 2;
        }

        @Override
        public int getTickRate(WorldView worldview) { return 25; }

        @Override
        protected BlockState toBlockState(FluidState fluidState) {
            return HONEY.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(fluidState));
        }

        public static class Flowing extends HoneyFluid {
            @Override
            protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
                super.appendProperties(builder);
                builder.add(LEVEL);
            }

            @Override
            public int getLevel(FluidState fluidState) {
                return fluidState.get(LEVEL);
            }

            @Override
            public boolean isStill(FluidState fluidState) {
                return false;
            }
        }

        public static class Still extends HoneyFluid {
            @Override
            public int getLevel(FluidState fluidState) {
                return 8;
            }

            @Override
            public boolean isStill(FluidState fluidState) {
                return true;
            }
        }
    }

    public static FlowableFluid STILL_CHOCOLATE;
    public static FlowableFluid FLOWING_CHOCOLATE;

    public static Item CHOCOLATE_BUCKET;

    public static Block CHOCOLATE;

    public static FlowableFluid STILL_HONEY;
    public static FlowableFluid FLOWING_HONEY;

    public static Item HONEY_BUCKET;

    public static Block HONEY;

    public static void register() {
        STILL_CHOCOLATE = Registry.register(Registry.FLUID, new Identifier(Create.ID, "chocolate"), new ChocolateFluid.Still());
        FLOWING_CHOCOLATE = Registry.register(Registry.FLUID, new Identifier(Create.ID, "flowing_chocolate"), new ChocolateFluid.Flowing());

        CHOCOLATE = Registry.register(Registry.BLOCK, new Identifier(Create.ID, "chocolate"), new FluidBlock(STILL_CHOCOLATE, FabricBlockSettings.copy(Blocks.WATER)){});

        CHOCOLATE_BUCKET = Registry.register(Registry.ITEM, new Identifier(Create.ID, "chocolate_bucket"), new BucketItem(STILL_CHOCOLATE, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1).group(Create.baseCreativeTab)));

        STILL_HONEY = Registry.register(Registry.FLUID, new Identifier(Create.ID, "honey"), new HoneyFluid.Still());
        FLOWING_HONEY = Registry.register(Registry.FLUID, new Identifier(Create.ID, "flowing_honey"), new HoneyFluid.Flowing());

        HONEY = Registry.register(Registry.BLOCK, new Identifier(Create.ID, "honey"), new FluidBlock(STILL_HONEY, FabricBlockSettings.copy(Blocks.WATER)){});

        HONEY_BUCKET = Registry.register(Registry.ITEM, new Identifier(Create.ID, "honey_bucket"), new BucketItem(STILL_HONEY, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1).group(Create.baseCreativeTab)));
    }
}
